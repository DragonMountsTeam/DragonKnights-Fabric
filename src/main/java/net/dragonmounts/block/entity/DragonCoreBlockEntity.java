package net.dragonmounts.block.entity;

import net.dragonmounts.block.DragonCoreBlock;
import net.dragonmounts.init.DMBlockEntities;
import net.dragonmounts.inventory.DragonCoreHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity.AnimationStatus;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.dragonmounts.util.BlockUtil.updateNeighborStates;

/**
 * @see net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity
 */
public class DragonCoreBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, ExtendedScreenHandlerFactory<BlockPos> {
    public static final int[] SLOTS = new int[]{0};
    private static final String TRANSLATION_KEY = "container.dragonmounts.dragon_core";
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private AnimationStatus stage = AnimationStatus.CLOSED;
    private int openCount;
    private float progress;
    private float progressOld;

    public DragonCoreBlockEntity(BlockPos pos, BlockState state) {
        super(DMBlockEntities.DRAGON_CORE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DragonCoreBlockEntity entity) {
        entity.updateAnimation(level, pos, state);
    }

    protected void updateAnimation(Level level, BlockPos pos, BlockState state) {
        this.progressOld = this.progress;
        switch (this.stage.ordinal()) {
            case 0:
                this.progress = 0.0F;
                return;
            case 1:
                if ((this.progress += 0.1F) >= 1.0F) {
                    this.stage = AnimationStatus.OPENED;
                    this.progress = 1.0F;
                    updateNeighborStates(level, pos, state, 3);
                } else if (this.progressOld == 0.0F) {
                    updateNeighborStates(level, pos, state, 3);
                }
                this.moveCollidedEntities(level, pos, state);
                return;
            case 2:
                this.progress = 1.0F;
                return;
            case 3: if ((this.progress -= 0.1F) <= 0.1F) {
                this.progress = 0.0F;
                this.stage = AnimationStatus.CLOSED;
                updateNeighborStates(level, pos, state, 3);
                level.levelEvent(2003, pos.above(), 0);
                level.destroyBlock(pos, true);
            } else if (this.progressOld == 1.0F) {
                updateNeighborStates(level, pos, state, 3);
            }
        }
    }

    public AnimationStatus getAnimationStatus() {
        return this.stage;
    }

    public AABB getBoundingBox() {
        return Shulker.getProgressAabb(1.0F, Direction.UP, 0.5F * this.getProgress(1.0F));
    }

    protected void moveCollidedEntities(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof DragonCoreBlock) {
            var direction = Direction.UP;
            var box = Shulker.getProgressDeltaAabb(1.0F, direction, this.progressOld, this.progress).move(pos);
            var list = level.getEntities(null, box);
            if (!list.isEmpty()) {
                for (var entity : list) {
                    if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
                        entity.move(MoverType.SHULKER_BOX, new Vec3((box.getXsize() + 0.01) * direction.getStepX(), (box.getYsize() + 0.01) * direction.getStepY(), (box.getZsize() + 0.01) * direction.getStepZ()));
                    }
                }

            }
        }
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    @SuppressWarnings("EnhancedSwitchMigration")
    public boolean triggerEvent(int id, int data) {
        if (id == 1) switch (this.openCount = data) {
            case 0:
                this.stage = AnimationStatus.CLOSING;
                return true;
            case 1:
                this.stage = AnimationStatus.OPENING;
                return true;
            default: return true;
        }
        return super.triggerEvent(id, data);
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            if (this.openCount < 0) this.openCount = 0;
            var pos = this.worldPosition;
            var level = this.level;
            //noinspection DataFlowIssue
            level.blockEvent(pos, this.getBlockState().getBlock(), 1, ++this.openCount);
            if (this.openCount == 1) {
                RandomSource random = level.random;
                level.playSound(null, pos, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.9F, random.nextFloat() * 0.1F + 0.9F);
                level.playSound(null, pos, SoundEvents.ENDER_DRAGON_AMBIENT, SoundSource.BLOCKS, 0.05F, random.nextFloat() * 0.3F + 0.9F);
                level.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.08F, random.nextFloat() * 0.1F + 0.9F);
            }

        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            var pos = this.worldPosition;
            var level = this.level;
            //noinspection DataFlowIssue
            level.blockEvent(pos, this.getBlockState().getBlock(), 1, --this.openCount);
            if (this.openCount <= 0) {
                level.gameEvent(player, GameEvent.CONTAINER_CLOSE, pos);
                level.playSound(null, pos, SoundEvents.ENDER_CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
            }
        }
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable(TRANSLATION_KEY);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag) && tag.contains("Items", 9)) {
            ContainerHelper.loadAllItems(tag, this.items, provider);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, false, provider);
        }
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    public int @NotNull [] getSlotsForFace(Direction direction) {
        return SLOTS;
    }

    public boolean canPlaceItemThroughFace(int i, ItemStack stack, @Nullable Direction direction) {
        return false;
    }

    public boolean canTakeItemThroughFace(int i, ItemStack stack, Direction direction) {
        return false;
    }

    public float getProgress(float partialTicks) {
        return Mth.lerp(partialTicks, this.progressOld, this.progress);
    }

    @Override
    protected @NotNull DragonCoreHandler createMenu(int id, Inventory player) {
        return new DragonCoreHandler(id, player, this);
    }

    public boolean isClosed() {
        return this.stage == AnimationStatus.CLOSED;
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }
}
