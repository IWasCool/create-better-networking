package com.ourcreationstudios.create_better_networking.mixin;

import java.util.UUID;

import com.ourcreationstudios.create_better_networking.ClipboardFrequencyClientEvents;
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
    private static void createbetternetworking$useClipboardHighlights(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;
        UUID frequency = ClipboardFrequencyClientEvents.getHeldNetworkFrequency();
        if (frequency == null)
            return;
        previouslyHeldFrequency = null;
        // The game-bus handler draws either a server snapshot or locally synchronized members.
        // Disable Create's link cache and panel outlines so there is only one highlight source.
        ci.cancel();
    }
}
