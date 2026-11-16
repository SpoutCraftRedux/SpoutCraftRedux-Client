package org.spoutcraftredux.client.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A button drawn from the 200x20 button texture: the left and right halves are drawn so the edges stay sharp, and
 * the whole button is scaled when it is wider than 200 or not 20 pixels tall. Text sits 6 pixels down, with shadow.
 */
public class Button extends Widget {
    private Component text;
    private Align align = Align.CENTER;
    private Runnable onClick = () -> {
    };

    public Button(Component text) {
        this.text = text;
        this.width = 200;
        this.height = 20;
    }

    public Button setText(Component text) {
        this.text = text;
        return this;
    }

    public Component getText() {
        return this.text;
    }

    public Button setAlign(Align align) {
        this.align = align;
        return this;
    }

    public Button onClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }
        boolean hovered = this.enabled && this.contains(mouseX, mouseY);
        Identifier sprite = !this.enabled ? WidgetStyle.BUTTON_DISABLED : hovered ? WidgetStyle.BUTTON_HOVER : WidgetStyle.BUTTON;
        drawButtonBackground(graphics, sprite, this.x, this.y, this.width, this.height);

        int drawWidth = Math.min(this.width, 200);
        graphics.pose().pushMatrix();
        graphics.pose().translate(this.x, this.y);
        graphics.pose().scale((float) this.width / drawWidth, this.height / 20.0F);
        String label = WidgetStyle.fit(font, this.text.getString(), drawWidth - 10);
        int left = switch (this.align) {
            case LEFT -> 5;
            case CENTER -> drawWidth / 2 - font.width(label) / 2;
            case RIGHT -> drawWidth - font.width(label) - 5;
        };
        Component shown = label.equals(this.text.getString()) ? this.text : Component.literal(label).withStyle(this.text.getStyle());
        graphics.text(font, shown, left, 6, WidgetStyle.textColor(this.enabled, hovered), true);
        graphics.pose().popMatrix();
    }

    /** Draws a 200x20 sprite as two halves, scaled to the given size. */
    static void drawButtonBackground(GuiGraphicsExtractor graphics, Identifier sprite, int x, int y, int width, int height) {
        int drawWidth = Math.min(width, 200);
        int half = (int) Math.ceil(drawWidth / 2.0);
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale((float) width / drawWidth, height / 20.0F);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 200, 20, 0, 0, 0, 0, half, 20);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 200, 20, 200 - half, 0, drawWidth / 2, 0, half, 20);
        graphics.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean doubleClick) {
        if (this.visible && this.enabled && button == InputConstants.MOUSE_BUTTON_LEFT && this.contains(mouseX, mouseY)) {
            WidgetStyle.playClick();
            this.onClick.run();
            return true;
        }
        return false;
    }
}
