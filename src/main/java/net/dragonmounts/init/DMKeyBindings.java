package net.dragonmounts.init;

import net.dragonmounts.config.ClientConfig;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import org.lwjgl.glfw.GLFW;

public class DMKeyBindings {
    public static final KeyBinding DESCENT = new StickyKeyBinding(
            "key.dragonmounts.descent",
            GLFW.GLFW_KEY_Z,
            "key.categories.movement",
            ClientConfig.INSTANCE.toggle_descent::get
    );

    public static void register() {
        KeyBindingRegistryImpl.registerKeyBinding(DESCENT);
    }
}
