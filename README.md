# Create: Better Networking

Clipboard tools for copying, applying, naming and highlighting Create logistics networks.

**Minecraft 1.21.1 | NeoForge 21.1.255+ | Create 6.0.x**

## Download and install

[Download Create: Better Networking 0.0.2](https://github.com/IWasCool/create-better-networking/releases/download/v0.0.2/create_better_networking-0.0.2.jar)

Place the JAR in your `mods` folder alongside Create and its required dependencies.

- **Client only:** select networks locally and see nearby synchronized members, outlines and HUD counts.
- **Client and server:** enable full clipboard copy/apply, shared network names and untuned gauge placement.

Server support is detected automatically. Local selections clear when you disconnect. Client-only highlights
include only loaded members whose network data the server sends to your client.

## Controls

Hold a Create clipboard in either hand:

| Action | Result |
| --- | --- |
| Shift-left-click a supported block | Select or copy its network |
| Left-click a supported block | Apply the saved network with server support |
| Shift-left-click air | Clear the saved selection |
| Hold a clipboard with a selection | Highlight nearby network members and show their count |

Works with stock links, stock-checking blocks, factory gauges and compatible addon blocks. The clipboard targets
the gauge panel under your crosshair and prevents accidental block mining in survival and creative mode.

With server support, add a clipboard entry such as `#network: Link Series` to select a named network. Remove the
entry to return to the copied selection. Network changes respect Create's locks and addon permissions.

The `showNetworkIdInHud` setting in `config/create_better_networking-common.toml` controls unnamed network labels.

[Recent changes](CHANGELOG.md) | [Detailed usage](docs/usage.md) | [Developer information](docs/README.md)

Licensed under [MIT](LICENSE).
