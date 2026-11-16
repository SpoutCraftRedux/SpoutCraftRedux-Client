package org.spoutcraftredux.client.gui.server;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spoutcraftredux.client.gui.widget.Align;
import org.spoutcraftredux.client.gui.widget.Button;
import org.spoutcraftredux.client.gui.widget.CheckBox;
import org.spoutcraftredux.client.gui.widget.Gradient;
import org.spoutcraftredux.client.gui.widget.ItemWidget;
import org.spoutcraftredux.client.gui.widget.Label;
import org.spoutcraftredux.client.gui.widget.ListView;
import org.spoutcraftredux.client.gui.widget.Slider;
import org.spoutcraftredux.client.gui.widget.TextField;
import org.spoutcraftredux.client.gui.widget.TextureWidget;
import org.spoutcraftredux.client.gui.widget.Widget;
import org.spoutcraftredux.client.network.NotificationHandler;
import org.spoutcraftredux.protocol.gui.Anchors;
import org.spoutcraftredux.protocol.gui.WidgetSpec;
import org.spoutcraftredux.protocol.gui.WidgetTypes;
import org.spoutcraftredux.protocol.message.ControlEvent;

/** Builds toolkit widgets from the server's widget descriptions and places them on the screen. */
public final class ServerWidgets {
    private ServerWidgets() {
    }

    private static Component text(String json) {
        return json.isEmpty() ? Component.empty() : NotificationHandler.parse(Minecraft.getInstance(), json);
    }

    private static Align align(int value) {
        return switch (value) {
            case Anchors.ALIGN_CENTER -> Align.CENTER;
            case Anchors.ALIGN_RIGHT -> Align.RIGHT;
            default -> Align.LEFT;
        };
    }

    /**
     * Creates the widget for a description, or null for a type this client doesn't know.
     *
     * @param events receives the player's interactions; null for the HUD, which takes no input
     */
    public static @Nullable Widget create(WidgetSpec spec, int screenId, @Nullable Consumer<ControlEvent> events) {
        Consumer<ControlEvent> send = events == null ? event -> {
        } : events;
        Widget widget = switch (spec.type()) {
            case WidgetTypes.LABEL -> new Label(text(spec.string("text", "")))
                .setColor(spec.integer("color", 0xFFFFFFFF))
                .setShadow(spec.bool("shadow", true))
                .setWrap(spec.bool("wrap", false))
                .setAlign(align(spec.integer("align", Anchors.ALIGN_LEFT)));
            case WidgetTypes.BUTTON -> new Button(text(spec.string("text", "")))
                .setAlign(align(spec.integer("align", Anchors.ALIGN_CENTER))).setStyle(buttonStyle(spec))
                .onClick(() -> send.accept(ControlEvent.click(screenId, spec.id())));
            case WidgetTypes.CHECKBOX -> new CheckBox(text(spec.string("text", "")))
                .setChecked(spec.bool("checked", false))
                .onChange(checked -> send.accept(new ControlEvent(screenId, spec.id(), ControlEvent.CHECK, checked, -1, 0.0F, "")));
            case WidgetTypes.TEXT_FIELD -> new TextField()
                .setMaxLength(Math.max(1, spec.integer("max_length", 256)))
                .setText(spec.string("text", ""))
                .setPlaceholder(text(spec.string("placeholder", "")))
                .onChange(value -> send.accept(new ControlEvent(screenId, spec.id(), ControlEvent.TEXT, false, -1, 0.0F, value)))
                .onSubmit(value -> send.accept(new ControlEvent(screenId, spec.id(), ControlEvent.SUBMIT, false, -1, 0.0F, value)));
            case WidgetTypes.SLIDER -> new Slider(text(spec.string("text", "")))
                .setPosition(spec.number("position", 0.0F))
                .onChange(value -> send.accept(new ControlEvent(screenId, spec.id(), ControlEvent.SLIDE, false, -1, value, "")));
            case WidgetTypes.GRADIENT -> new Gradient(spec.integer("top", 0xFF000000), spec.integer("bottom", spec.integer("top", 0xFF000000)));
            case WidgetTypes.ITEM -> new ItemWidget(item(spec.string("item", "")));
            case WidgetTypes.LIST -> list(spec, screenId, send);
            case WidgetTypes.TEXTURE -> new TextureWidget(spec.string("asset", ""));
            default -> null;
        };
        if (widget != null) {
            widget.setVisible(spec.visible()).setEnabled(spec.enabled());
            widget.setTooltip(spec.tooltipJson().isEmpty() ? null : text(spec.tooltipJson()));
        }
        return widget;
    }

    private static @Nullable String optionalString(WidgetSpec spec, String key) {
        return spec.properties().get(key) instanceof String value && !value.isEmpty() ? value : null;
    }

    private static @Nullable Integer optionalInt(WidgetSpec spec, String key) {
        return spec.properties().get(key) instanceof Integer value ? value : null;
    }

