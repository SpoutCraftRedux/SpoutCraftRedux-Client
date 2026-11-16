package org.spoutcraftredux.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** A rectangle fading from the top color to the bottom color. Use the same color twice for a solid box. */
public class Gradient extends Widget {
    private int topColor;
    private int bottomColor;

    public Gradient(int topColor, int bottomColor) {
        this.topColor = topColor;
        this.bottomColor = bottomColor;
    }

    public Gradient setColors(int topColor, int bottomColor) {
        this.topColor = topColor;
        this.bottomColor = bottomColor;
        return this;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTick) {
        if (this.visible) {
            graphics.fillGradient(this.x, this.y, this.x + this.width, this.y + this.height, this.topColor, this.bottomColor);
        }
    }
}
