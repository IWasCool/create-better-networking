# Architecture

All Java code is under `com.ourcreationstudios.create_better_networking`. This is a NeoForge 1.21.1 mod targeting
Java 21 and Create 6.0.x. The pinned development versions are in `gradle.properties` and
`gradle/mod-dependencies.json`.

## Main classes

| Class or package | Responsibility |
| --- | --- |
| `CreateBetterNetworking` | Registers block interaction listeners and the common config |
| `CreateBetterNetworkingClient` | Registers the client config screen |
| `ClipboardFrequencyInteraction` | Clipboard selection, named UUIDs, copy/paste, permissions and mining protection |
| `NetworkFrequencySupport` | Discovers block/item adapters, synchronizes blocks and invalidates logistics summaries |
| `FrequencyNamesData` | Stores readable network names in overworld saved data |
| `FrequencyHighlightServerEvents` | Collects nearby loaded members and sends authoritative snapshots |
| `FrequencyHighlightNetworking` | Registers the three custom payloads |
| `ClipboardFrequencyClientEvents` | Handles local selection, client fallback scans, reset input, outlines, tooltips and HUD feedback |
| `Config` | Defines `showNetworkIdInHud` |
| `api/` | Public frequency interfaces and NeoForge capability types |

## Mixins

`src/main/resources/create_better_networking.mixins.json` registers three common mixins and one client mixin:

| Mixin | Target and purpose |
| --- | --- |
| `FactoryPanelBlockItemMixin` | Allows untuned gauge placement on the server and on clients with negotiated server support |
| `FactoryPanelBlockEntityMixin` | Represents new unconnected panels with the all-zero UUID when untuned placement is supported |
| `LogisticallyLinkedBehaviourMixin` | Implements the frequency holder API and maintains live/global membership across network changes |
| `LogisticallyLinkedClientHandlerMixin` | Suppresses Create's link cache and panel highlights while Better Networking draws its snapshot |

The untuned-placement injection is optional (`require = 0`) so addons that replace the placement method can remove
that gate. The remaining injections retain the configuration's default required match count. Keep the client
handler in the JSON's `client` array so dedicated servers do not load client classes.

## Data and protocol contracts

- Mod and capability namespace: `create_better_networking`.
- Clipboard custom-data keys: `create_better_networking_frequency` and `create_better_networking_frequency_name`.
- Named UUID input: `create_better_networking:frequency:` followed by the normalized name, encoded as UTF-8.
- Saved-data ID: `create_better_networking_frequency_names`, stored through the overworld's data storage.
- Block and item capability path: `network_frequency` in the mod namespace.
- Payload paths: `frequency_highlights`, `clipboard_feedback` and `reset_clipboard`, registered with protocol `1`.

The highlight payload contains a dimension ID, network UUID, block positions and gauge panel slots. Its legacy
`tickers` list remains in the wire format even though current collection uses the general links list. Payload
handlers enqueue work on the game thread. All three payloads are optional, and both peers check their negotiated
channels before enabling tools or sending payloads. Server code owns network changes; the client renders the
resulting snapshots and feedback. Server interaction handlers leave players without the client mod alone.

Clipboard block clicks run at `LOWEST` priority without receiving canceled events. Open Parties and Claims
checks the same event at `HIGHEST`; FTB Chunks checks it through Architectury at `HIGH`. A denied claim click
therefore prevents both copying and pasting. The handler also rejects canceled events when called directly.
Create's permissions for both the existing and requested networks, plus addon holder permissions, are checked
before updating network membership, saved names or clipboard data. Claims protect the clicked block; use Create's
network lock to restrict access through other, unclaimed network members. Local client highlights are read-only.

Without negotiated server support, clipboard attacks are canceled at the client input event before digging packets
can be sent. Shift-left-click stores a UUID in client session memory, without changing item components. A local
scan of received chunks supplies highlight snapshots every 20 ticks and after selection. It reads synchronized
network fields rather than Create's expiring link cache. Server-backed mode continues to use only server snapshots,
even when a snapshot is missing or stale. Snapshot counts reset on expiry or network changes.

Login and logout clear local selection, snapshots, HUD feedback and the cached client support flag. That flag is
used only on the logical client for input and placement prediction; the logical server checks each player's
connection independently. The local selection survives dimension changes within the same connection, while
snapshots are tied to the exact client level instance.

## Build and resources

The Gradle build compiles production source, copies runtime resources, expands
`src/main/templates/META-INF/neoforge.mods.toml`, and creates only the installable mod JAR. Compile dependencies are
external and are not bundled.

All 137 locale files and existing artwork remain under `src/main/resources`. Their bytes are preserved by
`.gitattributes`.
