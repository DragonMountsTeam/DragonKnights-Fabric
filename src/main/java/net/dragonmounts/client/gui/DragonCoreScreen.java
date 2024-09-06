package net.dragonmounts.client.gui;

import net.dragonmounts.inventory.DragonCoreHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static net.dragonmounts.DragonMounts.makeId;

/**
 * @see net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen
 */
public class DragonCoreScreen extends AbstractContainerScreen<DragonCoreHandler> {
    private static final ResourceLocation TEXTURE_LOCATION = makeId("textures/gui/dragon_core.png");

    public DragonCoreScreen(DragonCoreHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float ticks) {
        super.render(graphics, x, y, ticks);
        this.renderTooltip(graphics, x, y);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float ticks, int x, int y) {
        graphics.blit(TEXTURE_LOCATION, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
