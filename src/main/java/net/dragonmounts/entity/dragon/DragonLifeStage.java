package net.dragonmounts.entity.dragon;

import com.google.common.base.Functions;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static net.dragonmounts.command.StageCommand.egg;
import static net.dragonmounts.command.StageCommand.set;
import static net.dragonmounts.entity.dragon.TameableDragonEntity.STAGE_MODIFIER_ID;
import static net.dragonmounts.util.TimeUtil.TICKS_PER_GAME_HOUR;

public enum DragonLifeStage implements StringRepresentable {
    NEWBORN(48 * TICKS_PER_GAME_HOUR, 0.04F, 0.09F),
    INFANT(24 * TICKS_PER_GAME_HOUR, 0.10F, 0.18F),
    PREJUVENILE(32 * TICKS_PER_GAME_HOUR, 0.19F, 0.60F),
    JUVENILE(60 * TICKS_PER_GAME_HOUR, 0.61F, 0.99F),
    ADULT(0, 1.00F, 1.00F);
    public static final String DATA_PARAMETER_KEY = "LifeStage";
    public static final String EGG_TRANSLATION_KEY = "dragon.life_stage.egg";
    private static final DragonLifeStage[] VALUES;
    private static final Map<String, DragonLifeStage> BY_NAME;
    public final AttributeModifier modifier;
    public final int duration;
    public final float startSize;
    public final float endSize;
    public final String name;
    public final String text;

    static {
        BY_NAME = Arrays.stream(VALUES = values()).collect(Collectors.toMap(DragonLifeStage::getSerializedName, Functions.identity()));
    }

    DragonLifeStage(int duration, float startSize, float endSize) {
        this.duration = duration;
        this.startSize = startSize;
        this.endSize = endSize;
        this.text = "dragon.life_stage." + (this.name = this.name().toLowerCase());
        this.modifier = new AttributeModifier(STAGE_MODIFIER_ID, Math.max(getSizeAverage(this), 0.1F), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    public Component getText() {
        return Component.translatable(this.text);
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public static DragonLifeStage byId(int id) {
        return id < 0 || id >= VALUES.length ? DragonLifeStage.ADULT : VALUES[id];
    }

    public static DragonLifeStage byName(String name) {
        return BY_NAME.getOrDefault(name, ADULT);
    }

    public static float getSize(DragonLifeStage stage, int age) {
        return stage.duration == 0 ? 1.00F : Mth.lerp(Math.abs(age) / (float) stage.duration, stage.endSize, stage.startSize);
    }

    public static float getSizeAverage(DragonLifeStage stage) {
        return 0.5F * (stage.endSize + stage.startSize);
    }

    public static RequiredArgumentBuilder<CommandSourceStack, EntitySelector> applyValues(RequiredArgumentBuilder<CommandSourceStack, EntitySelector> builder) {
        builder.then(Commands.literal("egg").executes(context -> egg(context.getSource(), EntityArgument.getEntity(context, "target"))));
        for (var stage : VALUES) {
            builder.then(Commands.literal(stage.name).executes(context -> set(context.getSource(), EntityArgument.getEntity(context, "target"), stage)));
        }
        return builder;
    }
}
