package net.dragonmounts.api;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DragonArmorMaterial implements Holder<ArmorMaterial> {
    public final Holder<ArmorMaterial> host;
    public final int defence;
    private ArmorMaterial modified;

    public DragonArmorMaterial(Holder<ArmorMaterial> host, int defence) {
        this.host = host;
        this.defence = defence;
    }

    @Override
    public @NotNull ArmorMaterial value() {
        if (this.modified == null) {
            var host = this.host.value();
            var map = host.defense();
            map.put(ArmorItem.Type.BODY, this.defence);
            this.modified = new ArmorMaterial(map, host.enchantmentValue(), host.equipSound(), host.repairIngredient(), host.layers(), 0, 0);
        }
        return this.modified;
    }

    @Override
    public boolean isBound() {
        return this.host.isBound();
    }

    @Override
    public boolean is(ResourceLocation identifier) {
        return this.host.is(identifier);
    }

    @Override
    public boolean is(ResourceKey<ArmorMaterial> key) {
        return this.host.is(key);
    }

    @Override
    public boolean is(Predicate<ResourceKey<ArmorMaterial>> predicate) {
        return this.host.is(predicate);
    }

    @Override
    public boolean is(TagKey<ArmorMaterial> tag) {
        return this.host.is(tag);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean is(Holder<ArmorMaterial> holder) {
        return this.host.is(holder);
    }

    @Override
    public @NotNull Stream<TagKey<ArmorMaterial>> tags() {
        return this.host.tags();
    }

    @Override
    public @NotNull Either<ResourceKey<ArmorMaterial>, ArmorMaterial> unwrap() {
        return this.host.unwrap();
    }

    @Override
    public @NotNull Optional<ResourceKey<ArmorMaterial>> unwrapKey() {
        return this.host.unwrapKey();
    }

    @Override
    public @NotNull Kind kind() {
        return this.host.kind();
    }

    @Override
    public boolean canSerializeIn(HolderOwner<ArmorMaterial> owner) {
        return this.host.canSerializeIn(owner);
    }
}
