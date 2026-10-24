# Addon compatibility

Better Networking targets UUID-based Create logistics networks. Automatic discovery covers stock links,
stock-checking blocks and addon subclasses of Create's `LogisticallyLinkedBehaviour`, including subclasses with
custom behaviour types. The shared mixin implements `NetworkFrequencyHolder` on the base behaviour.

In client-only mode, discovery can read only data the server already synchronizes. Addon frequency getters and
capability providers must work on the client to participate in local selection and highlights. Local reads do not
invoke server permission checks or setters. Actual clipboard changes still require both sides and retain all
server permission checks.

Moving a linked behaviour removes old live and global membership, assigns the new UUID, runs global links'
initialization overrides, and keeps the new link alive. Calling the subclass's initialization method preserves
addon-specific registration and ownership logic.

## Explicit block adapters

For custom storage, implement `com.ourcreationstudios.create_better_networking.api.NetworkFrequencyHolder` on a block
entity or an attached Create behaviour:

```java
public interface NetworkFrequencyHolder extends NetworkFrequencySource {
    UUID getNetworkFrequency();
    void setNetworkFrequency(UUID frequency);
    default boolean mayInteractWithNetwork(Player player) { return true; }
}
```

Return `null` when unconnected. The setter runs on the server and must maintain the addon's network registrations.
Override `mayInteractWithNetwork` for additional access rules; Create's permissions are checked alongside it for
copy/paste. Better Networking marks the block changed, synchronizes it, invalidates summaries and refreshes
highlights after a successful paste.

Compatibility code can instead register a `NetworkFrequencyCapabilities.BLOCK` provider through NeoForge's
`RegisterCapabilitiesEvent` on the mod event bus. Return a `NetworkFrequencyHolder` adapter. This also works for
ordinary Minecraft block entities without changing the addon's classes.

Discovery uses this priority:

1. A block capability provider.
2. A holder interface on the block entity.
3. Holder interfaces on all attached behaviours of a Create `SmartBlockEntity`.

An explicit adapter is authoritative, including when it returns a null frequency. Several independent discovered
behaviours can all participate in highlights. Clipboard interaction needs a single target; expose a capability
or block entity interface to select one when several behaviours exist. Factory gauges have a dedicated path
that selects the panel under the crosshair.

## Held item adapters

For custom item storage, register a `NetworkFrequencyCapabilities.ITEM` provider returning a
`NetworkFrequencySource`. The provider can return a read-only adapter such as:

```java
(stack, context) -> () -> readAddonNetwork(stack)
```

`readAddonNetwork` is your addon's UUID lookup. This works for any item type. An explicit item adapter takes
priority over Create's standard block-item `Freq` data; a null result means unconnected. Without an adapter,
any `BlockItem` using Create's standard `BLOCK_ENTITY_DATA` UUID named `Freq` can select a highlight network,
regardless of its item superclass. Clipboard selection takes priority over either held item.

Addons with custom registration or storage need an adapter. Unrelated systems such as item-pair redstone links
are outside the UUID logistics integration.
