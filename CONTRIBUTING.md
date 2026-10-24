# Working on Better Networking

Use JDK 21 and the checked-in Gradle wrapper. Read [architecture](docs/architecture.md), [usage](docs/usage.md) and
[addon compatibility](docs/addon-compatibility.md) before changing a subsystem.

## Setup and verification

```powershell
.\gradlew.bat build
```

On Linux or macOS, use `bash gradlew build`. The build resolves the pinned Create release from CurseMaven and Ponder
from Create's Maven repository. `libs/` is ignored and is not a build input. Update [the dependency manifest](gradle/mod-dependencies.json)
and verify the downloaded JAR's SHA-256 when deliberately changing Create versions.

The tracked build contains production source only. Earlier tests and optional automation are preserved outside the
repository; their commands are historical. For packaging changes, compare the rebuilt JAR with the preceding
artifact. A successful build does not establish in-game compatibility. See [build verification](docs/testing.md).

## Code style and compatibility

Use four-space indentation, UTF-8, LF line endings and a 120-column target. Prefer explicit control flow and small
methods. Keep comments that explain compatibility constraints. Mixins should adapt the smallest practical target.
Client classes must stay behind client-only registration or client mixin entries.

Preserve the mod ID, saved-data ID, clipboard data keys, translation keys, capability IDs and payload formats unless
the change includes migration or protocol support. When moving a mixin, update both its class and
`src/main/resources/create_better_networking.mixins.json`.

Runtime resources and metadata templates are copied into the JAR. `.gitattributes` preserves their bytes during checkout. Keep
translation keys and placeholders consistent across all 137 locales; preserve the literal `#network: Link Series`
example. Do not replace shipped assets with authoring files.

## Repository scope

Commit production Java, shipped resources and metadata, required Gradle files, repository configuration, license
notices and documentation. Tests, fixtures, diagnostics, installers, modpack overlays, artwork originals and
optional automation stay outside Git, matching Create: New World Core.

Build and distribute only the installable mod JAR. Release downloads must not include sources, test, development
or dependency JARs. Keep release notes and the changelog focused on player-facing changes; license details belong
in `LICENSE` and the license notices.

Local worlds, dependencies, caches, logs and credentials remain ignored. Record public changes in
[CHANGELOG.md](CHANGELOG.md); local deployment history belongs in ignored `MODPACK_STATUS.md`. Do not infer an
installed or published release from a successful build. The project's MIT license applies; retain third-party notices.
