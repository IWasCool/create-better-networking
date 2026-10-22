package com.ourcreationstudios.create_better_networking;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

/** Saves the readable names assigned to manually named frequencies. */
public final class FrequencyNamesData extends SavedData {
    private static final String DATA_ID = "create_better_networking_frequency_names";
    private static final Factory<FrequencyNamesData> FACTORY = new Factory<>(FrequencyNamesData::new,
            FrequencyNamesData::load);
    private final Map<UUID, String> names = new HashMap<>();

    private FrequencyNamesData() {}

    private static FrequencyNamesData load(CompoundTag tag, HolderLookup.Provider registries) {
        FrequencyNamesData data = new FrequencyNamesData();
        CompoundTag savedNames = tag.getCompound("Names");
        for (String key : savedNames.getAllKeys()) {
            try {
                data.names.put(UUID.fromString(key), savedNames.getString(key));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed entries while preserving the remaining saved names.
            }
        }
        return data;
    }

    public static String getName(ServerPlayer player, UUID frequency) {
        return get(player).names.get(frequency);
    }

    public static void setName(ServerPlayer player, UUID frequency, String name) {
        FrequencyNamesData data = get(player);
        data.names.put(frequency, name);
        data.setDirty();
    }

    private static FrequencyNamesData get(ServerPlayer player) {
        return player.serverLevel().getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_ID);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag savedNames = new CompoundTag();
        names.forEach((frequency, name) -> savedNames.putString(frequency.toString(), name));
        tag.put("Names", savedNames);
        return tag;
    }
}
