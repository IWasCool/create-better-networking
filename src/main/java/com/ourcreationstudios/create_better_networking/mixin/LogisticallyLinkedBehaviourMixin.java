package com.ourcreationstudios.create_better_networking.mixin;

import java.util.Objects;
import java.util.UUID;

import com.ourcreationstudios.create_better_networking.api.NetworkFrequencyHolder;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Applies the integration to Create links and every addon subclass. */
@Mixin(value = LogisticallyLinkedBehaviour.class, remap = false)
abstract class LogisticallyLinkedBehaviourMixin implements NetworkFrequencyHolder {
    @Shadow public UUID freqId;
    @Shadow private boolean global;
    @Shadow private boolean loadedGlobally;
    @Shadow private boolean addedGlobally;

    @Override
    public UUID getNetworkFrequency() {
        return freqId;
    }

    @Override
    public boolean mayInteractWithNetwork(Player player) {
        return ((LogisticallyLinkedBehaviour) (Object) this).mayInteract(player);
    }

    @Override
    public void setNetworkFrequency(UUID frequency) {
        Objects.requireNonNull(frequency, "frequency");
        if (frequency.equals(freqId))
            return;

        var link = (LogisticallyLinkedBehaviour) (Object) this;
        if (freqId != null) {
            LogisticallyLinkedBehaviour.remove(link);
            if (global)
                Create.LOGISTICS.linkRemoved(freqId, GlobalPos.of(link.getWorld().dimension(), link.getPos()));
        }
        freqId = frequency;
        if (global) {
            loadedGlobally = false;
            addedGlobally = false;
            // Dispatch through the addon subclass so its registration overrides run too.
            link.initialize();
        }
        LogisticallyLinkedBehaviour.keepAlive(link);
    }
}
