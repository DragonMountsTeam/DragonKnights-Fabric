package net.dragonmounts.block.entity;

import net.dragonmounts.block.DragonCoreBlock;
import net.dragonmounts.init.DMBlockEntities;
import net.dragonmounts.inventory.DragonCoreScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity.AnimationStage;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

/**
 * @see net.minecraft.block.entity.ShulkerBoxBlockEntity
 */
public class DragonCoreBlockEntity extends LootableContainerBlockEntity implements SidedInventory, Tickable, ExtendedScreenHandlerFactory {
    public static final int[] SLOTS = new int[]{0};
    private static final String TRANSLATION_KEY = "container.dragonmounts.dragon_core";
    private DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private int openCount;
    private AnimationStage stage = AnimationStage.CLOSED;
    private float progress;
    private float progressOld;

    public DragonCoreBlockEntity() {
        super(DMBlockEntities.DRAGON_CORE);
    }

    @Override
    public void tick() {
        this.updateAnimation();
        if (this.stage == AnimationStage.OPENING || this.stage == AnimationStage.CLOSING) {
            this.pushEntities();
        }
    }

    protected void updateAnimation() {
        this.progressOld = this.progress;
        switch (this.stage.ordinal()) {
            case 0:
                this.progress = 0.0F;
                return;
            case 1: if ((this.progress += 0.1F) >= 1.0F) {
                    this.pushEntities();
                    this.stage = AnimationStage.OPENED;
                    this.progress = 1.0F;
                    this.updateNeighborStates();
                }
                return;
            case 2:
                this.progress = 1.0F;
                return;
            case 3: if ((this.progress -= 0.1F) <= 0.1F) {
                this.progress = 0.0F;
                //noinspection DataFlowIssue
                this.world.syncWorldEvent(2003, this.pos.up(), 0);
                this.world.breakBlock(this.pos, true);
                this.stage = AnimationStage.CLOSED;
            }
        }
    }

    public AnimationStage getAnimationStage() {
        return this.stage;
    }

    public Box getBoundingBox() {
        return VoxelShapes.fullCube().getBoundingBox().stretch(0, 0.5 * this.getProgress(1.0F), 0);
    }

    public float getProgress(float partialTicks) {
        return MathHelper.lerp(partialTicks, this.progressOld, this.progress);
    }

    public boolean isClosed() {
        return this.stage == AnimationStage.CLOSED;
    }

    private void pushEntities() {
        //noinspection DataFlowIssue
        if (this.world.getBlockState(this.pos).getBlock() instanceof DragonCoreBlock) {
            Box box = this.getBoundingBox().offset(this.pos);
            List<Entity> list = this.world.getOtherEntities(null, box);
            if (list.isEmpty()) return;
            for (Entity entity : list) {
                if (entity.getPistonBehavior() != PistonBehavior.IGNORE) {
                    entity.move(MovementType.SHULKER_BOX, new Vec3d(0, box.maxY + 0.01 - entity.getBoundingBox().minY, 0));
                }
            }
        }
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public boolean onSyncedBlockEvent(int id, int data) {
        if (id == 1) switch (this.openCount = data) {
            case 0:
                this.stage = AnimationStage.CLOSING;
                this.updateNeighborStates();
                return true;
            case 1:
                this.stage = AnimationStage.OPENING;
                this.updateNeighborStates();
                return true;
            default: return true;
        }
        return super.onSyncedBlockEvent(id, data);
    }

    private void updateNeighborStates() {
        this.getCachedState().updateNeighbors(this.world, this.pos, 3);
    }

    @Override
    public void onOpen(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.openCount < 0) this.openCount = 0;
            World level = this.world;
            BlockPos pos;
            //noinspection DataFlowIssue
            level.addSyncedBlockEvent(pos = this.pos, this.getCachedState().getBlock(), 1, ++this.openCount);
            if (this.openCount == 1) {
                Random random = level.random;
                level.playSound(null, pos, SoundEvents.BLOCK_ENDER_CHEST_CLOSE, SoundCategory.BLOCKS, 0.9F, random.nextFloat() * 0.1F + 0.9F);
                level.playSound(null, pos, SoundEvents.ENTITY_ENDER_DRAGON_AMBIENT, SoundCategory.BLOCKS, 0.05F, random.nextFloat() * 0.3F + 0.9F);
                level.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 0.08F, random.nextFloat() * 0.1F + 0.9F);
            }

        }
    }

    @Override
    public void onClose(PlayerEntity player) {
        if (!player.isSpectator()) {
            //noinspection DataFlowIssue
            this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), 1, --this.openCount);
        }
    }

    @Override
    protected Text getContainerName() {
        return new TranslatableText(TRANSLATION_KEY);
    }

    @Override
    public void fromTag(BlockState state, NbtCompound tag) {
        super.fromTag(state, tag);
        this.items = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(tag)) {
            Inventories.readNbt(tag, this.items);
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        if (!this.serializeLootTable(super.writeNbt(tag))) {
            Inventories.writeNbt(tag, this.items);
        }
        return tag;
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        return this.items;
    }

    @Override
    protected void setInvStackList(DefaultedList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected ScreenHandler createScreenHandler(int id, PlayerInventory player) {
        return new DragonCoreScreenHandler(id, player, this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        return false;
    }
}
