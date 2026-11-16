package org.spoutcraftredux.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spoutcraftredux.client.SpoutCraftReduxClient;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    /** Clears the server's state whenever the client leaves a world, however it leaves. */
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At("HEAD"))
    private void spoutcraftredux$endSession(Screen screen, boolean keepResourcePacks, boolean transferring, CallbackInfo ci) {
        SpoutCraftReduxClient.endSession();
    }
}