    private static Button.Style buttonStyle(WidgetSpec spec) {
        return new Button.Style(optionalString(spec, "texture"), optionalString(spec, "hover_texture"),
            optionalString(spec, "disabled_texture"), spec.integer("border", 0),
            optionalInt(spec, "background"), optionalInt(spec, "hover_background"), optionalInt(spec, "disabled_background"),
            optionalInt(spec, "color"), optionalInt(spec, "hover_color"), optionalInt(spec, "disabled_color"),
            spec.bool("shadow", true));
    }

    private static Widget list(WidgetSpec spec, int screenId, Consumer<ControlEvent> send) {
        List<String> titles = spec.strings("titles");
        List<String> texts = spec.strings("texts");
        List<ListView.Item> rows = new ArrayList<>(titles.size());
        for (int i = 0; i < titles.size(); i++) {
            rows.add(new ListView.TextItem(text(titles.get(i)), i < texts.size() ? text(texts.get(i)) : Component.empty()));
        }
        ListView view = new ListView().setItems(rows).setSelected(spec.integer("selected", -1));
        view.onSelect(row -> send.accept(new ControlEvent(screenId, spec.id(), ControlEvent.SELECT, false, rows.indexOf(row), 0.0F, "")));
        return view;
    }

    private static ItemStack item(String id) {
        Identifier parsed = Identifier.tryParse(id);
        return parsed == null ? ItemStack.EMPTY : BuiltInRegistries.ITEM.getOptional(parsed).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    /**
     * Updates an existing widget in place, so a slider being dragged or a field being typed in isn't interrupted.
     *
     * @return false if the widget is of another type and has to be rebuilt
     */
    public static boolean apply(Widget widget, WidgetSpec spec) {
        boolean applied = switch (widget) {
            case Label label when spec.type().equals(WidgetTypes.LABEL) -> {
                label.setText(text(spec.string("text", ""))).setColor(spec.integer("color", 0xFFFFFFFF))
                    .setShadow(spec.bool("shadow", true)).setWrap(spec.bool("wrap", false))
                    .setAlign(align(spec.integer("align", Anchors.ALIGN_LEFT)));
                yield true;
            }
            case Button button when spec.type().equals(WidgetTypes.BUTTON) -> {
                button.setText(text(spec.string("text", ""))).setAlign(align(spec.integer("align", Anchors.ALIGN_CENTER)))
                    .setStyle(buttonStyle(spec));
                yield true;
            }
            case CheckBox box when spec.type().equals(WidgetTypes.CHECKBOX) -> {
                box.setText(text(spec.string("text", ""))).setChecked(spec.bool("checked", false));
                yield true;
            }
            case TextField field when spec.type().equals(WidgetTypes.TEXT_FIELD) -> {
                field.setMaxLength(Math.max(1, spec.integer("max_length", 256))).setPlaceholder(text(spec.string("placeholder", "")));
                if (!field.isFocused()) {
                    field.setText(spec.string("text", ""));
                }
                yield true;
            }
            case Slider slider when spec.type().equals(WidgetTypes.SLIDER) -> {
                slider.setText(text(spec.string("text", "")));
                if (!slider.isDragging()) {
                    slider.setPosition(spec.number("position", 0.0F));
                }
                yield true;
            }
            case Gradient gradient when spec.type().equals(WidgetTypes.GRADIENT) -> {
                gradient.setColors(spec.integer("top", 0xFF000000), spec.integer("bottom", spec.integer("top", 0xFF000000)));
                yield true;
            }
            case TextureWidget texture when spec.type().equals(WidgetTypes.TEXTURE) -> {
                texture.setAsset(spec.string("asset", ""));
                yield true;
            }
            case ItemWidget item when spec.type().equals(WidgetTypes.ITEM) -> {
                item.setStack(item(spec.string("item", "")));
                yield true;
            }
            default -> false;
        };
        if (applied) {
            widget.setVisible(spec.visible()).setEnabled(spec.enabled());
            widget.setTooltip(spec.tooltipJson().isEmpty() ? null : text(spec.tooltipJson()));
        }
        return applied;
    }

    /** Positions a widget for the given screen size, measuring from its anchor point. */
    public static void place(Widget widget, WidgetSpec spec, int screenWidth, int screenHeight) {
        int x;
        int y;
        int width = spec.width();
        int height = spec.height();
        if (spec.anchor() == Anchors.SCALE) {
            float scaleX = screenWidth / (float) Anchors.SCALE_WIDTH;
            float scaleY = screenHeight / (float) Anchors.SCALE_HEIGHT;
            x = Math.round(spec.x() * scaleX);
            y = Math.round(spec.y() * scaleY);
            width = Math.round(width * scaleX);
            height = Math.round(height * scaleY);
        } else {
            x = spec.x() + switch (spec.anchor() % 3) {
                case 1 -> screenWidth / 2;
                case 2 -> screenWidth;
                default -> 0;
            };
            y = spec.y() + switch (spec.anchor() / 3) {
                case 1 -> screenHeight / 2;
                case 2 -> screenHeight;
                default -> 0;
            };
        }
        widget.setX(x).setY(y);
        // A size of 0 keeps the widget's natural size
        if (width > 0) {
            widget.setWidth(width);
        }
        if (height > 0) {
            widget.setHeight(height);
        }
    }
}
