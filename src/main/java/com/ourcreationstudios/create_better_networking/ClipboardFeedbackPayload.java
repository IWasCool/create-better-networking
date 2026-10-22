package com.ourcreationstudios.create_better_networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClipboardFeedbackPayload(String translationKey, String argument, int color)
        implements CustomPacketPayload {
    public static final Type<ClipboardFeedbackPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateBetterNetworking.MODID, "clipboard_feedback"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClipboardFeedbackPayload> STREAM_CODEC =
            StreamCodec.of(ClipboardFeedbackPayload::write, ClipboardFeedbackPayload::read);

    private static void write(RegistryFriendlyByteBuf buffer, ClipboardFeedbackPayload payload) {
        buffer.writeUtf(payload.translationKey, 256);
        buffer.writeUtf(payload.argument, 64);
        buffer.writeInt(payload.color);
    }

    private static ClipboardFeedbackPayload read(RegistryFriendlyByteBuf buffer) {
        return new ClipboardFeedbackPayload(buffer.readUtf(256), buffer.readUtf(64), buffer.readInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
