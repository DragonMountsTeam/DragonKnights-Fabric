package net.dragonmounts.mixin;

import net.dragonmounts.item.AmuletItem;
import net.dragonmounts.item.IEntityContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    @Shadow
    private int itemAge;

    @Shadow
    public abstract ItemStack getStack();

    @Inject(method = "setStack", at = @At("TAIL"))
    public void persistent(ItemStack stack, CallbackInfo info) {
        Item item = stack.getItem();
        if (item instanceof IEntityContainer && !((IEntityContainer<?>) item).isEmpty(stack.getTag())) {
            this.itemAge = Short.MIN_VALUE;
        }
    }

    /**
     * Mojang patch that fixes <a href="https://bugs.mojang.com/browse/MC-53850">MC-53850</a>
     */
    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;scheduleVelocityUpdate()V"), cancellable = true)
    public void fixMC53850(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (this.world.isClient) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;remove()V"))
    public void onDestroy(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        ItemStack stack = this.getStack();
        Item item = stack.getItem();
        if (item instanceof AmuletItem) {
            AmuletItem<?> amulet = (AmuletItem<?>) item;
            NbtCompound tag = stack.getTag();
            if (amulet.isEmpty(tag)) return;
            assert tag != null;
            ServerWorld level = (ServerWorld) this.world;// safe to cast with MC-53850 fixed
            Entity entity = amulet.loadEntity(level, null, tag, this.getLandingPos(), SpawnReason.BUCKET, null, true, false);
            if (entity != null) {
                level.spawnEntityAndPassengers(entity);
            }
        }
    }

    private ItemEntityMixin(EntityType<?> a, World b) {super(a, b);}
}