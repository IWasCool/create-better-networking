package com.ourcreationstudios.create_better_networking;

import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnectionHandler;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

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

    private ClipboardFrequencyClientEvents() {}

    @SubscribeEvent
    public static void onAttackInput(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.isAttack() && minecraft.player != null
                && ClipboardFrequencyInteraction.isHoldingClipboard(minecraft.player)
                && minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
            // Let the block-click packet reach the server for copy/paste, but suppress
            // Minecraft's mining swing and block-hit particles, even on held clicks.
            event.setSwingHand(false);
        }
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
        if (event.getEntity().isShiftKeyDown()
                && ClipboardFrequencyInteraction.hasClipboardNetworkData(event.getEntity()))
            PacketDistributor.sendToServer(new ResetClipboardPayload());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            clearStatus();
            return;
        }

        UUID frequency = ClipboardFrequencyInteraction.getHeldNetworkFrequency(player);
        if (frequency == null) {
            clearStatus();
            return;
        }

        activeFrequency = frequency;
        Set<net.minecraft.core.BlockPos> highlightedBlocks = new HashSet<>();
        if (snapshot == null || snapshotLevel != minecraft.level || !frequency.equals(snapshot.frequency())
                || minecraft.level.getGameTime() - snapshotTick > 60)
            return;

        // Network membership comes from the server. Client-side frequency fields and
        // Create's expiring link cache may still describe the block's previous network.
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
            Outliner.getInstance()
                    .showAABB(Pair.of("create_better_networking_gauge", target),
                            FactoryPanelConnectionHandler.getBB(gauge.getBlockState(), panel.getPanelPosition())
                                    .inflate(-1.5 / 128f))
                    .lineWidth(1 / 32f)
                    .disableLineNormals()
                    .colored(AnimationTickHolder.getTicks() % 16 < 8 ? 0x708DAD : 0x90ADCD);
        }
        highlightedBlockCount = highlightedBlocks.size();
    }

    private static void highlightLinkedBlock(LocalPlayer player, net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
        highlightLinkedShape(player, blockEntity, blockEntity.getBlockState().getShape(player.level(), blockEntity.getBlockPos(),
                net.minecraft.world.phys.shapes.CollisionContext.empty()));
    }

    private static void highlightLinkedShape(LocalPlayer player, net.minecraft.world.level.block.entity.BlockEntity linked,
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
        UUID frequency = ClipboardFrequencyInteraction.getClipboardFrequency(event.getItemStack());
        String networkName = ClipboardFrequencyInteraction.getClipboardFrequencyName(event.getItemStack());
        if (frequency == null && networkName == null)
            return;
        String label = networkName == null ? shortId(frequency) : networkName;
        event.getToolTip().add(Component.translatable("create_better_networking.frequency.saved", label)
                .withStyle(ChatFormatting.GOLD));
        event.getToolTip().add(Component.translatable("create_better_networking.frequency.controls")
                .withStyle(ChatFormatting.GRAY));
    }

    @SubscribeEvent
    public static void onClipboardHud(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui)
            return;
        GuiGraphics graphics = event.getGuiGraphics();
        ItemStack mainHand = minecraft.player.getMainHandItem();
        ItemStack clipboard = ClipboardFrequencyInteraction.getClipboardFrequency(mainHand) != null
                || ClipboardFrequencyInteraction.getClipboardFrequencyName(mainHand) != null
                ? mainHand : minecraft.player.getOffhandItem();
        int center = graphics.guiWidth() / 2;
        int top = graphics.guiHeight() - 65;
        boolean showNetwork = activeFrequency != null
                && ClipboardFrequencyInteraction.getHeldNetworkFrequency(minecraft.player) != null;
        if (showNetwork) {
            String networkName = ClipboardFrequencyInteraction.getClipboardFrequencyName(clipboard);
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
}
