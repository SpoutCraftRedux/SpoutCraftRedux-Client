package org.spoutcraftredux.client.mixin;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spoutcraftredux.client.controls.KeyCombo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    /** Mouse bindings read Ctrl, Shift and Alt from the click itself. */
    @Inject(method = "onButton", at = @At("HEAD"))
    private void spoutcraftredux$setInput(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        KeyCombo.setCurrentInput(info);
    }

    @Inject(method = "onButton", at = @At("RETURN"))
    private void spoutcraftredux$clearInput(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        KeyCombo.setCurrentInput(null);
    }
}
