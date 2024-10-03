package net.dragonmounts.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.dragonmounts.init.DMItems;
import net.dragonmounts.item.FluteItem;
import net.dragonmounts.registry.FluteCommand;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import static net.dragonmounts.DragonMounts.makeId;

public class FluteOverlay extends FluteItem.Handler implements HudRenderCallback, ScreenEvents.BeforeInit, ClientPlayConnectionEvents.Join {
    private static boolean IS_AVAILABLE = false;
    private static int SELECTION;
    private static Slot[] SLOTS = new Slot[0];
    public static final FluteOverlay INSTANCE = new FluteOverlay();
    public static final TranslatableContents TIPS = new TranslatableContents("flute_command.tips", null, TranslatableContents.NO_ARGS);
    private static final ResourceLocation COMMAND_LOCATION = makeId("textures/gui/flute_command.png");
    private static final ResourceLocation PROGRESS_BAR_SPRITE = makeId("flute_command/progress");

    public static boolean isAvailable() {
        return IS_AVAILABLE;
    }

    private Minecraft minecraft;
    private int width;
    private int height;
    private int ticks;

    private FluteOverlay() {
        HudRenderCallback.EVENT.register(this);
        ScreenEvents.BEFORE_INIT.register(this);
        ClientPlayConnectionEvents.JOIN.register(this);
    }

    public static void lastSelection() {
        if (--SELECTION < 0) {
            SELECTION = SLOTS.length - 1;
        }
    }

    public static void nextSelection() {
        if (++SELECTION >= SLOTS.length) {
            SELECTION = 0;
        }
    }

    public static int getCommand() {
        if (SELECTION < 0 || SELECTION >= SLOTS.length) return -1;
        return SLOTS[SELECTION].command.id;
    }

    @Override
    public void start(Player player) {
        if (player.isLocalPlayer() && SLOTS.length != 0) {
            this.ticks = 0;
            IS_AVAILABLE = true;
        }
    }

    @Override
    public void tick(LivingEntity entity) {
        if (entity instanceof Player player && player.isLocalPlayer()) {
            ++this.ticks;
        }
    }

    protected void reposition() {
        var slots = SLOTS;
        int length = slots.length;
        int totalWidth = length * 31 - 5;
        int baseX = this.width / 2 - totalWidth / 2;
        int baseY = this.height / 2 - 31;
        for (int i = 0; i < length; ++i) {
            var slot = slots[i];
            slot.x = baseX + i * 31;
            slot.y = baseY;
        }
    }

    @Override
    public void onPlayReady(ClientPacketListener handler, PacketSender sender, Minecraft minecraft) {
        Slot[] slots = new Slot[FluteCommand.REGISTRY.size()];
        int index = 0;
        for (var command : FluteCommand.REGISTRY) {
            slots[index++] = new Slot(command);
        }
        SLOTS = slots;
        this.reposition();
    }

    @Override
    public void beforeInit(Minecraft minecraft, Screen screen, int width, int height) {
        this.minecraft = minecraft;
        this.width = width;
        this.height = height;
        this.reposition();
    }


    private void drawBar(GuiGraphics guiGraphics, int x, int y, float progress) {

    }

    @Override
    public void onHudRender(GuiGraphics guiGraphics, DeltaTracker tickCounter) {
        var minecraft = this.minecraft;
        if (minecraft.player == null || minecraft.player.getUseItem().getItem() != DMItems.FLUTE) {
            IS_AVAILABLE = false;
            return;
        }
        RenderSystem.enableDepthTest();
        guiGraphics.pose().pushPose();
        RenderSystem.enableBlend();
        int halfWidth = this.width >> 1;
        int halfHeight = this.height >> 1;
        guiGraphics.blit(COMMAND_LOCATION, halfWidth - 62, halfHeight - 31 - 27 - 5, 0.0F, 0.0F, 125, 80, 128, 128);
        guiGraphics.pose().popPose();
        var selected = SLOTS[SELECTION];
        guiGraphics.drawCenteredString(this.minecraft.font, selected.text, halfWidth, halfHeight - 31 - 20, -1);
        guiGraphics.drawCenteredString(this.minecraft.font, MutableComponent.create(TIPS), halfWidth, halfHeight + 5, 16777215);
        for (var slot : SLOTS) {
            slot.render(guiGraphics, selected);
        }
        int i = Mth.lerpDiscrete(Mth.clamp((this.ticks + tickCounter.getGameTimeDeltaPartialTick(false)) / FluteItem.USE_DURATION, 0, 1), 0, 119);
        if (i > 0) {
            guiGraphics.blitSprite(PROGRESS_BAR_SPRITE, 119, 5, 0, 0, halfWidth - 59, halfHeight - 60, i, 5);
        }
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }

    public static class Slot {
        private static final ResourceLocation SLOT_SPRITE = makeId("flute_command/slot");
        private static final ResourceLocation SELECTION_SPRITE = makeId("flute_command/selection");
        public final Component text;
        public final FluteCommand command;
        public int x;
        public int y;

        public Slot(FluteCommand command) {
            this.text = MutableComponent.create(command.name);
            this.command = command;
        }

        public void render(GuiGraphics guiGraphics, Slot selected) {
            guiGraphics.blitSprite(SLOT_SPRITE, this.x, this.y, 26, 26);
            guiGraphics.blitSprite(this.command.icon, this.x + 5, this.y + 5, 16, 16);
            if (this == selected) {
                guiGraphics.blitSprite(SELECTION_SPRITE, this.x, this.y, 26, 26);
            }
        }
    }
}
