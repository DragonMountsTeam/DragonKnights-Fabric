package net.dragonmounts.config;

import net.dragonmounts.DragonMounts;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.nbt.NbtCompound;

public class ClientConfig extends ConfigHolder {
    public static final ClientConfig INSTANCE = new ClientConfig(DragonMounts.MOD_ID);
    public final DoubleEntry camera_distance = new DoubleEntry("CameraDistance", "camera_distance", 20D);
    public final DoubleEntry camera_offset = new DoubleEntry("CameraOffset", "camera_offset", 0D);
    public final BooleanEntry converge_pitch_angle = new BooleanEntry("ConvergePitchAngle", "converge_pitch_angle", true);
    public final BooleanEntry converge_yaw_angle = new BooleanEntry("ConvergeYawAngle", "converge_yaw_angle", true);
    public final BooleanEntry hover_animation = new BooleanEntry("HoverAnimation", "hover_animation", true);
    public final BooleanEntry redirect_inventory = new BooleanEntry("RedirectInventory", "redirect_inventory", true);
    public final BooleanEntry toggle_descent = new BooleanEntry("ToggleDescent", "toggle_descent", false);

    protected ClientConfig(String identifier) {
        super(FabricLoaderImpl.INSTANCE.getConfigDir().resolve(identifier).resolve("server.dat").toFile(), false);
        this.load();
    }

    @Override
    protected void read(NbtCompound tag) {
        this.debug.read(tag);
        this.camera_distance.read(tag);
        this.camera_offset.read(tag);
        this.converge_pitch_angle.read(tag);
        this.converge_yaw_angle.read(tag);
        this.hover_animation.read(tag);
        this.redirect_inventory.read(tag);
        this.toggle_descent.read(tag);
    }

    @Override
    protected NbtCompound write(NbtCompound tag) {
        this.debug.save(tag);
        this.camera_distance.save(tag);
        this.camera_offset.save(tag);
        this.converge_pitch_angle.save(tag);
        this.converge_yaw_angle.save(tag);
        this.hover_animation.save(tag);
        this.redirect_inventory.save(tag);
        this.toggle_descent.save(tag);
        return tag;
    }

    public static void init() {}
}
