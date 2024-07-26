package net.dragonmounts.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.List;

public class ItemStackArrays {
    /**
     * @see net.minecraft.inventory.Inventories#splitStack(List, int, int)
     */
    public static ItemStack split(ItemStack[] stacks, int index, int amount) {
        if (index >= 0 && index < stacks.length) {
            ItemStack stack = stacks[index];
            if (!stack.isEmpty() && amount > 0) {
                return stack.split(amount);
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * @see net.minecraft.inventory.Inventories#removeStack(List, int)
     */
    public static ItemStack take(ItemStack[] stacks, int index) {
        if (index >= 0 && index < stacks.length) {
            ItemStack stack = stacks[index];
            stacks[index] = ItemStack.EMPTY;
            return stack;
        }
        return ItemStack.EMPTY;
    }

    public static NbtList writeList(NbtList list, ItemStack[] stacks) {
        for (int i = 0, n = stacks.length; i < n; ++i) {
            ItemStack stack = stacks[i];
            if (!stack.isEmpty()) {
                NbtCompound tag = new NbtCompound();
                tag.putByte("Slot", (byte) i);
                list.add(stack.writeNbt(tag));
            }
        }
        return list;
    }
}
