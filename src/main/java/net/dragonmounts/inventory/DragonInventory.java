package net.dragonmounts.inventory;

import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.util.ItemStackArrays;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.World;

import java.util.Arrays;

/**
 * @see net.minecraft.inventory.Inventory
 */
public class DragonInventory implements Inventory, NamedScreenHandlerFactory, ExtendedScreenHandlerFactory {
    public static final String DATA_PARAMETER_KEY = "Items";
    public static final int SLOT_SADDLE_INDEX = 0;
    public static final int SLOT_ARMOR_INDEX = 1;
    public static final int SLOT_CHEST_INDEX = 2;
    public static final int INVENTORY_SIZE = 30;
    public final TameableDragonEntity dragon;
    protected final ItemStack[] stacks;

    public DragonInventory(TameableDragonEntity dragon) {
        Arrays.fill(this.stacks = new ItemStack[INVENTORY_SIZE], ItemStack.EMPTY);
        this.dragon = dragon;
    }

    @Override
    public final int size() {
        return this.dragon.hasChest() ? this.stacks.length : 3;
    }

    @Override
    public boolean isEmpty() {
        ItemStack[] stacks = this.stacks;
        for (int i = 3, n = stacks.length; i < n; ++i) {
            if (!stacks[i].isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int index) {
        switch (index) {
            case SLOT_SADDLE_INDEX:
                return this.dragon.getSaddleStack();
            case SLOT_ARMOR_INDEX:
                return this.dragon.getArmorStack();
            case SLOT_CHEST_INDEX:
                return this.dragon.getChestStack();
            default:
                return index > 2 && index < this.stacks.length ? this.stacks[index] : ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack removeStack(int index, int count) {
        if (count <= 0) return ItemStack.EMPTY;
        ItemStack old, neo;
        switch (index) {
            case SLOT_SADDLE_INDEX:
                old = this.dragon.getSaddleStack();
                if (old.isEmpty()) return ItemStack.EMPTY;
                count = Math.min(count, old.getCount());
                neo = old.copy();
                neo.setCount(count);
                old.decrement(count);
                this.dragon.setSaddle(old, true);
                return neo;
            case SLOT_ARMOR_INDEX:
                old = this.dragon.getArmorStack();
                if (old.isEmpty()) return ItemStack.EMPTY;
                count = Math.min(count, old.getCount());
                neo = old.copy();
                neo.setCount(count);
                old.decrement(count);
                this.dragon.setArmor(old, true);
                return neo;
            case SLOT_CHEST_INDEX:
                old = this.dragon.getChestStack();
                if (old.isEmpty()) return ItemStack.EMPTY;
                count = Math.min(count, old.getCount());
                neo = old.copy();
                neo.setCount(count);
                old.decrement(count);
                this.dragon.setChest(old, true);
                return neo;
            default:
                neo = ItemStackArrays.split(this.stacks, index, count);
                if (!neo.isEmpty()) {
                    this.markDirty();
                }
                return neo;
        }
    }

    @Override
    public ItemStack removeStack(int index) {
        ItemStack stack;
        switch (index) {
            case SLOT_SADDLE_INDEX:
                stack = this.dragon.getSaddleStack();
                if (stack.isEmpty()) return ItemStack.EMPTY;
                this.dragon.setSaddle(ItemStack.EMPTY, false);
                return stack;
            case SLOT_ARMOR_INDEX:
                stack = this.dragon.getArmorStack();
                if (stack.isEmpty()) return ItemStack.EMPTY;
                this.dragon.setSaddle(ItemStack.EMPTY, false);
                return stack;
            case SLOT_CHEST_INDEX:
                stack = this.dragon.getChestStack();
                if (stack.isEmpty()) return ItemStack.EMPTY;
                this.dragon.setChest(ItemStack.EMPTY, false);
                return stack;
            default:
                return ItemStackArrays.take(this.stacks, index);
        }
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        switch (index) {
            case SLOT_SADDLE_INDEX:
                this.dragon.setSaddle(stack, true);
                return;
            case SLOT_ARMOR_INDEX:
                this.dragon.setArmor(stack, true);
                return;
            case SLOT_CHEST_INDEX:
                this.dragon.setChest(stack, true);
                return;
            default: if (index >= 0 && index < this.stacks.length) {
                if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack()) {
                    stack.setCount(this.getMaxCountPerStack());
                }
                this.stacks[index] = stack;
                this.markDirty();
            }
        }
    }

    public boolean setItemAfterChecked(int index, ItemStack stack) {
        Item item = stack.getItem();
        switch (index) {
            case SLOT_SADDLE_INDEX:
                if (item == Items.AIR || LimitedSlot.Saddle.canInsert(item)) {
                    this.dragon.setSaddle(stack, true);
                    return true;
                }
                return false;
            case SLOT_ARMOR_INDEX:
                if (item == Items.AIR || LimitedSlot.DragonArmor.canInsert(item)) {
                    this.dragon.setArmor(stack, true);
                    return true;
                }
                return false;
            case SLOT_CHEST_INDEX:
                if (item == Items.AIR || LimitedSlot.SingleWoodenChest.canInsert(item)) {
                    this.dragon.setChest(stack, true);
                    return true;
                }
                return false;
            default:
                if (index < 0 || index >= this.stacks.length || !this.dragon.hasChest()) return false;
                if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack()) {
                    stack.setCount(this.getMaxCountPerStack());
                }
                this.stacks[index] = stack;
                this.markDirty();
                return true;
        }
    }

    @Override
    public void markDirty() {
        this.dragon.inventoryChanged();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return this.dragon.isAlive() && player.squaredDistanceTo(this.dragon) <= 64.0D;//8 blocks
    }

    @Override
    public void clear() {
        TameableDragonEntity dragon = this.dragon;
        ItemStack empty = ItemStack.EMPTY;
        Arrays.fill(this.stacks, empty);
        dragon.setSaddle(empty, true);
        dragon.setArmor(empty, true);
        dragon.setChest(empty, true);
        this.markDirty();
    }

    @Override
    public Text getDisplayName() {
        return this.dragon.getDisplayName();
    }

    @Override
    public DragonInventoryScreenHandler createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return new DragonInventoryScreenHandler(id, inventory, this, this.dragon);
    }

    public void dropContents(boolean keepEquipments, double offsetY) {
        TameableDragonEntity dragon = this.dragon;
        World level = dragon.world;
        double x = dragon.getX();
        double y = dragon.getY() + offsetY;
        double z = dragon.getZ();
        if (!keepEquipments) {
            ItemScatterer.spawn(level, x, y, z, dragon.getSaddleStack());
            ItemScatterer.spawn(level, x, y, z, dragon.getArmorStack());
            ItemScatterer.spawn(level, x, y, z, dragon.getChestStack());
        }
        ItemStack[] stacks = this.stacks;
        for (int i = 3, n = stacks.length; i < n; ++i) {
            ItemScatterer.spawn(level, x, y, z, stacks[i]);
        }
    }

    public void fromTag(NbtList list) {
        TameableDragonEntity dragon = this.dragon;
        ItemStack[] stacks = this.stacks;
        ItemStack empty = ItemStack.EMPTY;
        Arrays.fill(stacks, empty);
        boolean saddle = false;
        boolean armor = false;
        boolean chest = false;
        for (int i = 0, j, n = list.size(), m = stacks.length; i < n; ++i) {
            NbtCompound tag = list.getCompound(i);
            switch (j = tag.getByte("Slot") & 255) {
                case SLOT_SADDLE_INDEX:
                    dragon.setSaddle(ItemStack.fromNbt(tag), saddle = true);
                    break;
                case SLOT_ARMOR_INDEX:
                    dragon.setArmor(ItemStack.fromNbt(tag), armor = true);
                    break;
                case SLOT_CHEST_INDEX:
                    dragon.setChest(ItemStack.fromNbt(tag), chest = true);
                    break;
                default: if (j < m) {
                    ItemStack stack = stacks[j] = ItemStack.fromNbt(tag);
                    if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack()) {
                        stack.setCount(this.getMaxCountPerStack());
                    }
                }
            }
        }
        if (!saddle) {
            dragon.setSaddle(empty, true);
        }
        if (!armor) {
            dragon.setArmor(empty, true);
        }
        if (!chest) {
            dragon.setChest(empty, true);
        }
        this.markDirty();
    }

    public NbtList createTag() {
        TameableDragonEntity dragon = this.dragon;
        ItemStack[] stacks = this.stacks;
        stacks[SLOT_SADDLE_INDEX] = dragon.getSaddleStack();
        stacks[SLOT_ARMOR_INDEX] = dragon.getArmorStack();
        stacks[SLOT_CHEST_INDEX] = dragon.getChestStack();
        return ItemStackArrays.writeList(new NbtList(), stacks);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buffer) {
        this.dragon.writeId(buffer);
    }
}
