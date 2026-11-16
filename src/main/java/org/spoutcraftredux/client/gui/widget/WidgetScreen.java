package org.spoutcraftredux.client.gui.widget;

import com.mojang.blaze3d.platform.cursor.CursorType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * A screen built from widgets. Over a world it darkens the game with a plain gradient; outside a world it uses the
 * normal menu background.
 */
public abstract class WidgetScreen extends Screen {
    private static final int SHADE_TOP = 0xC0101010;
    private static final int SHADE_BOTTOM = 0xD0101010;

    protected final @Nullable Screen parent;
    private final List<Widget> widgets = new ArrayList<>();
    private @Nullable Widget focused;

    protected WidgetScreen(Component title, @Nullable Screen parent) {
        super(title);
        this.parent = parent;
    }

    /** Creates the widgets. Called once when the screen opens. */
    protected abstract void createWidgets();

    /** Positions the widgets for the current screen size. Called on open and on every resize. */
    protected abstract void layoutWidgets();

    protected <T extends Widget> T add(T widget) {
        this.widgets.add(widget);
        return widget;
    }

    @Override
    protected void init() {
        if (this.widgets.isEmpty()) {
            this.createWidgets();
        }
        this.layoutWidgets();
    }

    @Override
    public void tick() {
        for (Widget widget : this.widgets) {
            widget.tick();
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft.level != null) {
            graphics.fillGradient(0, 0, this.width, this.height, SHADE_TOP, SHADE_BOTTOM);
        } else {
            super.extractBackground(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        for (Widget widget : this.widgets) {
            widget.render(graphics, this.font, mouseX, mouseY, partialTick);
        }
        for (Widget widget : this.widgets.reversed()) {
            CursorType cursor = widget.isVisible() ? widget.getCursor(mouseX, mouseY) : null;
            if (cursor != null) {
                graphics.requestCursor(cursor);
                break;
            }
        }
        for (Widget widget : this.widgets.reversed()) {
            if (widget.isVisible() && widget.getTooltip() != null && widget.contains(mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(this.font, widget.getTooltip(), mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        for (Widget widget : this.widgets.reversed()) {
            if (widget.isVisible() && widget.mouseClicked(event.x(), event.y(), event.button(), doubleClick)) {
                this.focus(widget.isFocusable() ? widget : null);
                return true;
            }
        }
        this.focus(null);
        return super.mouseClicked(event, doubleClick);
    }

    private void focus(@Nullable Widget widget) {
        if (this.focused != null && this.focused != widget) {
            this.focused.setFocused(false);
        }
        this.focused = widget;
        if (widget != null) {
            widget.setFocused(true);
        }
        // Typed characters only arrive while text input is switched on
        this.minecraft.onTextInputFocusChange(this, widget != null && widget.wantsTextInput());
    }

    @Override
    public void removed() {
        this.minecraft.onTextInputFocusChange(this, false);
        super.removed();
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean handled = false;
        for (Widget widget : this.widgets) {
            handled |= widget.mouseReleased(event.x(), event.y(), event.button());
        }
        return handled || super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        for (Widget widget : this.widgets) {
            if (widget.mouseDragged(event.x(), event.y(), event.button())) {
                return true;
            }
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (Widget widget : this.widgets.reversed()) {
            if (widget.isVisible() && widget.mouseScrolled(mouseX, mouseY, scrollY)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.focused != null && this.focused.keyPressed(event)) {
            return true;
        }
        for (Widget widget : this.widgets) {
            if (widget != this.focused && widget.keyPressed(event)) {
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.focused != null && this.focused.charTyped(event)) {
            return true;
        }
        for (Widget widget : this.widgets) {
            if (widget != this.focused && widget.charTyped(event)) {
                return true;
            }
        }
        return super.charTyped(event);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
