# Build verification

Use JDK 21 and the included Gradle wrapper:

```powershell
.\gradlew.bat build
```

On Linux or macOS, use `bash gradlew build`. To force recompilation of the production artifact, run
`.\gradlew.bat clean build --no-build-cache` (or `bash gradlew clean build --no-build-cache`). The build creates
`build/libs/create_better_networking-0.0.2.jar` and its `-sources.jar` companion.

## Public dependencies

Create 6.0.10 resolves as `curse.maven:create-328085:7963363`. Its expected JAR SHA-256 is recorded in
`gradle/mod-dependencies.json`; the digest is a reference for dependency reviews, not an automatic verification
rule. Ponder, Mixin, Minecraft tooling, NeoForge and mappings resolve through the configured repositories and
ModDevGradle. Their versions remain pinned in the build and properties files.

Neither `libs/` nor a local modpack is a build input. For repository-readiness checks, build a separate checkout
containing only Git-visible files. It must compile and package without copying local dependencies or IDE files.

## Packaging checks

For a repository cleanup, compare archive entries and their uncompressed content with the preceding baseline.
Verify production class files, mixin registrations, locale resources and expanded mod metadata. The installable
JAR must not contain test classes, authoring inputs, dependencies, local paths, unexpanded metadata placeholders
or development tooling. The sources JAR should contain the corresponding production Java and resources.
Both JARs must contain `META-INF/LICENSE` with the project MIT license and
`META-INF/LICENSES/NeoForge-MDK-MIT.txt` with the retained template notice. The expanded mod metadata must declare
`license="MIT"`.

Runtime resource bytes are preserved by `.gitattributes`. JAR timestamps and entry ordering are reproducible;
compare entry contents when the preceding artifact used different ZIP timestamps. Document intentional metadata
changes separately from gameplay changes.

The tracked build contains no custom tests, fixtures, diagnostic harness or GitHub Actions workflow. Gradle's
standard test task has no test sources, so a successful production build is compilation and packaging evidence.
It does not certify in-game behavior, client rendering or a published release.

## Optional server support checks

On 2026-10-24, a separate NeoForge/JUnit harness ran the current production sources with the archived regression
tests and new optional-server cases: **168 passed, 0 skipped, 0 failed**. The harness and reports remain outside
the production repository. The production Gradle build still has no custom test sources.

These checks exercise the actual placement and frequency mixins, all 137 language files, addon discovery,
server-provided highlights with negotiated channels, clients without channels, local selection and clearing
without item/network writes, held-attack cancellation, disconnect cleanup, and scans limited to received chunks.
They also check that a supporting server retains block-click handling and receives clipboard-reset requests.
Client input, connections and levels are mocked; these checks do not establish live multiplayer or rendering behavior.

For an in-game check, use a fresh test world and these installation combinations:

| Installation | Expected behavior |
| --- | --- |
| Client only, Create on the server | Join successfully; Shift-left-click selects, Shift-left-click air clears, loaded members highlight; applying explains the server requirement; untuned gauges keep Create's placement gate |
| Both sides | Join successfully; copy/apply, shared names, untuned gauges and server membership snapshots work |
| Server only, Create on the client | Join successfully; the player's clipboard retains normal Create interactions |

In client-only mode, verify main-hand and offhand clipboards, survival and creative attacks, multiple gauge panels,
dimension changes, and disconnect/reconnect. Selection should survive a dimension change, clear on disconnect,
and never change server-side clipboard data or block networks. Counts should reflect only synchronized loaded
members. With both sides installed, confirm protected networks still reject changes and stale snapshots do not
leave an old block count in the HUD.

## Historical checks

Before the 2026-10-22 repository cleanup, `cleanTest build --no-build-cache` ran the original suite: **161 passed,
2 skipped, 0 failed**. The skipped cases required optional Stock Bridge and Factory Logistics addons. Language
checks compared all 137 shipped locales with the Minecraft 1.21.1 language catalog and verified keys,
placeholders, duplicate keys and the literal clipboard command example.

The original tests, fixture, build files, baseline artifacts and results are preserved in a local archive outside
the repository. Historical test commands require that archived setup. See [addon compatibility](addon-compatibility.md)
for the earlier optional matrix and [CONTRIBUTING.md](../CONTRIBUTING.md) for the production-source-only scope.
