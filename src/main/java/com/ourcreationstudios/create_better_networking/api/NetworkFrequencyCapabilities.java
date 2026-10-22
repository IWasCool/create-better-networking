package com.ourcreationstudios.create_better_networking.api;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

/**
 * Optional adapters for addons that cannot implement the frequency interfaces directly.
 * Register providers on NeoForge's RegisterCapabilitiesEvent on the mod event bus.
 */
public final class NetworkFrequencyCapabilities {
    /** An explicit clipboard target, taking priority over automatic behaviour discovery. */
    public static final BlockCapability<NetworkFrequencyHolder, Void> BLOCK = BlockCapability.createVoid(
            ResourceLocation.fromNamespaceAndPath("create_better_networking", "network_frequency"),
            NetworkFrequencyHolder.class);

    /** A held item's logistics network, taking priority over Create's standard Freq data. */
    public static final ItemCapability<NetworkFrequencySource, Void> ITEM = ItemCapability.createVoid(
            ResourceLocation.fromNamespaceAndPath("create_better_networking", "network_frequency"),
            NetworkFrequencySource.class);

    private NetworkFrequencyCapabilities() {}
}
