package org.spoutcraftredux.client.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spoutcraftredux.client.controls.BindingSettings;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {
    /** A binding that needs Ctrl, Shift or Alt stays up unless exactly those are held. Releasing always works. */
    @Inject(method = "setDown", at = @At("HEAD"), cancellable = true)
    private void spoutcraftredux$checkModifiers(boolean down, CallbackInfo ci) {
        if (down && !BindingSettings.allows((KeyMapping) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "lambda$click$0", at = @At("HEAD"), cancellable = true)
    private static void spoutcraftredux$checkModifiersOnClick(KeyMapping mapping, CallbackInfo ci) {
        if (!BindingSettings.allows(mapping)) {
            ci.cancel();
        }
    }
}
