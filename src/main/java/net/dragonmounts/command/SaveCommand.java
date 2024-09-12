package net.dragonmounts.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.init.DMItems;
import net.dragonmounts.item.DragonAmuletItem;
import net.dragonmounts.item.DragonEssenceItem;
import net.dragonmounts.item.IEntityContainer;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;

import static net.dragonmounts.command.DMCommand.createClassCastException;
import static net.dragonmounts.entity.dragon.TameableDragonEntity.FLYING_DATA_PARAMETER_KEY;

public class SaveCommand {

    public static ArgumentBuilder<ServerCommandSource, ?> register(Predicate<ServerCommandSource> permission) {
        return CommandManager.literal("save").requires(permission).then(CommandManager.argument("target", EntityArgumentType.entity())
                .then(CommandManager.literal("amulet").executes(context -> saveAmulet(context, EntityArgumentType.getEntity(context, "target"))))
                .then(CommandManager.literal("essence").executes(context -> saveEssence(context, EntityArgumentType.getEntity(context, "target"))))
                .then(CommandManager.literal("container").then(CommandManager.argument("item", ItemStackArgumentType.itemStack()).executes(context ->
                        save(context, ItemStackArgumentType.getItemStackArgument(context, "item"), EntityArgumentType.getEntity(context, "target"))
                )))
        );
    }


    public static int saveAmulet(CommandContext<ServerCommandSource> context, Entity target) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        if (target instanceof TameableDragonEntity) {
            TameableDragonEntity dragon = (TameableDragonEntity) target;
            DragonAmuletItem amulet = dragon.getDragonType().getInstance(DragonAmuletItem.class, null);
            if (amulet != null) {
                give(source, amulet.saveEntity(dragon));
                return 1;
            }
        }
        ItemStack stack = DMItems.AMULET.saveEntity(target);
        if (stack.isEmpty()) {
            //source.sendFailure();
            return 0;
        }
        give(source, stack);
        return 1;
    }

    public static int saveEssence(CommandContext<ServerCommandSource> context, Entity target) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        if (target instanceof TameableDragonEntity) {
            TameableDragonEntity dragon = (TameableDragonEntity) target;
            DragonEssenceItem essence = dragon.getDragonType().getInstance(DragonEssenceItem.class, null);
            if (essence != null) {
                give(source, essence.saveEntity(dragon));
                return 1;
            }
        }
        source.sendError(createClassCastException(target, TameableDragonEntity.class));
        return 0;
    }

    public static int save(CommandContext<ServerCommandSource> context, ItemStackArgument input, Entity target) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Item item = input.getItem();
        if (item instanceof IEntityContainer<?> && ((IEntityContainer<?>) item).getContentType().isInstance(target)) {
            ItemStack stack;
            try {
                stack = (ItemStack) IEntityContainer.class
                        .getDeclaredMethod("saveEntity", Entity.class)
                        .invoke(item, target);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                source.sendError(new LiteralText(e.getMessage()));
                return 0;
            }
            if (stack.isEmpty()) {
                //source.sendFailure();
                return 0;
            }
            give(source, stack);
            return 1;
        }
        EntityType<?> type = target.getType();
        if (type.isSaveable()) {
            ItemStack stack = input.createStack(1, false);
            NbtCompound tag = target.writeNbt(new NbtCompound());
            tag.putString("id", EntityType.getId(type).toString());
            tag.remove(FLYING_DATA_PARAMETER_KEY);
            tag.remove("UUID");
            NbtCompound root = new NbtCompound();
            root.put("EntityTag", IEntityContainer.simplifyData(tag));
            stack.setTag(root);
            give(source, stack);
            return 1;
        }
        //source.sendFailure();
        return 0;
    }

    public static void give(ServerCommandSource source, ItemStack stack) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        Text name = stack.getName();
        int count = stack.getCount();
        if (!player.inventory.insertStack(stack)) {
            ItemEntity item = player.dropItem(stack, false);
            if (item != null) {
                item.resetPickupDelay();
                item.setOwner(player.getUuid());
            }
        }
        source.sendFeedback(new TranslatableText("commands.give.success.single", count, name, player.getDisplayName()), true);
    }
}
