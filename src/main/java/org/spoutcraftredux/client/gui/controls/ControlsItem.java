package org.spoutcraftredux.client.gui.controls;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.spoutcraftredux.client.controls.KeyCombo;
import org.spoutcraftredux.client.gui.widget.ListView;
import org.spoutcraftredux.client.gui.widget.WidgetStyle;

/**
 * One row in the controls list: a colored letter for the kind of binding, its name (red when another binding uses
 * the same key) and the key on the right, or "> <" while waiting for a new key.
 */
public abstract class ControlsItem implements ListView.Item {
    private static final int KEY_COLOR = 0xFFCCCCCC;
    private static final int CONFLICT_COLOR = 0xFFFF0000;

    protected final ControlsScreen screen;
    private boolean conflicting;

    protected ControlsItem(ControlsScreen screen) {
        this.screen = screen;
    }

    public abstract Component getName();

    public abstract KeyCombo getKey();

    public abstract void setKey(KeyCombo key);

    /** Whether Ctrl, Shift and Alt can be part of the key. */
    public abstract boolean useModifiers();

    protected abstract String letter();

    protected abstract int letterColor();

    public boolean conflicts(ControlsItem other) {
        KeyCombo mine = this.getKey();
        return !mine.isUnbound() && mine.equals(other.getKey());
    }

    public void setConflicting(boolean conflicting) {
        this.conflicting = conflicting;
    }

    @Override
    public int height() {
        return 11;
    }

    protected Component keyText() {
        return this.screen.isEditing(this) ? Component.literal("> <") : this.getKey().describe();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height) {
        graphics.text(font, this.letter(), x + 2, y + 2, this.letterColor(), true);
        int letterWidth = font.width(this.letter());
        Component key = this.keyText();
        int keyWidth = font.width(key);
        graphics.text(font, key, width - keyWidth, y + 2, KEY_COLOR, true);
        String name = WidgetStyle.fit(font, this.getName().getString(), width - letterWidth - keyWidth - 4);
        graphics.text(font, name, x + letterWidth + 4, y + 2, this.conflicting ? CONFLICT_COLOR : WidgetStyle.WHITE, true);
    }
}
