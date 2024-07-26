package net.dragonmounts.config;

import net.dragonmounts.DragonMounts;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.nbt.NbtCompound;

public class ServerConfig extends ConfigHolder {
    public static final ServerConfig INSTANCE = new ServerConfig(DragonMounts.MOD_ID);

    protected ServerConfig(String identifier) {
        super(FabricLoaderImpl.INSTANCE.getConfigDir().resolve(identifier).resolve("server.dat").toFile(), false);
        this.load();
    }

    @Override
    protected void read(NbtCompound tag) {
        this.debug.read(tag);
    }

    @Override
    protected NbtCompound write(NbtCompound tag) {
        this.debug.save(tag);
        return tag;
    }

    public static void init() {}
}
