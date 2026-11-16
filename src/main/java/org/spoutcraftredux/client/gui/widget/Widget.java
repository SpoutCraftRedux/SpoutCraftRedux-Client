package org.spoutcraftredux.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/** Base for every widget. Positions are in GUI pixels relative to the parent. */
public abstract class Widget {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected boolean visible = true;
    protected boolean enabled = true;
    protected boolean focused;
    protected @Nullable Component tooltip;

    public Widget setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    public Widget setX(int x) {
        this.x = x;
        return this;
    }

    public Widget setY(int y) {
        this.y = y;
        return this;
    }

    public Widget setWidth(int width) {
        this.width = width;
        return this;
    }

    public Widget setHeight(int height) {
        this.height = height;
        return this;
    }

    public Widget setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public Widget setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Widget setTooltip(@Nullable Component tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public @Nullable Component getTooltip() {
        return this.tooltip;
    }

    public boolean isFocusable() {
        return false;
    }

    /** Whether this widget needs typed characters while focused. */
    public boolean wantsTextInput() {
        return false;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }

    /** Mouse coordinates are in the same space as this widget's position. */
    public abstract void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTick);

    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean doubleClick) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        return false;
    }

    public boolean charTyped(CharacterEvent event) {
        return false;
    }

    public void tick() {
    }
}
