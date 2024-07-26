package net.dragonmounts.inventory;

import net.dragonmounts.init.DMScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import static net.dragonmounts.util.BlockEntityUtil.getInventory;

/**
 * @see net.minecraft.screen.ShulkerBoxScreenHandler
 */
public class DragonCoreScreenHandler extends ScreenHandler {
    public final Inventory inventory;

    public DragonCoreScreenHandler(int id, PlayerInventory playerInventory, PacketByteBuf data) {
        this(id, playerInventory, getInventory(playerInventory.player, data, 1));
    }

    public DragonCoreScreenHandler(int id, PlayerInventory playerInventory, Inventory inventory) {
        super(DMScreenHandlers.DRAGON_CORE, id);
        (this.inventory = inventory).onOpen(playerInventory.player);
        this.addSlot(new LimitedSlot.Reject(inventory, 0, 80, 35));
        for (int i = 0; i < 3; ++i) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + i * 9 + 9, 8 + k * 18, 84 + i * 18));
            }
        }
        for (int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
        }
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack stack = slot.getStack(), copy = stack.copy();
            if (index == 0 && !this.insertItem(stack, 1, this.slots.size(), true)) return ItemStack.EMPTY;
            if (index <= 27 && !this.insertItem(stack, 28, this.slots.size(), false)) return ItemStack.EMPTY;
            if (!this.insertItem(stack, 1, 28, false)) return ItemStack.EMPTY;
            if (copy.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
            return copy;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.inventory.onClose(player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }
}