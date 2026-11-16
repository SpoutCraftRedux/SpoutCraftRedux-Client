package org.spoutcraftredux.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/** An item icon, scaled from 16x16 to the widget's size. */
public class ItemWidget extends Widget {
    private ItemStack stack;

    public ItemWidget(ItemStack stack) {
        this.stack = stack;
        this.width = 16;
        this.height = 16;
    }

    public ItemWidget setStack(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTick) {
        if (!this.visible || this.stack.isEmpty()) {
            return;
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(this.x, this.y);
        graphics.pose().scale(this.width / 16.0F, this.height / 16.0F);
        graphics.item(this.stack, 0, 0);
        graphics.pose().popMatrix();
    }
}
