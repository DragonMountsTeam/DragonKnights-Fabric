package net.dragonmounts.inventory;

import net.dragonmounts.init.DMScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @see net.minecraft.world.inventory.ShulkerBoxMenu
 */
public class DragonCoreHandler extends AbstractContainerMenu {
    public final Container container;

    public DragonCoreHandler(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, pos != null && inventory.player.level().getBlockEntity(pos) instanceof Container value ? value : new SimpleContainer(1));
    }

    public DragonCoreHandler(int id, Inventory playerInventory, Container container) {
        super(DMScreenHandlers.DRAGON_CORE, id);
        (this.container = container).startOpen(playerInventory.player);
        this.addSlot(new Slot(container, 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        var slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            var stack = slot.getItem();
            var copy = stack.copy();
            if (index == 0 && !this.moveItemStackTo(stack, 1, this.slots.size(), true)) return ItemStack.EMPTY;
            if (index <= 27 && !this.moveItemStackTo(stack, 28, this.slots.size(), false)) return ItemStack.EMPTY;
            if (!this.moveItemStackTo(stack, 1, 28, false)) return ItemStack.EMPTY;
            if (copy.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            return copy;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}