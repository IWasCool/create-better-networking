package com.ourcreationstudios.create_better_networking;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = CreateBetterNetworking.MODID)
public final class FrequencyHighlightNetworking {
    // Client connection state only; logical-server code checks each player's negotiated channels instead.
    private static boolean serverSupport;

    private FrequencyHighlightNetworking() {}

    public static boolean hasServerSupport() {
        return serverSupport;
    }

    static void setServerSupport(boolean supported) {
        serverSupport = supported;
    }

    public static boolean supportsConnection(ICommonPacketListener listener) {
        return listener != null && listener.hasChannel(FrequencyHighlightPayload.TYPE)
                && listener.hasChannel(ClipboardFeedbackPayload.TYPE) && listener.hasChannel(ResetClipboardPayload.TYPE);
    }

    public static boolean hasClientSupport(ServerPlayer player) {
        return supportsConnection(player.connection);
    }

    public static boolean allowsUntunedPlacement(Level level) {
        return level != null && (!level.isClientSide() || hasServerSupport());
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1").optional();
        registrar.playToClient(FrequencyHighlightPayload.TYPE, FrequencyHighlightPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClipboardFrequencyClientEvents.receiveSnapshot(payload)));
        registrar.playToClient(ClipboardFeedbackPayload.TYPE, ClipboardFeedbackPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClipboardFrequencyClientEvents.receiveFeedback(payload)));
        registrar.playToServer(ResetClipboardPayload.TYPE, ResetClipboardPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        ClipboardFrequencyInteraction.resetClipboardFromRequest(context.player())));
    }
}
