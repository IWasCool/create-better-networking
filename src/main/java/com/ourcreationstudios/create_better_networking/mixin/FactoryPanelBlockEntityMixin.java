package com.ourcreationstudios.create_better_networking.mixin;

import java.util.UUID;

import com.ourcreationstudios.create_better_networking.ClipboardFrequencyInteraction;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = FactoryPanelBlockEntity.class, remap = false)
abstract class FactoryPanelBlockEntityMixin {
    @ModifyVariable(method = "addPanel", at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private UUID createbetternetworking$markUnconnectedPanel(UUID network) {
        return network == null ? ClipboardFrequencyInteraction.UNCONNECTED_FREQUENCY : network;
    }
}
