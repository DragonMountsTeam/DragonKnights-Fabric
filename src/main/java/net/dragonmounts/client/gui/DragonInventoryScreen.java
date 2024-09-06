package net.dragonmounts.client.gui;

import net.dragonmounts.client.ClientDragonEntity;
import net.dragonmounts.inventory.DragonInventoryHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static net.dragonmounts.DragonMounts.makeId;

/**
 * @see net.minecraft.client.gui.screens.inventory.HorseInventoryScreen
 */
public class DragonInventoryScreen extends EffectRenderingInventoryScreen<DragonInventoryHandler> {
    private static final ResourceLocation TEXTURE_LOCATION = makeId("textures/gui/dragon.png");

    public DragonInventoryScreen(DragonInventoryHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageHeight = 224;
        this.inventoryLabelY = 131;//224 - 94 + 1
    }

    public void render(GuiGraphics graphics, int x, int y, float ticks) {
        super.render(graphics, x, y, ticks);
        this.renderTooltip(graphics, x, y);
    }

    protected void renderBg(GuiGraphics graphics, float ticks, int x, int y) {
        int left = this.leftPos, top = this.topPos;
        graphics.blit(TEXTURE_LOCATION, left, top, 0, 0, this.imageWidth, this.imageHeight);
        final ClientDragonEntity dragon = (ClientDragonEntity) this.menu.dragon;
        if (dragon.hasChest()) {
            graphics.blit(TEXTURE_LOCATION, left + 7, top + 75, 7, 141, 162, 54);
        }
        dragon.renderCrystalBeams = false;
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, left + 60, top + 62, 5, left - x + 60, top - y + 13, 0.25F, x, y, dragon);
        dragon.renderCrystalBeams = true;
    }
}
