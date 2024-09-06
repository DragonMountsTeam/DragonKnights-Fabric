package net.dragonmounts.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.dragonmounts.api.DragonTypified;
import net.dragonmounts.block.AbstractDragonHeadBlock;
import net.dragonmounts.block.DragonHeadBlock;
import net.dragonmounts.block.DragonHeadWallBlock;
import net.dragonmounts.block.HatchableDragonEggBlock;
import net.dragonmounts.init.DragonTypes;
import net.dragonmounts.registry.DragonType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

import static net.dragonmounts.command.DMCommand.createClassCastException;
import static net.dragonmounts.init.DragonVariants.ENDER_FEMALE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.ROTATION_16;

public class TypeCommand {
    public abstract static class CommandHandler<A> {
        protected abstract A getArgument(CommandContext<CommandSourceStack> context) throws CommandSyntaxException;

        protected abstract int setType(CommandContext<CommandSourceStack> context, A argument, DragonType type);

        protected abstract int getType(CommandContext<CommandSourceStack> context, A argument);

        public <T> RequiredArgumentBuilder<CommandSourceStack, T> load(RequiredArgumentBuilder<CommandSourceStack, T> builder, Predicate<CommandSourceStack> permission) {
            for (var type : DragonType.REGISTRY) {
                builder.then(Commands.literal(type.identifier.toString()).requires(permission).executes(context -> this.setType(context, this.getArgument(context), type)));
            }
            builder.executes(context -> this.getType(context, this.getArgument(context)));
            return builder;
        }
    }

    public static class BlockHandler extends CommandHandler<BlockPos> {
        @FunctionalInterface
        public interface Getter {
            DragonType get(Block block, ServerLevel level, BlockPos pos, BlockState state);
        }

        @FunctionalInterface
        public interface Setter {
            BlockState set(Block block, ServerLevel level, BlockPos pos, BlockState state, DragonType type);
        }

