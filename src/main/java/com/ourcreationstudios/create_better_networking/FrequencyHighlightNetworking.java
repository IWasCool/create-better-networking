package com.ourcreationstudios.create_better_networking;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = CreateBetterNetworking.MODID)
public final class FrequencyHighlightNetworking {
    private FrequencyHighlightNetworking() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(FrequencyHighlightPayload.TYPE, FrequencyHighlightPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClipboardFrequencyClientEvents.receiveSnapshot(payload)));
        registrar.playToClient(ClipboardFeedbackPayload.TYPE, ClipboardFeedbackPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClipboardFrequencyClientEvents.receiveFeedback(payload)));
        registrar.playToServer(ResetClipboardPayload.TYPE, ResetClipboardPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        ClipboardFrequencyInteraction.resetClipboardFromRequest(context.player())));
    }
}
