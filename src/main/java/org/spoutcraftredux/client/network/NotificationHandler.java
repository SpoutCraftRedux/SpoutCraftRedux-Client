package org.spoutcraftredux.client.network;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.spoutcraftredux.client.gui.NotificationToast;
import org.spoutcraftredux.protocol.ProtocolException;
import org.spoutcraftredux.protocol.message.Notification;

public final class NotificationHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private NotificationHandler() {
    }

    public static void handle(Minecraft minecraft, byte[] data) {
        Notification notification;
        try {
            notification = Notification.decode(data);
        } catch (ProtocolException e) {
            LOGGER.warn("Ignoring malformed SpoutCraft Redux notification: {}", e.getMessage());
            return;
        }
        NotificationToast.show(
            minecraft.gui.toastManager(),
            parse(minecraft, notification.titleJson()),
            parse(minecraft, notification.messageJson()),
            icon(notification.itemId()),
            notification.effectiveDisplayMillis()
        );
    }

    public static Component parse(Minecraft minecraft, String json) {
        try {
            var ops = minecraft.getConnection() != null
                ? minecraft.getConnection().registryAccess().createSerializationContext(JsonOps.INSTANCE)
                : JsonOps.INSTANCE;
            return ComponentSerialization.CODEC.parse(ops, JsonParser.parseString(json))
                .resultOrPartial(error -> LOGGER.warn("Bad notification text: {}", error))
                .orElse(Component.empty());
        } catch (RuntimeException e) {
            LOGGER.warn("Bad notification text: {}", e.getMessage());
            return Component.empty();
        }
    }

    private static ItemStack icon(String itemId) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) {
            return ItemStack.EMPTY;
        }
        return BuiltInRegistries.ITEM.getOptional(id).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }
}
