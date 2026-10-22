package com.ourcreationstudios.create_better_networking.mixin;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockItem;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FactoryPanelBlockItem.class, remap = false)
abstract class FactoryPanelBlockItemMixin {
    // Change only the tuning gate so other mods can keep their placement hooks.
    // An addon may replace the method entirely and remove this gate.
    @ModifyExpressionValue(method = "place", at = @At(value = "INVOKE", target =
            "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBlockItem;isTuned(Lnet/minecraft/world/item/ItemStack;)Z"),
            require = 0, remap = false)
    private boolean createbetternetworking$allowUntunedPlacement(boolean tuned) {
        return true;
    }
}
