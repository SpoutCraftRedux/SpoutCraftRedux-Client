package org.spoutcraftredux.client.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spoutcraftredux.client.gui.controls.ControlsScreen;

@Mixin(Gui.class)
public abstract class GuiMixin {
    /** Opens the SpoutCraft Redux controls screen wherever Minecraft would open its key binds screen. */
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen spoutcraftredux$replaceKeyBinds(Screen screen) {
        if (screen instanceof KeyBindsScreen keyBinds) {
            return new ControlsScreen(((OptionsSubScreenAccessor) keyBinds).spoutcraftredux$getLastScreen());
        }
        return screen;
    }
}
