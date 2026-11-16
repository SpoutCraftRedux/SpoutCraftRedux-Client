package org.spoutcraftredux.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spoutcraftredux.client.controls.BindingSettings;
import org.spoutcraftredux.client.controls.KeyCombo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract boolean handleDebugKeys(KeyEvent event);

    @Inject(method = "keyPress", at = @At("RETURN"))
    private void spoutcraftredux$clearInput(long window, int action, KeyEvent event, CallbackInfo ci) {
        KeyCombo.setCurrentInput(null);
    }

    /** Runs F3 actions that the player set to work without F3. */
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void spoutcraftredux$standaloneDebugKeys(long window, int action, KeyEvent event, CallbackInfo ci) {
        KeyCombo.setCurrentInput(event);
        if (action != InputConstants.REPEAT) {
            KeyCombo.onKey(InputConstants.getKey(event), action == InputConstants.PRESS);
        }
        if (action != InputConstants.PRESS || this.minecraft.gui.screen() != null || BindingSettings.isDebugModifierHeld()) {
            return;
        }
        for (KeyMapping mapping : this.minecraft.options.keyMappings) {
            if (BindingSettings.isDebug(mapping) && BindingSettings.isStandalone(mapping) && mapping.matches(event)) {
                this.handleDebugKeys(event);
                return;
            }
        }
    }

    /** F3 actions that need Ctrl, Shift or Alt only run with exactly those held. */
    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void spoutcraftredux$debugModifiers(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        boolean matched = false;
        for (KeyMapping mapping : this.minecraft.options.keyMappings) {
            if (BindingSettings.isDebug(mapping) && mapping.matches(event)) {
                matched = true;
                if (BindingSettings.allows(mapping)) {
                    return;
                }
            }
        }
        if (matched) {
            cir.setReturnValue(false);
        }
    }
}
