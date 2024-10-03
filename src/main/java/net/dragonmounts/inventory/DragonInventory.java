package net.dragonmounts.inventory;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.util.ItemStackArrays;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SaddleItem;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * @see net.minecraft.world.entity.player.Inventory
 * @see net.minecraft.world.SimpleContainer
 */
public class DragonInventory implements Container, StackedContentsCompatible {
    public static final String DATA_PARAMETER_KEY = "Items";
    public static final int SLOT_ARMOR_INDEX = 0;
    public static final int SLOT_CHEST_INDEX = 1;
    public static final int SLOT_SADDLE_INDEX = 2;
    public static final int INVENTORY_SIZE = 30;
    public final TameableDragonEntity dragon;
    public final SlotAccess armor;
    public final Slot chest;
    public final Slot saddle;
    protected final ItemStack[] stacks;

    public DragonInventory(
            TameableDragonEntity dragon,
            EntityDataAccessor<ItemStack> chest,
            BooleanConsumer onChestChanged,
            EntityDataAccessor<ItemStack> saddle,
            BooleanConsumer onSaddleChanged
    ) {
        Arrays.fill(this.stacks = new ItemStack[INVENTORY_SIZE], ItemStack.EMPTY);
        this.dragon = dragon;
        this.armor = SlotAccess.forEquipmentSlot(dragon, EquipmentSlot.BODY, dragon::isBodyArmorItem);
        this.chest = new Slot(chest, this::isChest, onChestChanged);
        this.saddle = new Slot(saddle, this::isSaddle, onSaddleChanged);
    }

    public boolean isSaddle(ItemStack stack) {
        return stack.getItem() instanceof SaddleItem;
    }

    public boolean isChest(ItemStack stack) {
        return stack.is(ConventionalItemTags.WOODEN_CHESTS);
    }

    public boolean onInteract(ItemStack stack) {
        if (this.dragon.isBodyArmorItem(stack) && this.armor.get().isEmpty()) {
            this.dragon.setItemSlot(EquipmentSlot.BODY, stack.split(1));
            return true;
        }
        if (this.isSaddle(stack) && this.saddle.get().isEmpty()) {
            this.saddle.set(stack.split(1));
            return true;
        }
        if (this.isChest(stack) && this.chest.get().isEmpty()) {
            this.chest.set(stack.split(1));
            return true;
        }
        return false;
    }

    public SlotAccess access(int slot) {
        final int index = slot - 500;
        if (index < 0 || index > this.stacks.length) return SlotAccess.NULL;
        return switch (index) {
            case SLOT_ARMOR_INDEX -> this.armor;
            case SLOT_CHEST_INDEX -> this.chest;
            case SLOT_SADDLE_INDEX -> this.saddle;
            default -> new SlotAccess() {
                @Override
                public @NotNull ItemStack get() {
                    return stacks[index];
                }

                @Override
                public boolean set(ItemStack stack) {
                    stacks[index] = stack;
                    return true;
                }
            };
        };
    }

