package com.ourcreationstudios.create_better_networking.mixin;

import java.util.UUID;

import com.ourcreationstudios.create_better_networking.ClipboardFrequencyInteraction;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedClientHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LogisticallyLinkedClientHandler.class, remap = false)
abstract class LogisticallyLinkedClientHandlerMixin {
    @Shadow private static UUID previouslyHeldFrequency;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void createbetternetworking$useServerHighlights(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;
        UUID frequency = ClipboardFrequencyInteraction.getHeldNetworkFrequency(player);
        if (frequency == null)
            return;
        previouslyHeldFrequency = frequency;
        // The game-bus client handler draws the server's current membership snapshot.
        // Suppress cached outlines so an old network cannot add incorrect highlights.
        ci.cancel();
    }
}
