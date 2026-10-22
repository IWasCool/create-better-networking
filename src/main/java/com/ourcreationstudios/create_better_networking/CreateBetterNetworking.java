package com.ourcreationstudios.create_better_networking;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CreateBetterNetworking.MODID)
public class CreateBetterNetworking {
    public static final String MODID = "create_better_networking";

    public CreateBetterNetworking(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.addListener(ClipboardFrequencyInteraction::onLeftClickBlock);
        NeoForge.EVENT_BUS.addListener(ClipboardFrequencyInteraction::onBlockBreak);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
