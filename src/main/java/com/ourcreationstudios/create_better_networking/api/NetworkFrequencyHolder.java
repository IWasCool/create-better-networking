package com.ourcreationstudios.create_better_networking.api;

import java.util.UUID;

import net.minecraft.world.entity.player.Player;

/**
 * Optional integration for block entities or Create behaviours that store a logistics
 * network without using LogisticallyLinkedBehaviour. Standard Create links and subclasses
 * implement this interface automatically. Alternatively, expose a block capability adapter.
 */
public interface NetworkFrequencyHolder extends NetworkFrequencySource {
    /** Returns the logistics network UUID, or null when unconnected. */
    UUID getNetworkFrequency();

    /** Called on the server. Implementations must update any addon-specific network registrations. */
    void setNetworkFrequency(UUID frequency);

    /** Additional addon permissions, checked alongside Create's permissions for both networks. */
    default boolean mayInteractWithNetwork(Player player) {
        return true;
    }
}
