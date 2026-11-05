package com.ourcreationstudios.create_better_networking;

import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.ourcreationstudios.create_better_networking.api.NetworkFrequencyHolder;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClipboardFrequencyInteraction {
    private static final String FREQUENCY_KEY = "create_better_networking_frequency";
    private static final String FREQUENCY_NAME_KEY = "create_better_networking_frequency_name";
    public static final UUID UNCONNECTED_FREQUENCY = new UUID(0, 0);

    private ClipboardFrequencyInteraction() {}

    public static boolean isHoldingClipboard(Player player) {
        return !getClipboard(player).isEmpty();
    }

    static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && !FrequencyHighlightNetworking.hasClientSupport(player))
            return;
        if (isHoldingClipboard(event.getPlayer()))
            event.setCanceled(true);
    }

    public static UUID getHeldClipboardFrequency(Player player) {
        ItemStack clipboard = getClipboard(player);
        return getClipboardFrequency(clipboard);
    }

    public static boolean hasClipboardNetworkData(Player player) {
        ItemStack clipboard = getClipboard(player);
        return getClipboardFrequency(clipboard) != null || getClipboardFrequencyName(clipboard) != null;
    }

    public static UUID getClipboardFrequency(ItemStack stack) {
        if (!stack.is(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:clipboard"))))
            return null;
        return CompoundData.getFrequency(stack);
    }

    /** A dedicated control line selects a named frequency; ordinary quoted text is ignored. */
    public static String getManualFrequencyName(ItemStack stack) {
        if (getClipboardFrequency(stack) == null && !isClipboard(stack))
            return null;
        for (var page : ClipboardEntry.readAll(stack)) {
            for (ClipboardEntry entry : page) {
                String text = entry.text.getString().trim();
                if (!text.regionMatches(true, 0, "#network:", 0, 9))
                    continue;
                String name = text.substring(9).trim();
                if (!name.isEmpty())
                    return name.substring(0, Math.min(name.length(), 64));
            }
        }
        return null;
    }

    public static String getClipboardFrequencyName(ItemStack stack) {
        String manualName = getManualFrequencyName(stack);
        if (manualName != null)
            return manualName;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null)
            return null;
        String name = data.copyTag().getString(FREQUENCY_NAME_KEY).trim();
        if (!name.isEmpty())
            return name;
        return name.isEmpty() ? null : name;
    }

    static boolean isClipboard(ItemStack stack) {
        return stack.is(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:clipboard")));
    }

    static UUID frequencyForName(String name) {
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return UUID.nameUUIDFromBytes((CreateBetterNetworking.MODID + ":frequency:" + normalized)
                .getBytes(StandardCharsets.UTF_8));
    }

    public static UUID getHeldNetworkFrequency(Player player) {
        ItemStack clipboard = getClipboard(player);
        String manualName = getManualFrequencyName(clipboard);
        if (manualName != null)
            return frequencyForName(manualName);
        UUID frequency = getClipboardFrequency(clipboard);
        if (frequency != null)
            return frequency;
        for (ItemStack stack : new ItemStack[] { player.getMainHandItem(), player.getOffhandItem() }) {
            UUID itemFrequency = NetworkFrequencySupport.getItemFrequency(stack);
            if (itemFrequency != null)
                return itemFrequency;
        }
        return null;
    }

    static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.isCanceled())
            return;
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer && !FrequencyHighlightNetworking.hasClientSupport(serverPlayer))
            return;
        ItemStack clipboard = getClipboard(player);
        if (clipboard.isEmpty())
            return;

        // Cancel mining on both sides, including creative prediction and held clicks,
        // before checking whether this block supports a network interaction.
        event.setCanceled(true);
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START || event.getLevel().isClientSide())
            return;

        var level = event.getLevel();
        var pos = event.getPos();
        var state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        FactoryPanelBehaviour panel = null;
        if (blockEntity instanceof FactoryPanelBlockEntity gaugeBE) {
            PanelSlot slot = clickedPanelSlot(player, pos, state);
            if (slot == null)
                return;
            panel = gaugeBE.panels.get(slot);
            if (panel == null)
                return;
        }
        NetworkFrequencyHolder holder = panel == null ? NetworkFrequencySupport.find(blockEntity) : null;
        if (panel == null && holder == null)
            return;

        UUID clickedFrequency = panel != null
                ? panel.isActive() && !UNCONNECTED_FREQUENCY.equals(panel.network) ? panel.network : null
                : holder.getNetworkFrequency();
        if (holder != null && !holder.mayInteractWithNetwork(player)) {
            show(player, "create_better_networking.frequency.protected", ChatFormatting.RED);
            return;
        }
        if (player.isShiftKeyDown()) {
            if (clickedFrequency == null)
                return;
            if (!mayInteract(clickedFrequency, player)) {
                show(player, "create_better_networking.frequency.protected", ChatFormatting.RED);
                return;
            }
            String networkName = FrequencyNamesData.getName((ServerPlayer) player, clickedFrequency);
            CompoundData.setFrequency(clipboard, clickedFrequency, networkName);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            FrequencyHighlightServerEvents.refresh((ServerPlayer) player);
            sendFeedback((ServerPlayer) player, "create_better_networking.frequency.copied",
                    networkName == null ? clickedFrequency.toString().substring(0, 8) : networkName,
                    ChatFormatting.GREEN.getColor());
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.2f);
            return;
        }

        String requestedName = getManualFrequencyName(clipboard);
        UUID copiedFrequency = requestedName == null ? CompoundData.getFrequency(clipboard)
                : frequencyForName(requestedName);
        if (copiedFrequency == null) {
            show(player, "create_better_networking.frequency.empty", ChatFormatting.RED);
            return;
        }
        if (!mayInteract(copiedFrequency, player)
                || clickedFrequency != null && !mayInteract(clickedFrequency, player)) {
            show(player, "create_better_networking.frequency.protected", ChatFormatting.RED);
            return;
        }

        if (requestedName != null) {
            CompoundData.setFrequency(clipboard, copiedFrequency, requestedName);
            FrequencyNamesData.setName((ServerPlayer) player, copiedFrequency, requestedName);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }

        if (copiedFrequency.equals(clickedFrequency)) {
            NetworkFrequencySupport.sync(blockEntity);
            FrequencyHighlightServerEvents.refresh((ServerPlayer) player);
            show(player, "create_better_networking.frequency.same", ChatFormatting.GRAY);
            return;
        }

        if (panel != null) {
            panel.setNetwork(copiedFrequency);
            panel.panelBE().redraw = true;
        } else {
            holder.setNetworkFrequency(copiedFrequency);
        }
        NetworkFrequencySupport.sync(blockEntity);
        NetworkFrequencySupport.invalidateSummaries(clickedFrequency, copiedFrequency);

        FrequencyHighlightServerEvents.refresh((ServerPlayer) player);
        String displayName = FrequencyNamesData.getName((ServerPlayer) player, copiedFrequency);
        sendFeedback((ServerPlayer) player, "create_better_networking.frequency.pasted",
                displayName == null ? copiedFrequency.toString().substring(0, 8) : displayName,
                ChatFormatting.GREEN.getColor());
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
    }

    public static void resetClipboardFromRequest(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || !FrequencyHighlightNetworking.hasClientSupport(serverPlayer))
            return;
        if (!player.isShiftKeyDown())
            return;
        ItemStack clipboard = getClipboard(player);
        if (clipboard.isEmpty() || (CompoundData.getFrequency(clipboard) == null
                && getClipboardFrequencyName(clipboard) == null))
            return;

        CompoundData.clearFrequency(clipboard);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        sendFeedback(serverPlayer, "create_better_networking.frequency.cleared", "", ChatFormatting.GREEN.getColor());
    }

    private static PanelSlot clickedPanelSlot(Player player, net.minecraft.core.BlockPos pos,
                                               net.minecraft.world.level.block.state.BlockState state) {
        var hit = player.pick(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1, 1, false);
        if (!(hit instanceof BlockHitResult blockHit) || !blockHit.getBlockPos().equals(pos))
            return null;
        return FactoryPanelBlock.getTargetedSlot(pos, state, blockHit.getLocation());
    }

    static ItemStack getClipboard(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.is(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:clipboard"))))
            return main;
        ItemStack off = player.getOffhandItem();
        return off.is(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:clipboard"))) ? off : ItemStack.EMPTY;
    }

    private static boolean mayInteract(UUID frequency, Player player) {
        return Create.LOGISTICS.mayInteract(frequency, player);
    }

    private static void show(Player player, String translationKey, ChatFormatting color) {
        if (player instanceof ServerPlayer serverPlayer)
            sendFeedback(serverPlayer, translationKey, "", color.getColor());
    }

    private static void sendFeedback(ServerPlayer player, String translationKey, String argument, int color) {
        if (FrequencyHighlightNetworking.hasClientSupport(player))
            PacketDistributor.sendToPlayer(player, new ClipboardFeedbackPayload(translationKey, argument, color));
    }

    private static final class CompoundData {
        private CompoundData() {}

        static void setFrequency(ItemStack clipboard, UUID frequency) {
            setFrequency(clipboard, frequency, null);
        }

        static void setFrequency(ItemStack clipboard, UUID frequency, String name) {
            CustomData current = clipboard.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            var tag = current.copyTag();
            tag.putUUID(FREQUENCY_KEY, frequency);
            if (name == null || name.isBlank())
                tag.remove(FREQUENCY_NAME_KEY);
            else
                tag.putString(FREQUENCY_NAME_KEY, name);
            clipboard.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            clipboard.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        static UUID getFrequency(ItemStack clipboard) {
            CustomData data = clipboard.get(DataComponents.CUSTOM_DATA);
            if (data == null)
                return null;
            var tag = data.copyTag();
            return tag.hasUUID(FREQUENCY_KEY) ? tag.getUUID(FREQUENCY_KEY) : null;
        }

        static void clearFrequency(ItemStack clipboard) {
            CustomData current = clipboard.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            var tag = current.copyTag();
            tag.remove(FREQUENCY_KEY);
            tag.remove(FREQUENCY_NAME_KEY);
            if (tag.isEmpty())
                clipboard.remove(DataComponents.CUSTOM_DATA);
            else
                clipboard.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            clipboard.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
        }
    }
}
