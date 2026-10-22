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

Runtime resource bytes are preserved by `.gitattributes`. JAR timestamps and entry ordering are reproducible;
compare entry contents when the preceding artifact used different ZIP timestamps. Document intentional metadata
changes separately from gameplay changes.

The tracked build contains no custom tests, fixtures, diagnostic harness or GitHub Actions workflow. Gradle's
standard test task has no test sources, so a successful production build is compilation and packaging evidence.
It does not certify in-game behavior, client rendering or a published release.

## Historical checks

Before the 2026-10-22 repository cleanup, `cleanTest build --no-build-cache` ran the original suite: **161 passed,
2 skipped, 0 failed**. The skipped cases required optional Stock Bridge and Factory Logistics addons. Language
checks compared all 137 shipped locales with the Minecraft 1.21.1 language catalog and verified keys,
placeholders, duplicate keys and the literal clipboard command example.

The original tests, fixture, build files, baseline artifacts and results are preserved in a local archive outside
the repository. Historical test commands require that archived setup. See [addon compatibility](addon-compatibility.md)
for the earlier optional matrix and [CONTRIBUTING.md](../CONTRIBUTING.md) for the production-source-only scope.
