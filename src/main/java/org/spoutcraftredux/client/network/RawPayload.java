package org.spoutcraftredux.client.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Carries the protocol module's encoded bytes inside a vanilla custom payload. */
public record RawPayload(Type<RawPayload> type, byte[] data) implements CustomPacketPayload {
    public static Type<RawPayload> type(String channel) {
        return new Type<>(Identifier.parse(channel));
    }

    public static StreamCodec<FriendlyByteBuf, RawPayload> codec(Type<RawPayload> type, int maxBytes) {
        return StreamCodec.of(
            (buf, payload) -> buf.writeBytes(payload.data()),
            buf -> {
                int length = buf.readableBytes();
                if (length > maxBytes) {
                    throw new IllegalArgumentException(type.id() + " payload of " + length + " bytes exceeds " + maxBytes);
                }
                byte[] data = new byte[length];
                buf.readBytes(data);
                return new RawPayload(type, data);
            }
        );
    }
}
