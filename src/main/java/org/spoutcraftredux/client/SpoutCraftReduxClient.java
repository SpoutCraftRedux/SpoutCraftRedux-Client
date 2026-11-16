package org.spoutcraftredux.client;

import com.mojang.logging.LogUtils;
import java.util.OptionalInt;
import java.util.Set;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.spoutcraftredux.client.controls.BindingSettings;
import org.spoutcraftredux.client.controls.ServerKeyBindings;
import org.spoutcraftredux.client.controls.Shortcuts;
import org.spoutcraftredux.client.gui.dev.DevCommands;
import org.spoutcraftredux.client.gui.server.AssetCache;
import org.spoutcraftredux.client.gui.server.ServerGui;
import org.spoutcraftredux.client.network.NotificationHandler;
import org.spoutcraftredux.client.network.RawPayload;
import org.spoutcraftredux.client.network.SpoutCraftReduxChannels;
import org.spoutcraftredux.protocol.Capabilities;
import org.spoutcraftredux.protocol.ProtocolException;
import org.spoutcraftredux.protocol.SpoutCraftReduxProtocol;
import org.spoutcraftredux.protocol.message.Hello;
import org.spoutcraftredux.protocol.message.AssetData;
import org.spoutcraftredux.protocol.message.HelloAck;
import org.spoutcraftredux.protocol.message.ScreenClose;
import org.spoutcraftredux.protocol.message.ScreenOpen;
import org.spoutcraftredux.protocol.message.WidgetRemove;
import org.spoutcraftredux.protocol.message.WidgetUpdate;
import org.slf4j.Logger;

public final class SpoutCraftReduxClient implements ClientModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();
    /** Features this build implements; grows as phases land. */
    private static final Set<String> CLIENT_CAPABILITIES = Set.of(Capabilities.NOTIFICATION, Capabilities.KEYBIND, Capabilities.UI, Capabilities.HUD, Capabilities.ASSET);

    @Override
    public void onInitializeClient() {
        SpoutCraftReduxChannels.register();
        DevCommands.register();
        Shortcuts.load();
        BindingSettings.load();
        ClientTickEvents.END_CLIENT_TICK.register(Shortcuts::tick);

        ClientConfigurationNetworking.registerGlobalReceiver(SpoutCraftReduxChannels.HELLO, (payload, context) -> {
            Hello hello;
            try {
                hello = Hello.decode(payload.data());
            } catch (ProtocolException e) {
                LOGGER.warn("Ignoring malformed SpoutCraft Redux hello: {}", e.getMessage());
                return;
            }
            OptionalInt version = SpoutCraftReduxProtocol.negotiate(
                hello.protocolMin(), hello.protocolMax(),
                SpoutCraftReduxProtocol.MIN_SUPPORTED_VERSION, SpoutCraftReduxProtocol.VERSION
            );
            if (version.isEmpty()) {
                LOGGER.warn("SpoutCraft Redux server speaks protocol {}..{}, this client {}..{}; staying vanilla",
                    hello.protocolMin(), hello.protocolMax(), SpoutCraftReduxProtocol.MIN_SUPPORTED_VERSION, SpoutCraftReduxProtocol.VERSION);
                return;
            }
            SpoutCraftReduxSession.start(version.getAsInt(), hello.capabilities());
            HelloAck ack = new HelloAck(version.getAsInt(), CLIENT_CAPABILITIES, modVersion());
            context.responseSender().sendPacket(new RawPayload(SpoutCraftReduxChannels.HELLO_ACK, ack.encode()));
            LOGGER.info("Connected to a SpoutCraft Redux server (protocol {}, capabilities {})", version.getAsInt(), hello.capabilities());
        });

        ClientPlayNetworking.registerGlobalReceiver(SpoutCraftReduxChannels.KEY_BINDINGS, (payload, context) -> {
            try {
                ServerKeyBindings.receive(org.spoutcraftredux.protocol.message.KeyBindings.decode(payload.data()),
                    json -> NotificationHandler.parse(context.client(), json));
            } catch (ProtocolException e) {
                LOGGER.warn("Ignoring malformed SpoutCraft Redux key bindings: {}", e.getMessage());
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(ServerKeyBindings::tick);

        ServerGui.register();
        receive(SpoutCraftReduxChannels.SCREEN_OPEN, (minecraft, data) -> ServerGui.open(minecraft, ScreenOpen.decode(data)));
        receive(SpoutCraftReduxChannels.SCREEN_CLOSE, (minecraft, data) -> ServerGui.close(ScreenClose.decode(data)));
        receive(SpoutCraftReduxChannels.WIDGET_UPDATE, (minecraft, data) -> ServerGui.update(WidgetUpdate.decode(data)));
        receive(SpoutCraftReduxChannels.ASSET_DATA, (minecraft, data) -> AssetCache.receive(AssetData.decode(data)));
        receive(SpoutCraftReduxChannels.WIDGET_REMOVE, (minecraft, data) -> ServerGui.remove(WidgetRemove.decode(data)));

        ClientPlayNetworking.registerGlobalReceiver(SpoutCraftReduxChannels.NOTIFICATION,
            (payload, context) -> NotificationHandler.handle(context.client(), payload.data()));

        ClientConfigurationConnectionEvents.INIT.register((handler, client) -> SpoutCraftReduxSession.end());
        ClientConfigurationConnectionEvents.DISCONNECT.register((handler, client) -> SpoutCraftReduxSession.end());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            SpoutCraftReduxSession.end();
            ServerKeyBindings.clear();
            ServerGui.clear();
            AssetCache.clearRequests();
        });
    }

    /** Registers a play-phase receiver that drops malformed messages instead of disconnecting. */
    private static void receive(net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<RawPayload> type,
                                java.util.function.BiConsumer<net.minecraft.client.Minecraft, byte[]> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
            try {
                handler.accept(context.client(), payload.data());
            } catch (ProtocolException e) {
                LOGGER.warn("Ignoring malformed {}: {}", type.id(), e.getMessage());
            }
        });
    }

    private static String modVersion() {
        return FabricLoader.getInstance().getModContainer("spoutcraftredux")
            .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
    }
}
