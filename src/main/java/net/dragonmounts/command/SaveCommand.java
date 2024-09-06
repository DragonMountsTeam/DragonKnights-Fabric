package net.dragonmounts.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.init.DMItems;
import net.dragonmounts.item.DragonAmuletItem;
import net.dragonmounts.item.DragonEssenceItem;
import net.dragonmounts.item.DragonSpawnEggItem;
import net.dragonmounts.item.IEntityContainer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;

import static net.dragonmounts.command.DMCommand.createClassCastException;
import static net.dragonmounts.entity.dragon.TameableDragonEntity.FLYING_DATA_PARAMETER_KEY;
import static net.dragonmounts.util.EntityUtil.saveWithId;

public class SaveCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext builder, Predicate<CommandSourceStack> permission) {
        return Commands.literal("save").requires(permission).then(Commands.argument("target", EntityArgument.entity())
                .then(Commands.literal("amulet").executes(context -> saveAmulet(context, EntityArgument.getEntity(context, "target"))))
                .then(Commands.literal("essence").executes(context -> saveEssence(context, EntityArgument.getEntity(context, "target"))))
                .then(Commands.literal("spawn_egg").executes(context -> saveSpawnEgg(context, EntityArgument.getEntity(context, "target"))))
                .then(Commands.literal("container").then(Commands.argument("item", ItemArgument.item(builder)).executes(context ->
                        save(context, ItemArgument.getItem(context, "item"), EntityArgument.getEntity(context, "target"))
                )))
        );
    }

    public static int saveAmulet(CommandContext<CommandSourceStack> context, Entity target) throws CommandSyntaxException {
        var source = context.getSource();
        if (target instanceof TameableDragonEntity dragon) {
            var amulet = dragon.getDragonType().getInstance(DragonAmuletItem.class, null);
            if (amulet != null) {
                give(source, amulet.saveEntity(dragon, DataComponentPatch.EMPTY));
                return 1;
            }
        }
        var stack = DMItems.AMULET.saveEntity(target, DataComponentPatch.EMPTY);
        if (stack.isEmpty()) {
            source.sendFailure(Component.translatable("commands.dragonmounts.save.cannot_serialize", target.getDisplayName()));
            return 0;
        }
        give(source, stack);
        return 1;
    }

    public static int saveEssence(CommandContext<CommandSourceStack> context, Entity target) throws CommandSyntaxException {
        var source = context.getSource();
        if (target instanceof TameableDragonEntity dragon) {
            var essence = dragon.getDragonType().getInstance(DragonEssenceItem.class, null);
            if (essence != null) {
                give(source, essence.saveEntity(dragon, DataComponentPatch.EMPTY));
                return 1;
            }
        }
        source.sendFailure(createClassCastException(target, TameableDragonEntity.class));
        return 0;
    }

    public static int saveSpawnEgg(CommandContext<CommandSourceStack> context, Entity target) throws CommandSyntaxException {
        var source = context.getSource();
        if (target instanceof TameableDragonEntity dragon) {
            var spawnEgg = dragon.getDragonType().getInstance(DragonSpawnEggItem.class, null);
            if (spawnEgg != null) {
                give(source, spawnEgg.saveEntity(dragon));
                return 1;
            }
        }
        var type = target.getType();
        if (type.canSerialize()) {
            var item = SpawnEggItem.byId(type);
            if (item == null) {
                source.sendFailure(Component.translatable("commands.dragonmounts.save.no_spawn_egg", target.getDisplayName()));
                return 0;
            }
            give(source, IEntityContainer.saveEntityData(item, saveWithId(target, new CompoundTag()), DataComponentPatch.EMPTY));
            return 1;
        }
        source.sendFailure(Component.translatable("commands.dragonmounts.save.cannot_serialize", target.getDisplayName()));
        return 0;
    }

    public static int save(CommandContext<CommandSourceStack> context, ItemInput input, Entity target) throws CommandSyntaxException {
        var source = context.getSource();
        var item = input.getItem();
        if (item instanceof IEntityContainer<?> container && container.getContentType().isInstance(target)) {
            ItemStack stack;
            try {
                stack = (ItemStack) IEntityContainer.class
                        .getDeclaredMethod("saveEntity", Entity.class, DataComponentPatch.class)
                        .invoke(container, target, input.createItemStack(1, false).getComponentsPatch());
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                source.sendFailure(Component.literal(e.getMessage()));
                return 0;
            }
            if (stack.isEmpty()) {
                source.sendFailure(Component.translatable("commands.dragonmounts.save.cannot_serialize", target.getDisplayName()));
                return 0;
            }
            give(source, stack);
            return 1;
        }
        if (target.getType().canSerialize()) {
            var stack = input.createItemStack(1, false);
            var tag = saveWithId(target, new CompoundTag());
            tag.remove(FLYING_DATA_PARAMETER_KEY);
            tag.remove("UUID");
            stack.set(DataComponents.ENTITY_DATA, IEntityContainer.simplifyData(tag));
            give(source, stack);
            return 1;
        }
        source.sendFailure(Component.translatable("commands.dragonmounts.save.cannot_serialize", target.getDisplayName()));
        return 0;
    }

    public static void give(CommandSourceStack source, ItemStack stack) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        var name = stack.getDisplayName();
        int count = stack.getCount();
        if (!player.getInventory().add(stack)) {
            var item = player.drop(stack, false);
            if (item != null) {
                item.setNoPickUpDelay();
                item.setTarget(player.getUUID());
            }
        }
        source.sendSuccess(() -> Component.translatable("commands.give.success.single", count, name, player.getDisplayName()), true);
    }
}
