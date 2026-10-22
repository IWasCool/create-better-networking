# Clipboard controls

Hold a Create clipboard in the main hand or offhand. Shift-left-click a supported block to copy its logistics
network, then left-click another supported block to apply it. Shift-left-click air to clear saved network data.
Holding any clipboard prevents block mining in survival and creative, including blocks without network support.

Stock links, stock-checking blocks and compatible addon blocks are discovered through their network behaviours or
explicit adapters. Factory gauges use the active panel under the crosshair. An untuned gauge can be placed first
and connected afterwards. Copying from an unconnected block does not select a network.

The server checks Create's network permissions before copying or replacing a network. Addon adapters can impose
additional interaction rules. Successful changes update network membership, invalidate logistics summaries and
refresh nearby highlights.

## Named networks

Add a clipboard entry containing:

```text
#network: Link Series
```

Replace `Link Series` with a name of your choice, then apply the clipboard to a block. The first nonempty
`#network:` entry selects a deterministic network UUID and takes priority over copied data. Leading and trailing
spaces are trimmed, names are limited to 64 characters, and UUID selection is case-insensitive. Ordinary quoted
text is ignored. The displayed name is saved in the world's shared network-name data when applied.

A name selects a network; it does not rename an arbitrary copied UUID. Copying another network while the control
entry remains present does not override the name selection. Clearing saved network data leaves clipboard text
intact, so remove the `#network:` entry to stop selecting that named network.

## Highlights and feedback

A clipboard's selected network takes priority over held items. Without a clipboard selection, supported held block
items use Create's standard `BLOCK_ENTITY_DATA` UUID named `Freq`; item capability adapters can provide custom
storage. Members of the selected network receive outlines, and the HUD shows the network name or shortened UUID
and the number of distinct highlighted blocks.

Membership snapshots come from the server once every 20 player ticks and immediately after copy/paste operations.
Discovery scans already loaded chunks within four chunks of the player and applies an interaction-distance check;
it does not load distant chunks or enumerate the entire world. The client discards snapshots from other
dimensions and stops drawing snapshots older than 60 ticks. Multiple panels on one gauge count as one block.

Copy, paste, clear and permission feedback appears near the hotbar. The common config option `showNetworkIdInHud`
in `config/create_better_networking-common.toml` controls unnamed UUID labels and defaults to `true`.
