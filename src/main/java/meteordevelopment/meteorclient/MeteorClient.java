/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient;

import meteordevelopment.meteorclient.addons.AddonManager;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.meteor.KeyInputEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.hud.screens.AddHudElementScreen;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.systems.hud.screens.HudElementScreen;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.DiscordPresence;
import meteordevelopment.meteorclient.platform.ModPlatform;
import meteordevelopment.meteorclient.platform.ModRuntimeMetadata;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.ReflectInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Version;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.misc.input.KeyBinds;
import meteordevelopment.meteorclient.utils.network.OnlinePlayers;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import meteordevelopment.orbit.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.File;
import java.lang.invoke.MethodHandles;

@Mod(MeteorClient.MOD_ID)
public class MeteorClient {
    public static final String MOD_ID = "meteor-client";
    private static final ModRuntimeMetadata MOD_METADATA = ModPlatform.ownMetadata();
    public static final String NAME;
    public static final Version VERSION;
    public static final String BUILD_NUMBER;
    public static final String COMMIT;

    public static MeteorClient INSTANCE;
    public static MeteorAddon ADDON;

    public static Minecraft mc;
    public static final IEventBus EVENT_BUS = new EventBus();
    public static final File FOLDER = ModPlatform.getGameDir().resolve(MOD_ID).toFile();
    public static final Logger LOG;
    private static boolean initialized;

    static {
        NAME = MOD_METADATA.name();
        LOG = LoggerFactory.getLogger(NAME);

        String versionString = MOD_METADATA.version();
        if (versionString.contains("-")) versionString = versionString.split("-")[0];

        if (versionString.equals("${version}")) versionString = "0.0.0";

        VERSION = new Version(versionString);
        BUILD_NUMBER = MOD_METADATA.buildNumber();
        COMMIT = MOD_METADATA.commit();
    }

    public MeteorClient(ModContainer modContainer, net.neoforged.bus.api.IEventBus modEventBus) {
        INSTANCE = this;
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        if (initialized) return;
        initialized = true;
        event.enqueueWork(this::initializeClient);
    }

    private void initializeClient() {
        // Global minecraft client accessor
        mc = Minecraft.getInstance();

        if (ModPlatform.isDevelopmentEnvironment()) {
            LOG.info("Force loading mixins");
            MixinEnvironment.getCurrentEnvironment().audit();
        }

        LOG.info("Initializing {}", NAME);

        // Pre-load
        if (!FOLDER.exists()) {
            FOLDER.getParentFile().mkdirs();
            FOLDER.mkdir();
            Systems.addPreLoadTask(() -> Modules.get().get(DiscordPresence.class).enable());
        }

        // Register addons
        AddonManager.init();

        // Register event handlers
        AddonManager.ADDONS.forEach(addon -> {
            try {
                EVENT_BUS.registerLambdaFactory(addon.getPackage(), (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
            } catch (AbstractMethodError e) {
                throw new RuntimeException("Addon \"%s\" is too old and cannot be ran.".formatted(addon.name), e);
            }
        });

        // Register init classes
        ReflectInit.registerPackages();

        // Pre init
        ReflectInit.init(PreInit.class);

        // Register module categories
        Categories.init();

        // Load systems
        Systems.init();

        // Subscribe after systems are loaded
        EVENT_BUS.subscribe(this);

        // Initialise addons
        AddonManager.ADDONS.forEach(MeteorAddon::onInitialize);

        // Sort modules after addons have added their own
        Modules.get().sortModules();

        // Load configs
        Systems.load();

        // Post init
        ReflectInit.init(PostInit.class);

        // Save on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            OnlinePlayers.leave();
            Systems.save();
            GuiThemes.save();
        }));
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.screen == null && mc.getOverlay() == null && KeyBinds.OPEN_COMMANDS.consumeClick()) {
            mc.setScreen(new ChatScreen(Config.get().prefix.get(), true));
        }
    }

    @EventHandler
    private void onKey(KeyInputEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matches(event.input)) {
            toggleGui();
        }
    }

    @EventHandler
    private void onMouseClick(MouseClickEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matchesMouse(event.click)) {
            toggleGui();
        }
    }

    private void toggleGui() {
        if (Utils.canCloseGui()) mc.screen.onClose();
        else if (Utils.canOpenGui()) Tabs.get().getFirst().openScreen(GuiThemes.get());
    }

    // Hide HUD

    private boolean wasWidgetScreen, wasHudHiddenRoot;

    @EventHandler(priority = EventPriority.LOWEST)
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof WidgetScreen) {
            if (!wasWidgetScreen) wasHudHiddenRoot = mc.options.hideGui;
            if (GuiThemes.get().hideHUD() || wasHudHiddenRoot) {
                // Always show the MC HUD in the HUD editor screen since people like
                // to align some items with the hotbar or chat
                mc.options.hideGui = !(event.screen instanceof HudEditorScreen)
                    && !(event.screen instanceof AddHudElementScreen)
                    && !(event.screen instanceof HudElementScreen);
            }
        } else {
            if (wasWidgetScreen) mc.options.hideGui = wasHudHiddenRoot;
            wasHudHiddenRoot = mc.options.hideGui;
        }

        wasWidgetScreen = event.screen instanceof WidgetScreen;
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(MeteorClient.MOD_ID, path);
    }
}
