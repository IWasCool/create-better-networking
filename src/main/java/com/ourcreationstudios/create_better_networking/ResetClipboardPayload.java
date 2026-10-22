package com.ourcreationstudios.create_better_networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ResetClipboardPayload() implements CustomPacketPayload {
    public static final Type<ResetClipboardPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateBetterNetworking.MODID, "reset_clipboard"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResetClipboardPayload> STREAM_CODEC =
            StreamCodec.unit(new ResetClipboardPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
