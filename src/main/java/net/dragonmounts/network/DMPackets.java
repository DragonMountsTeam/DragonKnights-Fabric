package net.dragonmounts.network;

import net.minecraft.util.Identifier;

import static net.dragonmounts.DragonMounts.makeId;

public class DMPackets {
    public static final Identifier ARMOR_RIPOSTE_PACKET_ID = makeId("armor_riposte");
    public static final Identifier INIT_COOLDOWN_PACKET_ID = makeId("init_cd");
    public static final Identifier SYNC_COOLDOWN_PACKET_ID = makeId("sync_cd");
    public static final Identifier SHAKE_DRAGON_EGG_PACKET_ID = makeId("shake_egg");
    public static final Identifier SYNC_DRAGON_AGE_PACKET_ID = makeId("sync_dragon");
    public static final Identifier FEED_DRAGON_PACKET_ID = makeId("feed_dragon");
    public static final Identifier RIDE_DRAGON_PACKET_ID = makeId("ride_dragon");
    public static final Identifier SYNC_EGG_AGE_PACKET_ID = makeId("sync_egg");
}
