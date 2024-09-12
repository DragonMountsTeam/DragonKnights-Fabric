package net.dragonmounts.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.dragonmounts.client.ClientDragonEntity;
import net.dragonmounts.client.DragonRendererContext;
import net.dragonmounts.inventory.DragonInventoryScreenHandler;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.dragonmounts.DragonMounts.makeId;

/**
 * @see net.minecraft.client.gui.screen.ingame.HorseScreen
 */
public class DragonInventoryScreen extends AbstractInventoryScreen<DragonInventoryScreenHandler> {
    private static final Identifier TEXTURE_LOCATION = makeId("textures/gui/dragon.png");

    public DragonInventoryScreen(DragonInventoryScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.passEvents = false;
        this.backgroundHeight = 224;
        this.playerInventoryTitleY = 131;//224 - 94 + 1
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float ticks) {
        this.renderBackground(matrices);
        super.render(matrices, x, y, ticks);
        this.drawMouseoverTooltip(matrices, x, y);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float ticks, int x, int y) {
        //noinspection DataFlowIssue
        this.client.getTextureManager().bindTexture(TEXTURE_LOCATION);
        //noinspection deprecation
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int left = this.x, top = this.y;
        this.drawTexture(matrices, left, top, 0, 0, this.backgroundWidth, this.backgroundHeight);
        final ClientDragonEntity dragon = (ClientDragonEntity) this.handler.dragon;
        if (dragon.hasChest()) {
            this.drawTexture(matrices, left + 7, top + 75, 7, 141, 162, 54);
        }
        final DragonRendererContext context = dragon.context;
        context.isInGUI = true;
        InventoryScreen.drawEntity(left + 60, top + 62, 5, left - x + 60F, top - y + 13F, dragon);
        context.isInGUI = false;
    }
}
