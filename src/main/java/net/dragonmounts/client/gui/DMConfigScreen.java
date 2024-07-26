package net.dragonmounts.client.gui;

import net.dragonmounts.config.ClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.TranslatableText;

import java.util.List;

import static net.dragonmounts.client.gui.LazyBooleanConfigOption.DEFAULT_STRINGIFY;
import static net.dragonmounts.client.gui.LazyBooleanConfigOption.TOGGLE_STRINGIFY;
import static net.minecraft.client.gui.screen.option.GameOptionsScreen.getHoveredButtonTooltip;


public class DMConfigScreen extends Screen {
    protected final Screen parent;
    protected ButtonListWidget list;
    protected LazyBooleanConfigOption debug;
    protected LazySliderConfigOption cameraDistance;
    protected LazySliderConfigOption cameraOffset;
    protected LazyBooleanConfigOption convergePitch;
    protected LazyBooleanConfigOption convergeYaw;
    protected LazyBooleanConfigOption hoverAnimation;
    protected LazyBooleanConfigOption redirectInventory;
    protected LazyBooleanConfigOption toggleDescent;

    public DMConfigScreen(MinecraftClient minecraft, Screen parent) {
        super(new TranslatableText("options.dragonmounts.config"));
        this.parent = parent;
        this.client = minecraft;
    }

    protected void init() {
        ClientConfig config = ClientConfig.INSTANCE;
        this.debug = new LazyBooleanConfigOption("options.dragonmounts.debug", config.debug, DEFAULT_STRINGIFY, null);
        TranslatableText cameraNote = new TranslatableText("options.dragonmounts.camera.note");
        this.cameraDistance = new LazySliderConfigOption("options.dragonmounts.camera_distance", config.camera_distance, 0.0D, 64.0D, 0.25D, LazySliderConfigOption.STRINGIFY_X_2F, cameraNote);
        this.cameraOffset = new LazySliderConfigOption("options.dragonmounts.camera_offset", config.camera_offset, -16.0D, 16.0D, 0.25D, LazySliderConfigOption.STRINGIFY_X_2F, cameraNote);
        this.convergePitch = new LazyBooleanConfigOption("options.dragonmounts.converge_pitch_angle", config.converge_pitch_angle, DEFAULT_STRINGIFY, null);
        this.convergeYaw = new LazyBooleanConfigOption("options.dragonmounts.converge_yaw_angle", config.converge_yaw_angle, DEFAULT_STRINGIFY, null);
        this.hoverAnimation = new LazyBooleanConfigOption("options.dragonmounts.hover_animation", config.hover_animation, DEFAULT_STRINGIFY, null);
        this.redirectInventory = new LazyBooleanConfigOption("options.dragonmounts.redirect_inventory", config.redirect_inventory, DEFAULT_STRINGIFY, new TranslatableText("options.dragonmounts.redirect_inventory.note"));
        this.toggleDescent = new LazyBooleanConfigOption("key.dragonmounts.descent", config.toggle_descent, TOGGLE_STRINGIFY, null);
        this.list = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        this.list.addSingleOptionEntry(this.cameraDistance);
        this.list.addSingleOptionEntry(this.cameraOffset);
        this.list.addOptionEntry(this.debug, this.toggleDescent);
        this.list.addOptionEntry(this.convergePitch, this.convergeYaw);
        this.list.addOptionEntry(this.hoverAnimation, this.redirectInventory);
        this.children.add(this.list);
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, $ -> this.onClose()));
    }

    @Override
    public void onClose() {
        //noinspection DataFlowIssue
        this.client.openScreen(this.parent);
    }

    @Override
    public void removed() {
        this.debug.save();
        this.cameraDistance.save();
        this.cameraOffset.save();
        this.convergePitch.save();
        this.convergeYaw.save();
        this.hoverAnimation.save();
        this.redirectInventory.save();
        this.toggleDescent.save();
        ClientConfig.INSTANCE.save();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float ticks) {
        this.renderBackground(matrices, 0);
        this.list.render(matrices, mouseX, mouseY, ticks);
        drawCenteredText(matrices, this.textRenderer, this.title.copy(), this.width >> 1, 20, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, ticks);
        if (this.isDragging()) return;
        List<OrderedText> list = getHoveredButtonTooltip(this.list, mouseX, mouseY);
        if (list != null) {
            this.renderOrderedTooltip(matrices, list, mouseX, mouseY);
        }
    }
}
