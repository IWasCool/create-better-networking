package com.ourcreationstudios.create_better_networking;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = CreateBetterNetworking.MODID, dist = Dist.CLIENT)
public class CreateBetterNetworkingClient {
    public CreateBetterNetworkingClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