    @Override
    public final int getContainerSize() {
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
    public @NotNull ItemStack getItem(int index) {
        return switch (index) {
            case SLOT_ARMOR_INDEX -> this.armor.get();
            case SLOT_CHEST_INDEX -> this.chest.get();
            case SLOT_SADDLE_INDEX -> this.saddle.get();
            default -> index > 2 && index < this.stacks.length ? this.stacks[index] : ItemStack.EMPTY;
        };
    }

    @Override
    public @NotNull ItemStack removeItem(int index, int count) {
        if (count <= 0) return ItemStack.EMPTY;
        switch (index) {
            case SLOT_ARMOR_INDEX:
                return this.armor.get().split(count);
            case SLOT_CHEST_INDEX:
                return this.chest.get().split(count);
            case SLOT_SADDLE_INDEX:
                return this.saddle.get().split(count);
            default:
                var stack = ItemStackArrays.removeItem(this.stacks, index, count);
                if (!stack.isEmpty()) {
                    this.setChanged();
                }
                return stack;
        }
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int index) {
        ItemStack stack;
        switch (index) {
            case SLOT_ARMOR_INDEX:
                stack = this.armor.get();
                if (stack.isEmpty()) return ItemStack.EMPTY;
                this.armor.set(ItemStack.EMPTY);
                return stack;
            case SLOT_CHEST_INDEX:
                stack = this.chest.get();
                if (stack.isEmpty()) return ItemStack.EMPTY;
                this.chest.set(ItemStack.EMPTY);
                return stack;
            case SLOT_SADDLE_INDEX:
                stack = this.saddle.get();
                if (stack.isEmpty()) return ItemStack.EMPTY;
                this.saddle.set(ItemStack.EMPTY);
                return stack;
            default:
                return ItemStackArrays.takeItem(this.stacks, index);
        }
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        switch (index) {
            case SLOT_ARMOR_INDEX:
                this.armor.set(stack);
                return;
            case SLOT_CHEST_INDEX:
                this.chest.set(stack);
                return;
            case SLOT_SADDLE_INDEX:
                this.saddle.set(stack);
                return;
            default:
                if (index >= 0 && index < this.stacks.length) {
                    stack.limitSize(this.getMaxStackSize(stack));
                    this.stacks[index] = stack;
                    this.setChanged();
                }
        }
    }

    @Override
    public void fillStackedContents(StackedContents contents) {
        contents.accountStack(this.armor.get());
        contents.accountStack(this.chest.get());
        contents.accountStack(this.saddle.get());
        ItemStack[] stacks = this.stacks;
        for (int i = 3, n = stacks.length; i < n; ++i) {
            contents.accountStack(stacks[i]);
        }
    }

    @Override
    public void setChanged() {
        this.dragon.inventoryChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.dragon.isAlive() && player.canInteractWithEntity(this.dragon, 8.0);
    }

    @Override
    public void clearContent() {
        ItemStack empty = ItemStack.EMPTY;
        Arrays.fill(this.stacks, empty);
        this.armor.set(empty);
        this.chest.set(empty);
        this.saddle.set(empty);
        this.setChanged();
    }

    public void dropContents(boolean keepEquipments, double offsetY) {
        var pos = this.dragon.position();
        ItemStack[] stacks = this.stacks;
        stacks[SLOT_ARMOR_INDEX] = this.armor.get();
        stacks[SLOT_CHEST_INDEX] = this.chest.get();
        stacks[SLOT_SADDLE_INDEX] = this.saddle.get();
        ItemStackArrays.dropContents(this.dragon.level(), pos.x, pos.y + offsetY, pos.z, stacks, keepEquipments ? 3 : 0);
    }

    public void loadItems(ListTag list, HolderLookup.Provider registry) {
        var empty = ItemStack.EMPTY;
        ItemStack[] stacks = this.stacks;
        Arrays.fill(stacks, empty);
        for (int i = 0, j, n = list.size(), m = stacks.length; i < n; ++i) {
            var tag = list.getCompound(i);
            if ((j = tag.getByte("Slot") & 255) == SLOT_ARMOR_INDEX || j >= m) continue;
            var stack = stacks[j] = ItemStack.parse(registry, tag).orElse(empty);
            stack.limitSize(this.getMaxStackSize(stack));
        }
        this.chest.set(stacks[SLOT_CHEST_INDEX]);
        this.saddle.set(stacks[SLOT_SADDLE_INDEX]);
        this.setChanged();
    }

    public ListTag saveItems(HolderLookup.Provider registry) {
        ItemStack[] stacks = this.stacks;
        stacks[SLOT_CHEST_INDEX] = this.chest.get();
        stacks[SLOT_SADDLE_INDEX] = this.saddle.get();
        return ItemStackArrays.saveItems(registry, new ListTag(), stacks, 1);
    }

    public class Slot implements SlotAccess {
        public final SynchedEntityData data = DragonInventory.this.dragon.getEntityData();
        public final Predicate<ItemStack> predicate;
        protected final EntityDataAccessor<ItemStack> key;
        protected ItemStack stack = ItemStack.EMPTY;
        protected BooleanConsumer callback;

        public Slot(EntityDataAccessor<ItemStack> key, Predicate<ItemStack> predicate, BooleanConsumer callback) {
            this.key = key;
            this.predicate = predicate;
            this.callback = callback;
        }

        @Override
        public @NotNull ItemStack get() {
            return this.stack;
        }

        @Override
        public boolean set(ItemStack stack) {
            if (stack.isEmpty()) {
                this.callback.accept(false);
                this.data.set(this.key, this.stack = stack, false);
                return true;
            }
            if (this.predicate.test(stack)) {
                this.data.set(this.key, this.stack = stack, true);
                this.callback.accept(true);
                return true;
            }
            return false;
        }

        public void setLocal(ItemStack stack, boolean sync) {
            this.callback.accept(this.predicate.test(this.stack = stack));
            if (sync) {
                this.data.set(this.key, stack, !stack.isEmpty());
            }
        }
    }
}
