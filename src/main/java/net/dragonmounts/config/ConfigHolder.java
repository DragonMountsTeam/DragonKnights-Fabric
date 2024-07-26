package net.dragonmounts.config;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public abstract class ConfigHolder {
    private static final Logger LOGGER = LogManager.getLogger();
    private final File source;
    public final BooleanEntry debug;

    public ConfigHolder(File source, boolean debug) {
        this.source = source;
        this.debug = new BooleanEntry("Debug", debug);
    }

    public final void load() {
        Util.getIoWorkerExecutor().execute(this::loadSync);
    }

    public final void save() {
        Util.getIoWorkerExecutor().execute(this::saveSync);
    }

    public final synchronized void loadSync() {
        try {
            if (this.source.exists()) {
                try {
                    this.read(NbtIo.readCompressed(this.source));
                } catch (Exception exception) {
                    LOGGER.error("Exception reading {}", this.source, exception);
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Exception reading {}", this.source, exception);
        }
    }

    public final synchronized void saveSync() {
        try {
            if (this.source.exists()) {
                NbtIo.writeCompressed(this.write(NbtIo.readCompressed(this.source)), this.source);
            } else {
                File parent = this.source.getParentFile();
                if ((parent.exists() || parent.mkdir()) && this.source.createNewFile()) {
                    NbtIo.writeCompressed(this.write(new NbtCompound()), this.source);
                } else LOGGER.error("Failed to create {}", this.source);
            }
        } catch (Exception exception) {
            LOGGER.error("Exception writing {}", this.source, exception);
        }
    }

    protected abstract void read(NbtCompound tag);

    protected abstract NbtCompound write(NbtCompound tag);
}
