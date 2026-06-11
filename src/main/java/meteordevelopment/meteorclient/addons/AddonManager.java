/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.addons;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.platform.ModPlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class AddonManager {
    public static final List<MeteorAddon> ADDONS = new ArrayList<>();

    public static void init() {
        // Meteor pseudo addon
        {
            MeteorClient.ADDON = new MeteorAddon() {
                @Override
                public void onInitialize() {}

                @Override
                public String getPackage() {
                    return "meteordevelopment.meteorclient";
                }

                @Override
                public String getWebsite() {
                    return "https://meteorclient.com";
                }

                @Override
                public GithubRepo getRepo() {
                    return new GithubRepo("MeteorDevelopment", "meteor-client");
                }

                @Override
                public String getCommit() {
                    return MeteorClient.COMMIT.isEmpty() ? null : MeteorClient.COMMIT;
                }
            };

            var metadata = ModPlatform.ownMetadata();
            MeteorClient.ADDON.name = metadata.name();
            MeteorClient.ADDON.authors = metadata.authors();
            MeteorClient.ADDON.color.parse(metadata.color());
            ADDONS.add(MeteorClient.ADDON);
        }

        for (MeteorAddon addon : ServiceLoader.load(MeteorAddon.class)) {
            try {
                populateMetadata(addon);
                ADDONS.add(addon);
            } catch (ServiceConfigurationError error) {
                throw new RuntimeException("Failed to load Meteor addon from service entry.", error);
            }
        }
    }

    private static void populateMetadata(MeteorAddon addon) {
        MeteorAddonMetadata metadata = addon.getClass().getAnnotation(MeteorAddonMetadata.class);

        addon.name = metadata != null && !metadata.name().isBlank() ? metadata.name() : addon.getClass().getSimpleName();
        addon.authors = metadata != null ? metadata.authors() : new String[0];

        if (metadata != null && !metadata.color().isBlank()) {
            addon.color.parse(metadata.color());
        }
    }
}
