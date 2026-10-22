package com.ourcreationstudios.create_better_networking;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * A snapshot of nearby network members, determined from the server's block entities.
 * links includes all supported logistics blocks, including addons. tickers is retained
 * in the wire format for compatibility with earlier snapshots.
 */
public record FrequencyHighlightPayload(ResourceLocation dimension, UUID frequency, List<BlockPos> links,
                                        List<BlockPos> tickers,
                                        List<GaugeTarget> gauges) implements CustomPacketPayload {
    public static final Type<FrequencyHighlightPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateBetterNetworking.MODID, "frequency_highlights"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FrequencyHighlightPayload> STREAM_CODEC =
            StreamCodec.of(FrequencyHighlightPayload::write, FrequencyHighlightPayload::read);

    public FrequencyHighlightPayload {
        links = List.copyOf(links);
        tickers = List.copyOf(tickers);
        gauges = List.copyOf(gauges);
    }

    public record GaugeTarget(BlockPos pos, PanelSlot slot) {}

    private static void write(RegistryFriendlyByteBuf buffer, FrequencyHighlightPayload payload) {
        buffer.writeResourceLocation(payload.dimension());
        buffer.writeUUID(payload.frequency());
        buffer.writeCollection(payload.links(), (buf, pos) -> buf.writeBlockPos(pos));
        buffer.writeCollection(payload.tickers(), (buf, pos) -> buf.writeBlockPos(pos));
        buffer.writeCollection(payload.gauges(), (buf, gauge) -> {
            buf.writeBlockPos(gauge.pos());
            buf.writeEnum(gauge.slot());
        });
    }

    private static FrequencyHighlightPayload read(RegistryFriendlyByteBuf buffer) {
        ResourceLocation dimension = buffer.readResourceLocation();
        UUID frequency = buffer.readUUID();
        List<BlockPos> links = buffer.readCollection(ArrayList::new, buf -> buf.readBlockPos());
        List<BlockPos> tickers = buffer.readCollection(ArrayList::new, buf -> buf.readBlockPos());
        List<GaugeTarget> gauges = buffer.readCollection(ArrayList::new,
                buf -> new GaugeTarget(buf.readBlockPos(), buf.readEnum(PanelSlot.class)));
        return new FrequencyHighlightPayload(dimension, frequency, links, tickers, gauges);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
