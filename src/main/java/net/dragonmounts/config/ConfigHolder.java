package net.dragonmounts.config;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ConfigHolder {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Path source;
    public final BooleanEntry debug;

    public ConfigHolder(Path source, boolean debug) {
        this.source = source;
        this.debug = new BooleanEntry("Debug", debug);
    }

    public final void load() {
        Util.ioPool().execute(this::loadSync);
    }

    public final void save() {
        Util.ioPool().execute(this::saveSync);
    }

    public final synchronized void loadSync() {
        var source = this.source;
        try {
            if (Files.isRegularFile(source)) {
                try {
                    this.read(NbtIo.readCompressed(source, NbtAccounter.unlimitedHeap()));
                } catch (Exception exception) {
                    LOGGER.error("Exception reading {}", source, exception);
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Exception reading {}", source, exception);
        }
    }

    public final synchronized void saveSync() {
        var source = this.source;
        try {
            if (Files.isRegularFile(source)) {
                NbtIo.writeCompressed(this.write(NbtIo.readCompressed(source, NbtAccounter.unlimitedHeap())), source);
            } else if (Files.notExists(source)) {
                Files.createDirectories(source.getParent());
                NbtIo.writeCompressed(this.write(new CompoundTag()), source);
            }
        } catch (Exception exception) {
            LOGGER.error("Exception writing {}", source, exception);
        }
    }

    protected abstract void read(CompoundTag tag);

    protected abstract CompoundTag write(CompoundTag tag);
}
