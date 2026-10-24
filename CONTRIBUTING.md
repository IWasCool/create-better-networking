# Development

Use JDK 21 and the checked-in Gradle wrapper. Build with `gradlew.bat build` on Windows or `./gradlew build` on
Linux/macOS. Dependencies resolve from public repositories; their versions are in `gradle.properties` and
[gradle/mod-dependencies.json](gradle/mod-dependencies.json).

[Architecture](docs/architecture.md) describes the classes, mixins and data formats.
[Addon integration](docs/addon-compatibility.md) covers the frequency interfaces and capabilities.
[Build verification](docs/testing.md) describes the current checks.

Use four-space indentation, UTF-8 and LF line endings. Keep mixins narrowly scoped and client classes behind
client-only registration. Preserve the mod ID, saved-data keys, capability IDs and payload formats unless a change
includes migration or protocol support. Update the mixin JSON when adding or moving a mixin.

Keep translation keys and placeholders consistent across locales. Preserve the literal `#network: Link Series`
example and retain third-party license notices.

This repository is a source reference for people inspecting, reusing or porting the mod. Keep the README brief.
Commit production source, resources, build files and useful developer notes. Keep download promotion, installation
tutorials, local build results and housekeeping history out of the repository. Local artifacts and deployment
records remain ignored.
