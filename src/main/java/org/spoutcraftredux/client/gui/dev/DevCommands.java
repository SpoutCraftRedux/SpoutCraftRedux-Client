package org.spoutcraftredux.client.gui.dev;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

/** Client commands for checking the GUI by hand. Registered only in a development environment. */
public final class DevCommands {
    private DevCommands() {
    }

    public static void register() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return;
        }
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommands.literal("spoutcraftredux-client").then(ClientCommands.literal("widgets").executes(context -> {
                Minecraft minecraft = context.getSource().getClient();
                // Run after the chat screen has closed, or it would close this screen too
                minecraft.execute(() -> minecraft.gui.setScreen(new WidgetDemoScreen()));
                return 1;
            }))
        ));
    }
}
