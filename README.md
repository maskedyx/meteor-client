
<p align="center">
<img src="https://meteorclient.com/icon.png" alt="meteor-client-logo" width="15%"/>
</p>

<h1 align="center">Meteor</h1>
<p align="center">A Minecraft NeoForge Utility Mod for anarchy servers.</p>

<div align="center">
    <a href="https://discord.gg/bBGQZvd"><img src="https://img.shields.io/discord/689197705683140636?logo=discord" alt="Discord"/></a>
    <br>
    <img src="https://img.shields.io/github/last-commit/MeteorDevelopment/meteor-client" alt="GitHub last commit"/>
    <img src="https://img.shields.io/github/commit-activity/w/MeteorDevelopment/meteor-client" alt="GitHub commit activity"/>
    <img src="https://img.shields.io/github/contributors/MeteorDevelopment/meteor-client" alt="GitHub contributors"/>
    <br>
    <img src="https://img.shields.io/github/languages/code-size/MeteorDevelopment/meteor-client" alt="GitHub code size in bytes"/>
    <img src="https://img.shields.io/endpoint?url=https://ghloc.vercel.app/api/MeteorDevelopment/meteor-client/badge?filter=.java$&label=lines%20of%20code&color=blue" alt="GitHub lines of code"/>
</div>

## Usage

### Building
- Clone this repository.
- Make sure you have network access to the NeoForged and Meteor Maven repositories used by Gradle.
- Install a JDK that can satisfy the Java 21 toolchain declared in `gradle/libs.versions.toml`.
- Run `./gradlew build`.
- Find the built jar in `build/libs/`.

### Testing
- This repository currently does not contain a dedicated `src/test` suite; the existing automated check is the Gradle build used by CI.
- Run `./gradlew build` before opening a pull request to verify that the client still compiles and packages correctly.
- For gameplay or compatibility changes, run `./gradlew runClient` and smoke-test the affected behavior in a NeoForge client instance for the supported Minecraft version.

### Compatibility Checklist
If you remove or alter compatibility-sensitive files or declarations, double-check the related metadata before merging:

- Keep the Minecraft, NeoForge, and Java versions aligned with `gradle/libs.versions.toml`, `build.gradle.kts`, and `src/main/resources/META-INF/neoforge.mods.toml`.
- If you add, remove, or rename mixin configuration files, update both `src/main/resources/META-INF/neoforge.mods.toml` and the jar manifest configuration in `build.gradle.kts`.
- If you change access widening, keep `src/main/resources/META-INF/accesstransformer.cfg` and `src/main/resources/meteor-client.classtweaker` in sync.
- If you change build metadata placeholders, make sure `src/main/resources/META-INF/neoforge.mods.toml` and `src/main/resources/meteor-client.properties` still receive the expected values from `processResources`.
- Re-run `./gradlew build` after those changes and do a local smoke test with NeoForge when the change affects runtime behavior.

### Installation
Install NeoForge for the supported Minecraft version, then place the built jar in your `mods` folder.

## Contributions
We will review and help with all reasonable pull requests as long as the guidelines below are met.

- The license header must be applied to all java source code files.
- IDE or system-related files should be added to the `.gitignore`, never committed in pull requests.
- In general, check existing code to make sure your code matches relatively close to the code already in the project.
- Favour readability over compactness.
- If you need help, check out the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) for a reference.

## Bugs and Suggestions
Bug reports and suggestions should be made in this repo's [issue tracker](https://github.com/MeteorDevelopment/meteor-client/issues) using the templates provided.  
Please provide as much information as you can to best help us understand your issue and give a better chance of it being resolved.

## Donations
All of our work is completely free and non-profit (donations pay only for hosting costs), therefore we are very grateful for all donations made to support us in running our community.  
Donations can be made via our [website](https://meteorclient.com/donate) and the minimum amount to get donor benefits is €5.  
You will be rewarded with a role on our Discord server and a customisable in-game cape.  
⚠️ _Make sure to create a Meteor account and link your Discord and Minecraft accounts to fully experience your rewards._ ⚠️

## Credits
[Cabaletta](https://github.com/cabaletta) and [WagYourTail](https://github.com/wagyourtail) for [Baritone](https://github.com/cabaletta/baritone)  
The [Fabric Team](https://github.com/FabricMC) for [Fabric](https://github.com/FabricMC/fabric-loader) and [Yarn](https://github.com/FabricMC/yarn)

## Licensing
This project is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html). 

If you use **ANY** code from the source:
- You must disclose the source code of your modified work and the source code you took from this project. This means you are not allowed to use code from this project (even partially) in a closed-source and/or obfuscated application.
- You must state clearly and obviously to all end users that you are using code from this project.
- Your application must also be licensed under the same license.

*If you have any other questions, check our [FAQ](https://meteorclient.com/faq) or ask in our [Discord](https://meteorclient.com/discord) server.*
