package org.spoutcraftredux.client.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.spoutcraftredux.protocol.Limits;
import org.spoutcraftredux.protocol.message.Hello;
import org.spoutcraftredux.protocol.message.HelloAck;
import org.spoutcraftredux.protocol.message.Notification;

public final class SpoutCraftReduxChannels {
    public static final Type<RawPayload> HELLO = RawPayload.type(Hello.CHANNEL);
    public static final Type<RawPayload> HELLO_ACK = RawPayload.type(HelloAck.CHANNEL);
    public static final Type<RawPayload> NOTIFICATION = RawPayload.type(Notification.CHANNEL);

    private SpoutCraftReduxChannels() {
    }

    public static void register() {
        PayloadTypeRegistry.clientboundConfiguration().register(HELLO, RawPayload.codec(HELLO, Limits.MAX_CLIENTBOUND_BYTES));
        PayloadTypeRegistry.serverboundConfiguration().register(HELLO_ACK, RawPayload.codec(HELLO_ACK, Limits.MAX_SERVERBOUND_BYTES));
        PayloadTypeRegistry.clientboundPlay().register(NOTIFICATION, RawPayload.codec(NOTIFICATION, Limits.MAX_CLIENTBOUND_BYTES));
    }
}
