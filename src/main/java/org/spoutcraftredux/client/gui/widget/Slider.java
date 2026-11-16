package org.spoutcraftredux.client.gui.widget;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import org.jspecify.annotations.Nullable;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

/**
 * A slider: the disabled button texture as the track and an 8 pixel handle made from the edges of the normal
 * button texture. The label is drawn without shadow.
 */
public class Slider extends Widget {
    private Component text;
    private float position;
    private boolean dragging;
    private Align align = Align.CENTER;
    private Consumer<Float> onChange = value -> {
    };

    public Slider(Component text) {
        this.text = text;
        this.width = 150;
        this.height = 20;
    }

    public Slider setText(Component text) {
        this.text = text;
        return this;
    }

    public Slider setPosition(float position) {
        this.position = Math.clamp(position, 0.0F, 1.0F);
        return this;
    }

    public float getPosition() {
        return this.position;
    }

    public Slider setAlign(Align align) {
        this.align = align;
        return this;
    }

    public Slider onChange(Consumer<Float> onChange) {
        this.onChange = onChange;
        return this;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }
        Button.drawButtonBackground(graphics, WidgetStyle.BUTTON_DISABLED, this.x, this.y, this.width, this.height);

        int drawWidth = Math.min(this.width, 200);
        graphics.pose().pushMatrix();
        graphics.pose().translate(this.x, this.y);
        graphics.pose().scale((float) this.width / drawWidth, this.height / 20.0F);
        int track = drawWidth - 8;
        int handle = (int) (this.position * track);
        boolean hovered = this.enabled && (this.dragging || this.contains(mouseX, mouseY));
        var handleSprite = hovered ? WidgetStyle.BUTTON_HOVER : WidgetStyle.BUTTON;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, handleSprite, 200, 20, 0, 0, handle, 0, 4, 20);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, handleSprite, 200, 20, 196, 0, handle + 4, 0, 4, 20);

        int left = switch (this.align) {
            case LEFT -> 5;
            case CENTER -> track / 2 - font.width(this.text) / 2;
            case RIGHT -> track - font.width(this.text) - 5;
        };
        graphics.text(font, this.text, left, 6, this.enabled ? WidgetStyle.TEXT : WidgetStyle.TEXT_DISABLED, false);
        graphics.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean doubleClick) {
        if (this.visible && this.enabled && button == InputConstants.MOUSE_BUTTON_LEFT && this.contains(mouseX, mouseY)) {
            WidgetStyle.playClick();
            this.dragging = true;
            this.drag(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (this.dragging) {
            this.drag(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean was = this.dragging;
        this.dragging = false;
        return was;
    }

    private void drag(double mouseX) {
        this.setPosition((float) (mouseX - (this.x + 4)) / (this.width - 8));
        this.onChange.accept(this.position);
    }

    @Override
    public @Nullable CursorType getCursor(double mouseX, double mouseY) {
        return this.dragging ? CursorTypes.POINTING_HAND : this.clickCursor(mouseX, mouseY);
    }
}