        public static final Setter SETTER_DRAGON_EEG = (block, level, pos, state, type) -> type.ifPresent(HatchableDragonEggBlock.class, HatchableDragonEggBlock::defaultBlockState, state);
        public static final Setter SETTER_DRAGON_HEAD = (block, level, pos, state, type) -> {
            var variant = type.variants.draw(level.random, block == Blocks.DRAGON_HEAD ? ENDER_FEMALE : block instanceof AbstractDragonHeadBlock head ? head.variant : null, false);
            return variant == null ? state : variant.headBlock.defaultBlockState().setValue(ROTATION_16, state.getValue(ROTATION_16));
        };
        public static final Setter SETTER_DRAGON_HEAD_WALL = (block, level, pos, state, type) -> {
            var variant = type.variants.draw(level.random, block == Blocks.DRAGON_WALL_HEAD ? ENDER_FEMALE : block instanceof AbstractDragonHeadBlock head ? head.variant : null, false);
            return variant == null ? state : variant.headWallBlock.defaultBlockState().setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING));
        };

        private final Reference2ObjectOpenHashMap<Class<? extends Block>, Getter> getters = new Reference2ObjectOpenHashMap<>();
        private final Reference2ObjectOpenHashMap<Class<? extends Block>, Setter> setters = new Reference2ObjectOpenHashMap<>();

        @SuppressWarnings("UnusedReturnValue")
        public Getter bind(Class<? extends Block> clazz, Getter getter) {
            return getter == null ? this.getters.remove(clazz) : this.getters.put(clazz, getter);
        }

        @SuppressWarnings("UnusedReturnValue")
        public Setter bind(Class<? extends Block> clazz, Setter setter) {
            return setter == null ? this.setters.remove(clazz) : this.setters.put(clazz, setter);
        }

        @Override
        protected BlockPos getArgument(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            return BlockPosArgument.getLoadedBlockPos(context, "pos");
        }

        @Override
        protected int setType(CommandContext<CommandSourceStack> context, BlockPos pos, DragonType type) {
            var source = context.getSource();
            var level = source.getLevel();
            var original = level.getBlockState(pos);
            var block = original.getBlock();
            var clazz = block.getClass();
            var setter = this.setters.get(clazz);
            if (setter != null) {
                var state = setter.set(block, level, pos, original, type);
                if (state != original) {
                    level.setBlockAndUpdate(pos, state);
                    source.sendSuccess(() -> Component.translatable("commands.dragonmounts.type.block.set", pos.getX(), pos.getY(), pos.getZ(), type.getName()), true);
                    return 1;
                }
            }
            source.sendFailure(Component.literal("java.lang.NullPointerException: " + clazz.getName() + " has not bound to a handler"));
            return 0;
        }

        @Override
        protected int getType(CommandContext<CommandSourceStack> context, BlockPos pos) {
            var source = context.getSource();
            var level = source.getLevel();
            var state = level.getBlockState(pos);
            var block = state.getBlock();
            var clazz = block.getClass();
            var getter = this.getters.get(clazz);
            if (getter != null) {
                var type = getter.get(block, level, pos, state);
                if (type != null) {
                    source.sendSuccess(() -> Component.translatable("commands.dragonmounts.type.block.get", pos.getX(), pos.getY(), pos.getZ(), type.getName()), true);
                    return 1;
                }
            }
            if (block instanceof DragonTypified) {
                source.sendSuccess(() -> Component.translatable("commands.dragonmounts.type.block.get", pos.getX(), pos.getY(), pos.getZ(), ((DragonTypified) block).getDragonType().getName()), true);
                return 1;
            }
            source.sendFailure(createClassCastException(clazz, DragonTypified.class));
            return 0;
        }
    }

    public static class EntityHandler extends CommandHandler<Entity> {
        @Override
        protected Entity getArgument(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            return EntityArgument.getEntity(context, "target");
        }

        @Override
        protected int setType(CommandContext<CommandSourceStack> context, Entity entity, DragonType type) {
            var source = context.getSource();
            if (entity instanceof DragonTypified.Mutable) {
                ((DragonTypified.Mutable) entity).setDragonType(type);
                source.sendSuccess(() -> Component.translatable("commands.dragonmounts.type.entity.set", entity.getDisplayName(), type.getName()), true);
                return 1;
            }
            source.sendFailure(createClassCastException(entity, DragonTypified.Mutable.class));
            return 0;
        }

        @Override
        protected int getType(CommandContext<CommandSourceStack> context, Entity entity) {
            var source = context.getSource();
            if (entity instanceof DragonTypified) {
                source.sendSuccess(() -> Component.translatable("commands.dragonmounts.type.entity.get", entity.getDisplayName(), ((DragonTypified) entity).getDragonType().getName()), true);
                return 1;
            }
            source.sendFailure(createClassCastException(entity, DragonTypified.class));
            return 0;
        }
    }

    public static final BlockHandler BLOCK_HANDLER = new BlockHandler();
    public static final EntityHandler ENTITY_HANDLER = new EntityHandler();

    static {
        BLOCK_HANDLER.bind(DragonEggBlock.class, ($_, __, $, $$) -> DragonTypes.ENDER);
        BLOCK_HANDLER.bind(SkullBlock.class, (block, __, $, $$) -> block == Blocks.DRAGON_HEAD ? DragonTypes.ENDER : null);
        BLOCK_HANDLER.bind(WallSkullBlock.class, (block, __, $, $$) -> block == Blocks.DRAGON_WALL_HEAD ? DragonTypes.ENDER : null);
        BLOCK_HANDLER.bind(DragonEggBlock.class, BlockHandler.SETTER_DRAGON_EEG);
        BLOCK_HANDLER.bind(HatchableDragonEggBlock.class, BlockHandler.SETTER_DRAGON_EEG);
        BLOCK_HANDLER.bind(DragonHeadBlock.class, BlockHandler.SETTER_DRAGON_HEAD);
        BLOCK_HANDLER.bind(SkullBlock.class, BlockHandler.SETTER_DRAGON_HEAD);
        BLOCK_HANDLER.bind(DragonHeadWallBlock.class, BlockHandler.SETTER_DRAGON_HEAD_WALL);
        BLOCK_HANDLER.bind(WallSkullBlock.class, BlockHandler.SETTER_DRAGON_HEAD_WALL);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register(Predicate<CommandSourceStack> permission) {
        return Commands.literal("type")
                .then(Commands.literal("block").then(BLOCK_HANDLER.load(Commands.argument("pos", BlockPosArgument.blockPos()), permission)))
                .then(Commands.literal("entity").then(ENTITY_HANDLER.load(Commands.argument("target", EntityArgument.entity()), permission)));
    }
}
