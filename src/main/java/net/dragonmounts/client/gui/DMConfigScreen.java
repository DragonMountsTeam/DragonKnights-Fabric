package net.dragonmounts.client.gui;

import net.dragonmounts.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import static net.dragonmounts.client.gui.LazyBooleanConfigOption.TOGGLE_STRINGIFIER;
import static net.dragonmounts.client.gui.LazyFloatConfigOption.X_2F_STRINGIFIER;
import static net.minecraft.client.OptionInstance.BOOLEAN_TO_STRING;

public class DMConfigScreen extends OptionsSubScreen {
    protected static final LazyBooleanConfigOption DEBUG;
    protected static final LazyFloatConfigOption CAMERA_DISTANCE;
    protected static final LazyFloatConfigOption CAMERA_OFFSET;
    protected static final LazyBooleanConfigOption CONVERGE_PITCH;
    protected static final LazyBooleanConfigOption CONVERGE_YAW;
    protected static final LazyBooleanConfigOption HOVER_ANIMATION;
    protected static final LazyBooleanConfigOption REDIRECT_INVENTORY;
    protected static final LazyBooleanConfigOption TOGGLE_DESCENT;

    static {
        ClientConfig config = ClientConfig.INSTANCE;
        DEBUG = new LazyBooleanConfigOption("options.dragonmounts.debug", config.debug, null, BOOLEAN_TO_STRING);
        Component cameraNote = Component.translatable("options.dragonmounts.camera.note");
        CAMERA_DISTANCE = new LazyFloatConfigOption("options.dragonmounts.camera_distance", config.camera_distance, new LazyFloatConfigOption.Range(0.0F, 64.0F, 0.25F), cameraNote, X_2F_STRINGIFIER);
        CAMERA_OFFSET = new LazyFloatConfigOption("options.dragonmounts.camera_offset", config.camera_offset, new LazyFloatConfigOption.Range(-16.0F, 16.0F, 0.25F), cameraNote, X_2F_STRINGIFIER);
        CONVERGE_PITCH = new LazyBooleanConfigOption("options.dragonmounts.converge_pitch_angle", config.converge_pitch_angle, null, BOOLEAN_TO_STRING);
        CONVERGE_YAW = new LazyBooleanConfigOption("options.dragonmounts.converge_yaw_angle", config.converge_yaw_angle, null, BOOLEAN_TO_STRING);
        HOVER_ANIMATION = new LazyBooleanConfigOption("options.dragonmounts.hover_animation", config.hover_animation, null, BOOLEAN_TO_STRING);
        REDIRECT_INVENTORY = new LazyBooleanConfigOption("options.dragonmounts.redirect_inventory", config.redirect_inventory, Component.translatable("options.dragonmounts.redirect_inventory.note"), BOOLEAN_TO_STRING);
        TOGGLE_DESCENT = new LazyBooleanConfigOption("key.dragonmounts.descent", config.toggle_descent, null, TOGGLE_STRINGIFIER);
    }

    public DMConfigScreen(Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, Component.translatable("options.dragonmounts.config"));
    }

    @Override
    protected void addOptions() {
        assert this.list != null;
        this.list.addBig(CAMERA_DISTANCE.makeInstance());
        this.list.addBig(CAMERA_OFFSET.makeInstance());
        this.list.addSmall(
                DEBUG.makeInstance(),
                TOGGLE_DESCENT.makeInstance(),
                CONVERGE_PITCH.makeInstance(),
                CONVERGE_YAW.makeInstance(),
                HOVER_ANIMATION.makeInstance(),
                REDIRECT_INVENTORY.makeInstance()
        );
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void removed() {
        ClientConfig.INSTANCE.save();
    }

    @Override
    protected void setInitialFocus() {}
}
