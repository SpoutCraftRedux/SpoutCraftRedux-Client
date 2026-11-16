package org.spoutcraftredux.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

/** A scrollable box holding other widgets, positioned relative to its top left corner. */
public class ScrollArea extends Scrollable {
    private final List<Widget> children = new ArrayList<>();

    public ScrollArea add(Widget child) {
        this.children.add(child);
        return this;
    }

    public List<Widget> children() {
        return this.children;
    }

    @Override
    protected int contentHeight() {
        int bottom = 0;
        for (Widget child : this.children) {
            if (child.isVisible()) {
                bottom = Math.max(bottom, child.getY() + child.getHeight());
            }
        }
        return bottom + 5;
    }

    @Override
    protected void renderContents(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTick) {
        for (Widget child : this.children) {
            child.render(graphics, font, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean doubleClick) {
        if (!this.visible || !this.inViewport(mouseX, mouseY)) {
            return false;
        }
        double localX = mouseX - this.x;
        double localY = mouseY - this.y + this.scrollY;
        for (Widget child : this.children.reversed()) {
            if (child.mouseClicked(localX, localY, button, doubleClick)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = false;
        for (Widget child : this.children) {
            handled |= child.mouseReleased(mouseX - this.x, mouseY - this.y + this.scrollY, button);
        }
        return handled;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        for (Widget child : this.children) {
            if (child.mouseDragged(mouseX - this.x, mouseY - this.y + this.scrollY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        for (Widget child : this.children) {
            if (child.keyPressed(event)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        for (Widget child : this.children) {
            if (child.charTyped(event)) {
                return true;
            }
        }
        return false;
    }
}
