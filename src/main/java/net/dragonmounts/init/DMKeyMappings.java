package net.dragonmounts.init;

import net.dragonmounts.config.ClientConfig;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.ToggleKeyMapping;
import org.lwjgl.glfw.GLFW;

public class DMKeyMappings {
    public static final ToggleKeyMapping DESCENT = new ToggleKeyMapping(
            "key.dragonmounts.descent",
            GLFW.GLFW_KEY_Z,
            "key.categories.movement",
            ClientConfig.INSTANCE.toggle_descent::get
    );

    public static void register() {
        KeyBindingHelper.registerKeyBinding(DESCENT);
    }
}
