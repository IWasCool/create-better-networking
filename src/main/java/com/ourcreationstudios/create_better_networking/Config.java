package com.ourcreationstudios.create_better_networking;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_NETWORK_ID_IN_HUD = BUILDER
            .comment("Whether to show the network ID in the clipboard highlight HUD when the network has no name")
            .define("showNetworkIdInHud", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {}
}
