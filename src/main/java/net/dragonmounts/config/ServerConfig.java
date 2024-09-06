package net.dragonmounts.config;

import net.dragonmounts.DragonMounts;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.nbt.CompoundTag;

public class ServerConfig extends ConfigHolder {
    public static final ServerConfig INSTANCE = new ServerConfig(DragonMounts.MOD_ID);

    protected ServerConfig(String identifier) {
        super(FabricLoaderImpl.INSTANCE.getConfigDir().resolve(identifier).resolve("server.dat"), false);
        this.load();
    }

    @Override
    protected void read(CompoundTag tag) {
        this.debug.read(tag);
    }

    @Override
    protected CompoundTag write(CompoundTag tag) {
        this.debug.save(tag);
        return tag;
    }

    public static void init() {}
}
