package com.ourcreationstudios.create_better_networking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnectionHandler;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;

@EventBusSubscriber(modid = CreateBetterNetworking.MODID, value = Dist.CLIENT)
public final class ClipboardFrequencyClientEvents {
    private static UUID activeFrequency;
    private static int highlightedBlockCount;
    private static FrequencyHighlightPayload snapshot;
    private static ClientLevel snapshotLevel;
    private static long snapshotTick;
    private static Component clickFeedback;
    private static int clickFeedbackColor;
    private static long clickFeedbackUntil;
    private static UUID localFrequency;
    private static boolean localSelectionChanged;
    private static boolean localAttackHandled;

    private ClipboardFrequencyClientEvents() {}

    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        clearConnectionState();
        FrequencyHighlightNetworking.setServerSupport(
                FrequencyHighlightNetworking.supportsConnection(event.getPlayer().connection));
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clearConnectionState();
    }

    public static UUID getHeldNetworkFrequency() {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return null;
        if (FrequencyHighlightNetworking.hasServerSupport())
            return ClipboardFrequencyInteraction.getHeldNetworkFrequency(player);

        ItemStack clipboard = ClipboardFrequencyInteraction.getClipboard(player);
        String manualName = ClipboardFrequencyInteraction.getManualFrequencyName(clipboard);
        if (manualName != null)
            return ClipboardFrequencyInteraction.frequencyForName(manualName);
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

    private static UUID getClipboardFrequency(ItemStack stack) {
        if (!FrequencyHighlightNetworking.hasServerSupport() && localSelectionChanged
                && ClipboardFrequencyInteraction.isClipboard(stack))
            return localFrequency;
        return ClipboardFrequencyInteraction.getClipboardFrequency(stack);
    }

    private static String getClipboardFrequencyName(ItemStack stack) {
        String manualName = ClipboardFrequencyInteraction.getManualFrequencyName(stack);
        if (manualName != null)
            return manualName;
        if (!FrequencyHighlightNetworking.hasServerSupport() && localSelectionChanged)
            return null;
        return ClipboardFrequencyInteraction.getClipboardFrequencyName(stack);
    }

    @SubscribeEvent
    public static void onAttackInput(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.isAttack() && minecraft.player != null
                && ClipboardFrequencyInteraction.isHoldingClipboard(minecraft.player)
                && minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
            event.setSwingHand(false);
            if (!FrequencyHighlightNetworking.hasServerSupport()) {
                // Cancel before Minecraft sends a digging packet, including creative and held attacks.
                event.setCanceled(true);
                if (!localAttackHandled) {
                    localAttackHandled = true;
                    selectLocalNetwork(minecraft.player, (BlockHitResult) minecraft.hitResult);
                }
            }
        }
    }

    private static void selectLocalNetwork(LocalPlayer player, BlockHitResult hit) {
        var blockEntity = player.level().getBlockEntity(hit.getBlockPos());
        UUID frequency;
        if (blockEntity instanceof FactoryPanelBlockEntity gauge) {
            var slot = FactoryPanelBlock.getTargetedSlot(hit.getBlockPos(), gauge.getBlockState(), hit.getLocation());
            FactoryPanelBehaviour panel = slot == null ? null : gauge.panels.get(slot);
            if (panel == null || !panel.isActive())
                return;
            frequency = panel.network;
        } else {
            var holder = NetworkFrequencySupport.find(blockEntity);
            if (holder == null)
                return;
            frequency = holder.getNetworkFrequency();
        }
        if (!player.isShiftKeyDown()) {
            receiveFeedback(new ClipboardFeedbackPayload("create_better_networking.frequency.server_required", "",
                    ChatFormatting.RED.getColor()));
            return;
        }
        if (frequency == null || ClipboardFrequencyInteraction.UNCONNECTED_FREQUENCY.equals(frequency))
            return;

        // Keep the selection out of ItemStack components: the server still owns the clipboard item.
        localFrequency = frequency;
        localSelectionChanged = true;
        clearSnapshot();
        receiveFeedback(new ClipboardFeedbackPayload("create_better_networking.frequency.copied", shortId(frequency),
                ChatFormatting.GREEN.getColor()));
    }

    public static void receiveSnapshot(FrequencyHighlightPayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().location().equals(payload.dimension()))
            return;
        snapshot = payload;
        snapshotLevel = level;
        snapshotTick = level.getGameTime();
    }

    public static void receiveFeedback(ClipboardFeedbackPayload payload) {
        clickFeedback = Component.translatable(payload.translationKey(), payload.argument());
        clickFeedbackColor = payload.color();
        clickFeedbackUntil = System.currentTimeMillis() + 1800;
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (!event.getEntity().isShiftKeyDown()
                || !ClipboardFrequencyInteraction.isHoldingClipboard(event.getEntity()))
            return;
        if (FrequencyHighlightNetworking.hasServerSupport()) {
            if (ClipboardFrequencyInteraction.hasClipboardNetworkData(event.getEntity()))
                PacketDistributor.sendToServer(new ResetClipboardPayload());
        } else {
            ItemStack clipboard = ClipboardFrequencyInteraction.getClipboard(event.getEntity());
            if (getClipboardFrequency(clipboard) == null && getClipboardFrequencyName(clipboard) == null)
                return;
            localFrequency = null;
            localSelectionChanged = true;
            clearSnapshot();
            clearStatus();
            receiveFeedback(new ClipboardFeedbackPayload("create_better_networking.frequency.cleared", "",
                    ChatFormatting.GREEN.getColor()));
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            clearStatus();
            clearSnapshot();
            return;
        }

        FrequencyHighlightNetworking.setServerSupport(
                FrequencyHighlightNetworking.supportsConnection(minecraft.getConnection()));
        if (!minecraft.options.keyAttack.isDown())
            localAttackHandled = false;

        UUID frequency = getHeldNetworkFrequency();
        if (frequency == null) {
            clearStatus();
            return;
        }

        activeFrequency = frequency;
        highlightedBlockCount = 0;
        if (!FrequencyHighlightNetworking.hasServerSupport()
                && (snapshot == null || snapshotLevel != minecraft.level || !frequency.equals(snapshot.frequency())
                        || minecraft.level.getGameTime() - snapshotTick >= 20))
            receiveSnapshot(collectLocalSnapshot(player, frequency));

        Set<BlockPos> highlightedBlocks = new HashSet<>();
        if (snapshot == null || snapshotLevel != minecraft.level || !frequency.equals(snapshot.frequency())
                || minecraft.level.getGameTime() - snapshotTick > 60)
            return;

        // With server support, never fall back to potentially stale client frequency fields.
        for (var pos : snapshot.links()) {
            var blockEntity = minecraft.level.getBlockEntity(pos);
            if (blockEntity == null || !player.canInteractWithBlock(pos, 64))
                continue;
            highlightedBlocks.add(pos);
            highlightLinkedBlock(player, blockEntity);
        }
        for (var pos : snapshot.tickers()) {
            var blockEntity = minecraft.level.getBlockEntity(pos);
            if (blockEntity == null || !player.canInteractWithBlock(pos, 64))
                continue;
            highlightedBlocks.add(pos);
            highlightLinkedBlock(player, blockEntity);
        }
        for (var target : snapshot.gauges()) {
            if (!(minecraft.level.getBlockEntity(target.pos()) instanceof FactoryPanelBlockEntity gauge)
                    || !player.canInteractWithBlock(target.pos(), 64))
                continue;
            FactoryPanelBehaviour panel = gauge.panels.get(target.slot());
            if (panel == null)
                continue;
            highlightedBlocks.add(target.pos());
            highlightPanel(gauge, panel, target);
        }
        highlightedBlockCount = highlightedBlocks.size();
    }

    private static FrequencyHighlightPayload collectLocalSnapshot(LocalPlayer player, UUID frequency) {
        List<BlockPos> links = new ArrayList<>();
        List<FrequencyHighlightPayload.GaugeTarget> gauges = new ArrayList<>();
        ClientLevel level = (ClientLevel) player.level();
        ChunkPos center = new ChunkPos(player.blockPosition());
        for (int x = center.x - 4; x <= center.x + 4; x++) {
            for (int z = center.z - 4; z <= center.z + 4; z++) {
                var chunk = level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
                if (chunk == null)
                    continue;
                for (var blockEntity : chunk.getBlockEntities().values()) {
                    if (!player.canInteractWithBlock(blockEntity.getBlockPos(), 64))
                        continue;
                    if (blockEntity instanceof FactoryPanelBlockEntity gauge) {
                        for (var entry : gauge.panels.entrySet()) {
                            if (entry.getValue().isActive() && frequency.equals(entry.getValue().network))
                                gauges.add(new FrequencyHighlightPayload.GaugeTarget(gauge.getBlockPos().immutable(),
                                        entry.getKey()));
                        }
                    } else {
                        for (var holder : NetworkFrequencySupport.findAll(blockEntity)) {
                            if (frequency.equals(holder.getNetworkFrequency())) {
                                links.add(blockEntity.getBlockPos().immutable());
                                break;
                            }
                        }
                    }
                }
            }
        }
        return new FrequencyHighlightPayload(level.dimension().location(), frequency, links, List.of(), gauges);
    }

    private static void highlightPanel(FactoryPanelBlockEntity gauge, FactoryPanelBehaviour panel,
                                       FrequencyHighlightPayload.GaugeTarget target) {
        Outliner.getInstance()
                .showAABB(Pair.of("create_better_networking_gauge", target),
                        FactoryPanelConnectionHandler.getBB(gauge.getBlockState(), panel.getPanelPosition())
                                .inflate(-1.5 / 128f))
                .lineWidth(1 / 32f)
                .disableLineNormals()
                .colored(AnimationTickHolder.getTicks() % 16 < 8 ? 0x708DAD : 0x90ADCD);
    }

    private static void highlightLinkedBlock(LocalPlayer player, BlockEntity blockEntity) {
        highlightLinkedShape(player, blockEntity, blockEntity.getBlockState().getShape(player.level(),
                blockEntity.getBlockPos(), CollisionContext.empty()));
    }

    private static void highlightLinkedShape(LocalPlayer player, BlockEntity linked,
                                             VoxelShape shape) {
        for (int index = 0; index < shape.toAabbs().size(); index++) {
            AABB bounds = shape.toAabbs().get(index);
            Outliner.getInstance()
                    .showAABB(Pair.of("create_better_networking_link", Pair.of(linked.getBlockPos(), index)), bounds.inflate(-1 / 128f)
                            .move(linked.getBlockPos()), 2)
                    .lineWidth(1 / 32f)
                    .disableLineNormals()
                    .colored(AnimationTickHolder.getTicks() % 16 < 8 ? 0x708DAD : 0x90ADCD);
        }
    }

    @SubscribeEvent
    public static void onClipboardTooltip(ItemTooltipEvent event) {
        if (!ClipboardFrequencyInteraction.isClipboard(event.getItemStack()))
            return;
        UUID frequency = getClipboardFrequency(event.getItemStack());
        String networkName = getClipboardFrequencyName(event.getItemStack());
        if (frequency != null || networkName != null) {
            String label = networkName == null ? shortId(frequency) : networkName;
            event.getToolTip().add(Component.translatable("create_better_networking.frequency.saved", label)
                    .withStyle(ChatFormatting.GOLD));
        }
        String controls = FrequencyHighlightNetworking.hasServerSupport()
                ? "create_better_networking.frequency.controls" : "create_better_networking.frequency.client_controls";
        event.getToolTip().add(Component.translatable(controls)
                .withStyle(ChatFormatting.GRAY));
    }

    @SubscribeEvent
    public static void onClipboardHud(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui)
            return;
        GuiGraphics graphics = event.getGuiGraphics();
        ItemStack clipboard = ClipboardFrequencyInteraction.getClipboard(minecraft.player);
        int center = graphics.guiWidth() / 2;
        int top = graphics.guiHeight() - 65;
        boolean showNetwork = activeFrequency != null && getHeldNetworkFrequency() != null;
        if (showNetwork) {
            String networkName = getClipboardFrequencyName(clipboard);
            Component status;
            if (networkName != null) {
                status = Component.translatable("create_better_networking.frequency.highlight_status", networkName,
                        highlightedBlockCount);
            } else if (Config.SHOW_NETWORK_ID_IN_HUD.get()) {
                status = Component.translatable("create_better_networking.frequency.highlight_status",
                        shortId(activeFrequency), highlightedBlockCount);
            } else {
                status = Component.translatable("create_better_networking.frequency.highlight_status_hidden",
                        highlightedBlockCount);
            }
            int halfWidth = minecraft.font.width(status) / 2 + 4;
            graphics.fill(center - halfWidth, top - 2, center + halfWidth, top + 11, 0x99000000);
            graphics.drawCenteredString(minecraft.font, status, center, top, 0xFFD966);
        }
        if (clickFeedback != null && System.currentTimeMillis() < clickFeedbackUntil) {
            int feedbackTop = showNetwork ? top - 15 : top;
            int feedbackHalfWidth = minecraft.font.width(clickFeedback) / 2 + 4;
            graphics.fill(center - feedbackHalfWidth, feedbackTop - 2, center + feedbackHalfWidth,
                    feedbackTop + 11, 0x99000000);
            graphics.drawCenteredString(minecraft.font, clickFeedback, center, feedbackTop, clickFeedbackColor);
        } else {
            clickFeedback = null;
        }
    }

    private static String shortId(UUID frequency) {
        return frequency.toString().substring(0, 8);
    }

    private static void clearStatus() {
        activeFrequency = null;
        highlightedBlockCount = 0;
    }

    private static void clearSnapshot() {
        snapshot = null;
        snapshotLevel = null;
        snapshotTick = 0;
        highlightedBlockCount = 0;
    }

    private static void clearConnectionState() {
        FrequencyHighlightNetworking.setServerSupport(false);
        localFrequency = null;
        localSelectionChanged = false;
        localAttackHandled = false;
        clickFeedback = null;
        clickFeedbackUntil = 0;
        clearStatus();
        clearSnapshot();
    }
}
