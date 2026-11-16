package org.spoutcraftredux.client.gui.widget;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * Text, one line per newline, 10 pixels apart, drawn with a shadow by default. With wrapping on, long lines break
 * at the widget's width.
 */
public class Label extends Widget {
    private static final int LINE_HEIGHT = 10;

    private Component text;
    private int color = WidgetStyle.WHITE;
    private boolean shadow = true;
    private boolean wrap;
    private Align align = Align.LEFT;

    public Label(Component text) {
        this.text = text;
    }

    public Label setText(Component text) {
        this.text = text;
        return this;
    }

    public Component getText() {
        return this.text;
    }

    public Label setColor(int argb) {
        this.color = argb;
        return this;
    }

    public Label setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public Label setWrap(boolean wrap) {
        this.wrap = wrap;
        return this;
    }

    public Label setAlign(Align align) {
        this.align = align;
        return this;
    }

    public List<FormattedCharSequence> lines(Font font) {
        return font.split(this.text, this.wrap && this.width > 0 ? this.width : Integer.MAX_VALUE);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }
        List<FormattedCharSequence> lines = this.lines(font);
        int textWidth = 0;
        for (FormattedCharSequence line : lines) {
            textWidth = Math.max(textWidth, font.width(line));
        }
        int box = this.width > 0 ? this.width : textWidth;
        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            int left = switch (this.align) {
                case LEFT -> 0;
                case CENTER -> box / 2 - font.width(line) / 2;
                case RIGHT -> box - font.width(line);
            };
            graphics.text(font, line, this.x + left, this.y + i * LINE_HEIGHT, this.color, this.shadow);
        }
    }
}
