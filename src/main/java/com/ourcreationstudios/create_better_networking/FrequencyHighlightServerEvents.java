package com.ourcreationstudios.create_better_networking;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = CreateBetterNetworking.MODID)
public final class FrequencyHighlightServerEvents {
    private FrequencyHighlightServerEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0)
            refresh(player);
    }

    public static void refresh(ServerPlayer player) {
        UUID frequency = ClipboardFrequencyInteraction.getHeldNetworkFrequency(player);
        if (frequency == null)
            return;

        List<BlockPos> links = new ArrayList<>();
        List<BlockPos> tickers = new ArrayList<>();
        List<FrequencyHighlightPayload.GaugeTarget> gauges = new ArrayList<>();
        var level = player.serverLevel();
        ChunkPos center = new ChunkPos(player.blockPosition());
        for (int x = center.x - 4; x <= center.x + 4; x++) {
            for (int z = center.z - 4; z <= center.z + 4; z++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null)
                    continue;
                for (var blockEntity : chunk.getBlockEntities().values()) {
                    if (!player.canInteractWithBlock(blockEntity.getBlockPos(), 64))
                        continue;
                    if (blockEntity instanceof FactoryPanelBlockEntity gauge) {
                        for (var entry : gauge.panels.entrySet()) {
                            if (entry.getValue().isActive() && frequency.equals(entry.getValue().network))
                                gauges.add(new FrequencyHighlightPayload.GaugeTarget(gauge.getBlockPos().immutable(),
                                        entry.getKey()));
                        }
                    } else {
                        for (var holder : NetworkFrequencySupport.findAll(blockEntity)) {
                            if (frequency.equals(holder.getNetworkFrequency()) && holder.mayInteractWithNetwork(player)) {
                                links.add(blockEntity.getBlockPos().immutable());
                                break;
                            }
                        }
                    }
                }
            }
        }
        PacketDistributor.sendToPlayer(player, new FrequencyHighlightPayload(level.dimension().location(), frequency,
                links, tickers, gauges));
    }
}
