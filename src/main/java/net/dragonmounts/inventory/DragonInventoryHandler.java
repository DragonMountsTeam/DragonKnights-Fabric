package net.dragonmounts.inventory;

import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.init.DMScreenHandlers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.NotNull;

import static net.dragonmounts.inventory.DragonInventory.*;

public class DragonInventoryHandler extends AbstractContainerMenu {
    protected static final int PLAYER_INVENTORY_SIZE = INVENTORY_SIZE + 27;
    protected static final int PLAYER_HOTBAR_SIZE = PLAYER_INVENTORY_SIZE + 9;
    protected final DragonInventory inventory;
    public final TameableDragonEntity dragon;

    public DragonInventoryHandler(int id, Inventory playerInventory, TameableDragonEntity dragon) {
        super(DMScreenHandlers.DRAGON_INVENTORY, id);
        DragonInventory dragonInventory = this.inventory = dragon.inventory;
        this.dragon = dragon;
        dragonInventory.startOpen(playerInventory.player);
        this.addSlot(new ArmorSlot(SLOT_ARMOR_INDEX, 8, 36));
        this.addSlot(new ChestSlot(SLOT_CHEST_INDEX, 8, 54));
        this.addSlot(new SaddleSlot(SLOT_SADDLE_INDEX, 8, 18));
        for (int i = 0; i < 3; ++i) {
            for (int j = 3; j < 12; ++j) {
                this.addSlot(new InventorySlot(j + i * 9, j * 18 - 46, 76 + i * 18));
            }
        }
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 142 + i * 18));
            }
        }
        for (int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 200));
        }
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        var slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem(), copy = stack.copy();
            if (index < INVENTORY_SIZE) {
                if (!this.moveItemStackTo(stack, INVENTORY_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(2).mayPlace(stack) && !this.getSlot(2).hasItem()) {
                if (!this.moveItemStackTo(stack, 2, 3, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(1).mayPlace(stack) && !this.getSlot(1).hasItem()) {
                if (!this.moveItemStackTo(stack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(0).mayPlace(stack) && !this.getSlot(0).hasItem()) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.dragon.hasChest() || !this.moveItemStackTo(stack, 3, INVENTORY_SIZE, false)) {
                if (index >= PLAYER_INVENTORY_SIZE) {
                    this.moveItemStackTo(stack, INVENTORY_SIZE, PLAYER_INVENTORY_SIZE, false);
                } else {
                    this.moveItemStackTo(stack, PLAYER_INVENTORY_SIZE, PLAYER_HOTBAR_SIZE, false);
                }
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
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
        this.inventory.stopOpen(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    public class SaddleSlot extends Slot {
        public SaddleSlot(int slot, int x, int y) {
            super(DragonInventoryHandler.this.inventory, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return DragonInventoryHandler.this.inventory.isSaddle(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    public class ArmorSlot extends Slot {
        public ArmorSlot(int slot, int x, int y) {
            super(DragonInventoryHandler.this.inventory, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return DragonInventoryHandler.this.dragon.isBodyArmorItem(stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            ItemStack stack = this.getItem();
            return (stack.isEmpty() || player.isCreative() || !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) && super.mayPickup(player);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

    }

    public class ChestSlot extends Slot {
        public ChestSlot(int slot, int x, int y) {
            super(DragonInventoryHandler.this.inventory, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return DragonInventoryHandler.this.inventory.isChest(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    public class InventorySlot extends Slot {
        public InventorySlot(int slot, int x, int y) {
            super(DragonInventoryHandler.this.inventory, slot, x, y);
        }

        @Override
        public boolean isActive() {
            return DragonInventoryHandler.this.dragon.hasChest();
        }
    }
}
