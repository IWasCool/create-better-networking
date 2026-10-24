package com.ourcreationstudios.create_better_networking;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ourcreationstudios.create_better_networking.api.NetworkFrequencyCapabilities;
import com.ourcreationstudios.create_better_networking.api.NetworkFrequencyHolder;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Shared discovery for clipboard interactions and server or client highlight snapshots. */
public final class NetworkFrequencySupport {
    private NetworkFrequencySupport() {}

    public static NetworkFrequencyHolder find(BlockEntity blockEntity) {
        List<NetworkFrequencyHolder> holders = findAll(blockEntity);
        // A block with independent networks must explicitly choose a clipboard target.
        return holders.size() == 1 ? holders.getFirst() : null;
    }

    /** All discoverable networks, including blocks too ambiguous for clipboard pasting. */
    public static List<NetworkFrequencyHolder> findAll(BlockEntity blockEntity) {
        if (blockEntity == null)
            return List.of();
        if (blockEntity.getLevel() != null) {
            var holder = blockEntity.getLevel().getCapability(NetworkFrequencyCapabilities.BLOCK,
                    blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, null);
            if (holder != null)
                return List.of(holder);
        }
        if (blockEntity instanceof NetworkFrequencyHolder holder)
            return List.of(holder);
        if (!(blockEntity instanceof SmartBlockEntity smart))
            return List.of();

        List<NetworkFrequencyHolder> result = new ArrayList<>();
        for (var behaviour : smart.getAllBehaviours()) {
            if (behaviour instanceof NetworkFrequencyHolder holder)
                result.add(holder);
        }
        return List.copyOf(result);
    }

    public static UUID getItemFrequency(ItemStack stack) {
        if (stack.isEmpty())
            return null;
        var source = stack.getCapability(NetworkFrequencyCapabilities.ITEM);
        if (source != null)
            return source.getNetworkFrequency();
        // Create's data format is shared by addon block items, regardless of their base class.
        return stack.getItem() instanceof BlockItem ? LogisticallyLinkedBlockItem.networkFromStack(stack) : null;
    }

    public static void sync(BlockEntity blockEntity) {
        blockEntity.setChanged();
        if (blockEntity instanceof SyncedBlockEntity synced)
            synced.sendData();
        else if (blockEntity.getLevel() != null)
            blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(),
                    blockEntity.getBlockState(), 3);
    }

    public static void invalidateSummaries(UUID... frequencies) {
        for (UUID frequency : frequencies) {
            if (frequency == null)
                continue;
            LogisticsManager.SUMMARIES.invalidate(frequency);
            LogisticsManager.ACCURATE_SUMMARIES.invalidate(frequency);
        }
    }
}
