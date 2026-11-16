package org.spoutcraftredux.client.gui.server;

import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spoutcraftredux.client.gui.widget.Widget;
import org.spoutcraftredux.client.network.NotificationHandler;
import org.spoutcraftredux.client.network.RawPayload;
import org.spoutcraftredux.client.network.SpoutCraftReduxChannels;
import org.spoutcraftredux.protocol.gui.WidgetSpec;
import org.spoutcraftredux.protocol.message.ControlEvent;
import org.spoutcraftredux.protocol.message.ScreenClose;
import org.spoutcraftredux.protocol.message.ScreenOpen;
import org.spoutcraftredux.protocol.message.WidgetRemove;
import org.spoutcraftredux.protocol.message.WidgetUpdate;

/** Keeps the server's HUD widgets and popup screen in step with what the server sends. */
public final class ServerGui {
    private record HudEntry(WidgetSpec spec, Widget widget) {
    }

    private static final Map<Integer, HudEntry> HUD = new LinkedHashMap<>();
    private static @Nullable ServerScreen popup;

    private ServerGui() {
    }

    public static void register() {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("spoutcraftredux", "server_hud"), ServerGui::renderHud);
    }

    private static void renderHud(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (HUD.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        for (HudEntry entry : HUD.values()) {
            ServerWidgets.place(entry.widget(), entry.spec(), width, height);
            entry.widget().render(graphics, minecraft.font, Integer.MIN_VALUE, Integer.MIN_VALUE, delta.getGameTimeDeltaPartialTick(false));
        }
    }

    private static void send(ControlEvent event) {
        ClientPlayNetworking.send(new RawPayload(SpoutCraftReduxChannels.CONTROL_EVENT, event.encode()));
    }

    public static void open(Minecraft minecraft, ScreenOpen message) {
        if (popup != null) {
            popup.markClosedByServer();
        }
        int id = message.screenId();
        ServerScreen screen = new ServerScreen(id, NotificationHandler.parse(minecraft, message.titleJson()), message.widgets(),
            ServerGui::send, () -> {
                popup = null;
                ClientPlayNetworking.send(new RawPayload(SpoutCraftReduxChannels.SCREEN_CLOSED, new ScreenClose(id).encode()));
            });
        popup = screen;
        minecraft.gui.setScreen(screen);
    }

    public static void close(ScreenClose message) {
        ServerScreen screen = popup;
        if (screen != null && screen.screenId() == message.screenId()) {
            popup = null;
            screen.closeFromServer();
        }
    }

    public static void update(WidgetUpdate message) {
        for (WidgetSpec spec : message.widgets()) {
            if (message.screenId() == WidgetUpdate.HUD) {
                Widget widget = ServerWidgets.create(spec, WidgetUpdate.HUD, null);
                if (widget != null) {
                    HUD.put(spec.id(), new HudEntry(spec, widget));
                }
            } else if (popup != null && popup.screenId() == message.screenId()) {
                popup.put(spec);
            }
        }
    }

    public static void remove(WidgetRemove message) {
        for (int id : message.widgetIds()) {
            if (message.screenId() == WidgetUpdate.HUD) {
                HUD.remove(id);
            } else if (popup != null && popup.screenId() == message.screenId()) {
                popup.remove(id);
            }
        }
    }

    /** Forgets everything on disconnect. */
    public static void clear() {
        HUD.clear();
        popup = null;
    }
}
