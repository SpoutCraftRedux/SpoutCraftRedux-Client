package org.spoutcraftredux.client.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.spoutcraftredux.protocol.Limits;
import org.spoutcraftredux.protocol.message.Hello;
import org.spoutcraftredux.protocol.message.AssetData;
import org.spoutcraftredux.protocol.message.AssetRequest;
import org.spoutcraftredux.protocol.message.ControlEvent;
import org.spoutcraftredux.protocol.message.HelloAck;
import org.spoutcraftredux.protocol.message.ScreenClose;
import org.spoutcraftredux.protocol.message.ScreenOpen;
import org.spoutcraftredux.protocol.message.WidgetRemove;
import org.spoutcraftredux.protocol.message.WidgetUpdate;
import org.spoutcraftredux.protocol.message.KeyBindingPressed;
import org.spoutcraftredux.protocol.message.KeyBindings;
import org.spoutcraftredux.protocol.message.Notification;

public final class SpoutCraftReduxChannels {
    public static final Type<RawPayload> HELLO = RawPayload.type(Hello.CHANNEL);
    public static final Type<RawPayload> HELLO_ACK = RawPayload.type(HelloAck.CHANNEL);
    public static final Type<RawPayload> NOTIFICATION = RawPayload.type(Notification.CHANNEL);
    public static final Type<RawPayload> KEY_BINDINGS = RawPayload.type(KeyBindings.CHANNEL);
    public static final Type<RawPayload> KEY_BINDING_PRESSED = RawPayload.type(KeyBindingPressed.CHANNEL);
    public static final Type<RawPayload> SCREEN_OPEN = RawPayload.type(ScreenOpen.CHANNEL);
    public static final Type<RawPayload> SCREEN_CLOSE = RawPayload.type(ScreenClose.CLIENTBOUND);
    public static final Type<RawPayload> SCREEN_CLOSED = RawPayload.type(ScreenClose.SERVERBOUND);
    public static final Type<RawPayload> WIDGET_UPDATE = RawPayload.type(WidgetUpdate.CHANNEL);
    public static final Type<RawPayload> WIDGET_REMOVE = RawPayload.type(WidgetRemove.CHANNEL);
    public static final Type<RawPayload> CONTROL_EVENT = RawPayload.type(ControlEvent.CHANNEL);
    public static final Type<RawPayload> ASSET_REQUEST = RawPayload.type(AssetRequest.CHANNEL);
    public static final Type<RawPayload> ASSET_DATA = RawPayload.type(AssetData.CHANNEL);

    private SpoutCraftReduxChannels() {
    }

    public static void register() {
        PayloadTypeRegistry.clientboundConfiguration().register(HELLO, RawPayload.codec(HELLO, Limits.MAX_CLIENTBOUND_BYTES));
        PayloadTypeRegistry.serverboundConfiguration().register(HELLO_ACK, RawPayload.codec(HELLO_ACK, Limits.MAX_SERVERBOUND_BYTES));
        PayloadTypeRegistry.clientboundPlay().register(NOTIFICATION, RawPayload.codec(NOTIFICATION, Limits.MAX_CLIENTBOUND_BYTES));
        PayloadTypeRegistry.clientboundPlay().register(KEY_BINDINGS, RawPayload.codec(KEY_BINDINGS, Limits.MAX_CLIENTBOUND_BYTES));
        PayloadTypeRegistry.serverboundPlay().register(KEY_BINDING_PRESSED, RawPayload.codec(KEY_BINDING_PRESSED, Limits.MAX_SERVERBOUND_BYTES));
        for (Type<RawPayload> type : java.util.List.of(SCREEN_OPEN, SCREEN_CLOSE, WIDGET_UPDATE, WIDGET_REMOVE)) {
            PayloadTypeRegistry.clientboundPlay().register(type, RawPayload.codec(type, Limits.MAX_CLIENTBOUND_BYTES));
        }
        PayloadTypeRegistry.serverboundPlay().register(SCREEN_CLOSED, RawPayload.codec(SCREEN_CLOSED, Limits.MAX_SERVERBOUND_BYTES));
        PayloadTypeRegistry.serverboundPlay().register(CONTROL_EVENT, RawPayload.codec(CONTROL_EVENT, Limits.MAX_SERVERBOUND_BYTES));
        PayloadTypeRegistry.serverboundPlay().register(ASSET_REQUEST, RawPayload.codec(ASSET_REQUEST, Limits.MAX_SERVERBOUND_BYTES));
        PayloadTypeRegistry.clientboundPlay().register(ASSET_DATA, RawPayload.codec(ASSET_DATA, Limits.MAX_CLIENTBOUND_BYTES));
    }
}
