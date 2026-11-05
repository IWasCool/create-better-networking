# Build verification

Use JDK 21 and the included Gradle wrapper:

```powershell
.\gradlew.bat clean build --no-build-cache
```

On Linux/macOS, use `./gradlew clean build --no-build-cache`.
The output is `build/libs/create_better_networking-1.0.0.jar`.

Dependencies resolve from public repositories. The pinned Create release and reference checksum are in
[the dependency manifest](../gradle/mod-dependencies.json). Local modpacks and `libs/` are not build inputs.

Check the JAR's expanded mod metadata, mixin registrations, translations and license notices. The artifact should
contain production classes and runtime resources. Keep test classes, development tooling and dependencies out of
the JAR. Only the installable mod JAR is generated.

There are no automated tests in this repository. A successful build verifies compilation and packaging. After
behavior changes, check client-only mode, both-side support, protected networks, addon integration and dedicated
server loading in game.
