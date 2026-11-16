package org.spoutcraftredux.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

/** Shared colors, textures and helpers so every widget draws the same way. */
public final class WidgetStyle {
    public static final int TEXT = 0xFFE0E0E0;
    public static final int TEXT_HOVER = 0xFFFFFFA0;
    public static final int TEXT_DISABLED = 0xFFA0A0A0;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int LIST_SUBTITLE = 0xFFCCCCCC;

    public static final Identifier BUTTON = Identifier.withDefaultNamespace("widget/button");
    public static final Identifier BUTTON_HOVER = Identifier.withDefaultNamespace("widget/button_highlighted");
    public static final Identifier BUTTON_DISABLED = Identifier.withDefaultNamespace("widget/button_disabled");
    public static final Identifier SCROLLER = Identifier.withDefaultNamespace("container/creative_inventory/scroller");

    public static final Identifier BOX_NORMAL = texture("box_normal");
    public static final Identifier BOX_HOVER = texture("box_hover");
    public static final Identifier BOX_DISABLED = texture("box_disabled");
    public static final Identifier BOX_CHECK = texture("box_check");
    public static final Identifier BOX_RADIO = texture("box_radio");
    public static final Identifier BOX_ASCENDING = texture("box_ascending");
    public static final Identifier BOX_DESCENDING = texture("box_descending");

    private WidgetStyle() {
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath("spoutcraftredux", "textures/gui/widget/" + name + ".png");
    }

    public static int textColor(boolean enabled, boolean hovered) {
        if (!enabled) {
            return TEXT_DISABLED;
        }
        return hovered ? TEXT_HOVER : TEXT;
    }

    public static int withAlpha(int argb, float alpha) {
        return ((int) (alpha * 255.0F) << 24) | (argb & 0xFFFFFF);
    }

    /** Cuts text down with "..." until it fits the width. */
    public static String fit(Font font, String text, int width) {
        if (width <= 1 || font.width(text) <= width) {
            return text;
        }
        String fitted = text;
        int remove = 0;
        while (font.width(fitted) > width && fitted.length() > 3) {
            remove++;
            fitted = text.substring(0, Math.max(0, text.length() - 1 - remove)) + "...";
        }
        return fitted;
    }

    public static void playClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
