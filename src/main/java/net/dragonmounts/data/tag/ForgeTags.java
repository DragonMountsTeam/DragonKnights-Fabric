package net.dragonmounts.data.tag;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class ForgeTags {
    public static class Item {
        public static final Tag<net.minecraft.item.Item> CHESTS_WOODEN = TagRegistry.item(new Identifier("forge", "chests/wooden"));
    }
}
