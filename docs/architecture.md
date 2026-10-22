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
| `ClipboardFrequencyClientEvents` | Handles reset input, outlines, clipboard tooltips and HUD feedback |
| `Config` | Defines `showNetworkIdInHud` |
| `api/` | Public frequency interfaces and NeoForge capability types |

## Mixins

`src/main/resources/create_better_networking.mixins.json` registers three common mixins and one client mixin:

| Mixin | Target and purpose |
| --- | --- |
| `FactoryPanelBlockItemMixin` | Allows Create's untuned gauge placement without bypassing the rest of placement validation |
| `FactoryPanelBlockEntityMixin` | Represents newly placed unconnected panels with the all-zero UUID |
| `LogisticallyLinkedBehaviourMixin` | Implements the frequency holder API and maintains live/global membership across network changes |
| `LogisticallyLinkedClientHandlerMixin` | Suppresses Create's cached highlights while Better Networking draws the server snapshot |

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
handlers enqueue work on the game thread. Server code owns network changes; the client renders the resulting
snapshots and feedback.

## Build and resources

The Gradle build compiles production source, copies runtime resources, expands
`src/main/templates/META-INF/neoforge.mods.toml`, and creates the mod and sources JARs. The sources JAR contains
production code and resources only. Compile dependencies are external and are not bundled.

All 137 locale files and existing artwork remain under `src/main/resources`. Their bytes are preserved by
`.gitattributes`. Tests and optional development automation are archived outside this production repository.
