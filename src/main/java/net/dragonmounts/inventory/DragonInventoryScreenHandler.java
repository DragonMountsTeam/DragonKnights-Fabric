package net.dragonmounts.inventory;

import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.init.DMScreenHandlers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import static net.dragonmounts.inventory.DragonInventory.*;

public class DragonInventoryScreenHandler extends ScreenHandler {
    protected static final int PLAYER_INVENTORY_SIZE = INVENTORY_SIZE + 27;
    protected static final int PLAYER_HOTBAR_SIZE = PLAYER_INVENTORY_SIZE + 9;

    public static DragonInventoryScreenHandler fromPacket(int containerId, PlayerInventory playerInventory, PacketByteBuf buffer) {
        Entity entity = buffer == null ? null : playerInventory.player.world.getEntityById(buffer.readVarInt());
        if (entity instanceof TameableDragonEntity) {
            TameableDragonEntity dragon = (TameableDragonEntity) entity;
            return new DragonInventoryScreenHandler(containerId, playerInventory, dragon.inventory, dragon);
        }
        return null;
    }

    private final DragonInventory inventory;
    public final TameableDragonEntity dragon;

    public DragonInventoryScreenHandler(int containerId, PlayerInventory playerInventory, DragonInventory dragonInventory, TameableDragonEntity dragon) {
        super(DMScreenHandlers.DRAGON_INVENTORY, containerId);
        this.dragon = dragon;
        this.inventory = dragonInventory;
        PlayerEntity player = playerInventory.player;
        dragonInventory.onOpen(player);
        this.addSlot(new LimitedSlot.Saddle(dragonInventory, SLOT_SADDLE_INDEX, 8, 18));
        this.addSlot(new LimitedSlot.DragonArmor(dragonInventory, SLOT_ARMOR_INDEX, 8, 36));
        this.addSlot(new LimitedSlot.SingleWoodenChest(dragonInventory, SLOT_CHEST_INDEX, 8, 54));
        for (int i = 0; i < 3; ++i) {
            for (int k = 3; k < 12; ++k) {
                this.addSlot(new DragonChestSlot(dragonInventory, dragon, k + i * 9, k * 18 - 46, 76 + i * 18));
            }
        }
        for (int i = 0; i < 3; ++i) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + i * 9 + 9, 8 + k * 18, 142 + i * 18));
            }
        }
        for (int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 200));
        }
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack stack = slot.getStack(), copy = stack.copy();
            if (index < INVENTORY_SIZE) {
                if (!this.insertItem(stack, INVENTORY_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(2).canInsert(stack) && !this.getSlot(2).hasStack()) {
                if (!this.insertItem(stack, 2, 3, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(1).canInsert(stack) && !this.getSlot(1).hasStack()) {
                if (!this.insertItem(stack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(0).canInsert(stack) && !this.getSlot(0).hasStack()) {
                if (!this.insertItem(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.dragon.hasChest() || !this.insertItem(stack, 3, INVENTORY_SIZE, false)) {
                if (index >= PLAYER_INVENTORY_SIZE) {
                    this.insertItem(stack, INVENTORY_SIZE, PLAYER_INVENTORY_SIZE, false);
                } else {
                    this.insertItem(stack, PLAYER_INVENTORY_SIZE, PLAYER_HOTBAR_SIZE, false);
                }
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
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
