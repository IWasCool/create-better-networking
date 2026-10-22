# Create: Better Networking

Minecraft **1.21.1** · NeoForge · Mod **0.0.2**

Clipboard tools for copying, applying, naming and highlighting Create logistics networks. Works with stock links,
stock-checking blocks, factory gauges and compatible addon blocks.

## Controls

Hold a Create clipboard in either hand:

| Action | Result |
| --- | --- |
| Shift-left-click a supported block | Copy its logistics network to the clipboard |
| Left-click a supported block | Apply the clipboard's network |
| Shift-left-click air | Clear the saved network |
| Hold a clipboard with a network | Highlight nearby loaded members and show their count |

To select a named network, add a clipboard entry containing `#network: Link Series`, replacing `Link Series` with
your chosen name. The name selects a network and takes priority over a copied network. Remove that entry to return
to ordinary copy and paste. See [the usage guide](docs/usage.md) for naming and highlighting details.

Holding a clipboard prevents block mining in survival and creative mode and suppresses mining swings and block-hit
particles. Network changes respect Create's network locks and addon permissions. Factory gauges can be placed before
assigning a network; the clipboard targets the panel under the crosshair.

## Install

Install the mod on **both client and server** with Minecraft **1.21.1**, NeoForge **21.1.255 or newer within the 1.21.1
line**, and Create **6.0.x**. The development baseline is Create **6.0.10**. Install Create's required dependencies too.

The common config file is `config/create_better_networking-common.toml`. `showNetworkIdInHud` defaults to `true` and
controls whether unnamed network IDs appear in the highlight HUD. Named networks still show their names.

## Build

Install **JDK 21**, then run the included Gradle wrapper:

```powershell
.\gradlew.bat build
```

On Linux or macOS, run `bash gradlew build`. Dependencies download automatically from public repositories; a clean
checkout needs no local modpack or `libs/` directory. The Create release and reference checksum are recorded in
[gradle/mod-dependencies.json](gradle/mod-dependencies.json).

The installable mod is `build/libs/create_better_networking-0.0.2.jar`. The `-sources.jar` contains its production
source and resources. See [build verification](docs/testing.md) for the scope of these checks.

## Addon support and translations

Addons using Create's `LogisticallyLinkedBehaviour` inherit clipboard support automatically, including subclasses
with custom behaviour types. Addons with custom logistics storage can expose the frequency interfaces or NeoForge
capabilities in `com.ourcreationstudios.create_better_networking.api`.

[Addon compatibility](docs/addon-compatibility.md) covers adapter priorities, multi-network blocks, held items,
permissions and the historical compatibility matrix. UUID-based Create logistics networks are supported; unrelated
frequency systems, such as item-pair redstone links, need their own integration.

Translations cover all **137 language locales** shipped with Minecraft 1.21.1 and the mod's **16 translation keys**.
These are initial machine-assisted translations; native-speaker review is welcome. Keep the literal
`#network: Link Series` command example unchanged in translations.

## Repository layout

| Directory | Contents |
| --- | --- |
| `src/main/java/` | Production code, addon API and four mixins |
| `src/main/resources/` | Shipped translations, artwork and mixin registration |
| `src/main/templates/` | Mod metadata expanded during the build |
| `gradle/` | Dependency configuration and Gradle wrapper |
| `docs/` | Usage, architecture, addon integration and build verification |
| `LICENSES/` | Retained template license and third-party boundaries |

This repository follows Create: New World Core's production-source-only layout. Earlier tests, their fixtures and
optional automation are archived outside the repository. Local JARs, worlds, IDE files, logs and build outputs are
ignored. See [CONTRIBUTING.md](CONTRIBUTING.md) before changing code.

## License

Project code and artwork retain the existing **All Rights Reserved** license. See [LICENSE](LICENSE) and
[LICENSES/README.md](LICENSES/README.md) for the retained NeoForge template notice.
