package net.dragonmounts.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.dragonmounts.inventory.DragonCoreScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.dragonmounts.DragonMounts.MOD_ID;

/**
 * @see net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen
 */
public class DragonCoreScreen extends HandledScreen<DragonCoreScreenHandler> {
    private static final Identifier TEXTURE_LOCATION = new Identifier(MOD_ID, "textures/gui/dragon_core.png");

    public DragonCoreScreen(DragonCoreScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float ticks) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, ticks);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float ticks, int x, int y) {
        //noinspection DataFlowIssue
        this.client.getTextureManager().bindTexture(TEXTURE_LOCATION);
        //noinspection deprecation
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawTexture(
                matrices,
                (this.width - this.backgroundWidth) >> 1,
                (this.height - this.backgroundHeight) >> 1,
                0,
                0,
                this.backgroundWidth,
                this.backgroundHeight
        );
    }
}
