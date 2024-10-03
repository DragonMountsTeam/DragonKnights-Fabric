package net.dragonmounts.item;

import net.dragonmounts.client.gui.FluteOverlay;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.init.DMDataComponents;
import net.dragonmounts.network.FluteCommandPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class FluteItem extends Item {
    public static Handler HANDLER = new Handler();
    public static final int USE_DURATION = 32;

    public FluteItem(Properties props) {
        super(props);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return stack.has(DMDataComponents.FLUTE_SOUND) ? UseAnim.TOOT_HORN : UseAnim.NONE;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        return entity instanceof TameableDragonEntity
                ? ((TameableDragonEntity) entity).authorizeFlute(player, player.getItemInHand(hand))
                : super.interactLivingEntity(stack, player, entity, hand);
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {
        return stack.is(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (stack.has(DMDataComponents.FLUTE_SOUND)) {
            player.startUsingItem(hand);
            HANDLER.start(player);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainTime) {
        if (this.getUseDuration(stack, entity) >= USE_DURATION + remainTime && entity instanceof Player player && player.isLocalPlayer()) {
            int command = FluteOverlay.getCommand();
            if (command == -1) return;
            var sound = stack.get(DMDataComponents.FLUTE_SOUND);
            if (sound != null) {
                ClientPlayNetworking.send(new FluteCommandPayload(sound.uuid, command));
            }
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        HANDLER.tick(entity);
    }

    public static class Handler {
        public void start(Player player) {}

        public void tick(LivingEntity entity) {}
    }
}
