/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.platform;

public record ModRuntimeMetadata(
    String name,
    String version,
    String buildNumber,
    String commit,
    String[] authors,
    String color
) {
}
