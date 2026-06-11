/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.platform;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

public final class ModPlatform {
    private static final ModRuntimeMetadata OWN_METADATA = loadOwnMetadata();

    private ModPlatform() {
    }

    public static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    public static boolean isDevelopmentEnvironment() {
        return FMLEnvironment.production == false;
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static ModRuntimeMetadata ownMetadata() {
        return OWN_METADATA;
    }

    private static ModRuntimeMetadata loadOwnMetadata() {
        Properties properties = new Properties();

        try (InputStream stream = ModPlatform.class.getClassLoader().getResourceAsStream("meteor-client.properties")) {
            if (stream != null) properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load bundled mod metadata.", e);
        }

        return new ModRuntimeMetadata(
            properties.getProperty("name", "Meteor Client"),
            properties.getProperty("version", "0.0.0"),
            properties.getProperty("build_number", ""),
            properties.getProperty("commit", ""),
            splitCsv(properties.getProperty("authors", "")),
            properties.getProperty("color", "145,61,226")
        );
    }

    private static String[] splitCsv(String value) {
        if (value.isBlank()) return new String[0];

        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(part -> !part.isEmpty())
            .toArray(String[]::new);
    }
}
