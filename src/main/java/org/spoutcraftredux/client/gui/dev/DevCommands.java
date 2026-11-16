package org.spoutcraftredux.client.gui.dev;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

/** Client commands for checking the GUI by hand. Registered only in a development environment. */
public final class DevCommands {
    private static @Nullable Screen pending;

    private DevCommands() {
    }

    public static void register() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return;
        }
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommands.literal("spoutcraftredux-client").then(ClientCommands.literal("widgets").executes(context -> {
                // The chat screen closes after the command runs, so open on the next tick
                pending = new WidgetDemoScreen();
                return 1;
            }))
        ));
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if (pending != null) {
                minecraft.gui.setScreen(pending);
                pending = null;
            }
        });
    }
}
