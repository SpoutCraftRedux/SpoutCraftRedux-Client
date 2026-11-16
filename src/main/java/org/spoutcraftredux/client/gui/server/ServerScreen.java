package org.spoutcraftredux.client.gui.server;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import org.spoutcraftredux.client.gui.widget.Widget;
import org.spoutcraftredux.client.gui.widget.WidgetScreen;
import org.spoutcraftredux.protocol.gui.WidgetSpec;
import org.spoutcraftredux.protocol.message.ControlEvent;

/** A popup screen the server built. Closing it tells the server, unless the server closed it. */
public class ServerScreen extends WidgetScreen {
    private record Entry(WidgetSpec spec, Widget widget) {
    }

    private final int screenId;
    private final Consumer<ControlEvent> events;
    private final Runnable onPlayerClose;
    private final Map<Integer, Entry> entries = new LinkedHashMap<>();
    private boolean closedByServer;

    public ServerScreen(int screenId, Component title, List<WidgetSpec> widgets, Consumer<ControlEvent> events, Runnable onPlayerClose) {
        super(title, null);
        this.screenId = screenId;
        this.events = events;
        this.onPlayerClose = onPlayerClose;
        widgets.forEach(this::put);
    }

    public int screenId() {
        return this.screenId;
    }

    /** Adds or replaces a widget, keeping its drawing position when replaced. */
    public void put(WidgetSpec spec) {
        Widget widget = ServerWidgets.create(spec, this.screenId, this.events);
        Entry old = this.entries.get(spec.id());
        if (widget == null) {
            return;
        }
        if (old != null) {
            this.replaceWidget(old.widget(), widget);
        } else {
            this.add(widget);
        }
        this.entries.put(spec.id(), new Entry(spec, widget));
        if (this.width > 0) {
            ServerWidgets.place(widget, spec, this.width, this.height);
        }
    }

    public void remove(int widgetId) {
        Entry old = this.entries.remove(widgetId);
        if (old != null) {
            this.removeWidget(old.widget());
        }
    }

    /** Stops this screen from telling the server it was closed, for when the server replaces it. */
    public void markClosedByServer() {
        this.closedByServer = true;
    }

    public void closeFromServer() {
        this.closedByServer = true;
        this.onClose();
    }

    @Override
    protected void createWidgets() {
    }

    @Override
    protected void layoutWidgets() {
        for (Entry entry : this.entries.values()) {
            ServerWidgets.place(entry.widget(), entry.spec(), this.width, this.height);
        }
    }

    @Override
    public void removed() {
        if (!this.closedByServer) {
            this.onPlayerClose.run();
        }
        super.removed();
    }
}
