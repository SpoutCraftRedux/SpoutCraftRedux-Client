package org.spoutcraftredux.client.gui.widget;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import org.jspecify.annotations.Nullable;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

/**
 * A clipped, scrollable box with a translucent black background. When the content is taller than the box, a 16
 * pixel scrollbar appears on the right with the creative-inventory scroller as the thumb, and the top and bottom
 * edges fade to black.
 */
public abstract class Scrollable extends Widget {
    private static final int BACKGROUND = 0x99000000;
    private static final int BAR_TOP = 0x54424242;
    private static final int BAR_BOTTOM = 0x611A1A1A;
    private static final int SCROLLBAR = 16;
    private static final int SCROLL_STEP = 20;

    protected int scrollY;
    private boolean draggingBar;

    protected abstract int contentHeight();

    /** Draws the content with 0,0 at the top left of the content; mouse coordinates are in that space too. */
    protected abstract void renderContents(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTick);

    public boolean needsScrollBar() {
        return this.contentHeight() > this.height;
    }

    public int viewportWidth() {
        return this.width - (this.needsScrollBar() ? SCROLLBAR : 0);
    }

    public int maxScroll() {
        return Math.max(0, this.contentHeight() - this.height);
    }

    public void setScroll(int scrollY) {
        this.scrollY = Math.clamp(scrollY, 0, this.maxScroll());
    }

    public int getScroll() {
        return this.scrollY;
    }

    protected boolean inViewport(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.viewportWidth() && mouseY < this.y + this.height;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }
        this.setScroll(this.scrollY);
        graphics.enableScissor(this.x, this.y, this.x + this.width, this.y + this.height);
        graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, BACKGROUND);
        graphics.pose().pushMatrix();
        graphics.pose().translate(this.x, this.y - this.scrollY);
        boolean inside = this.inViewport(mouseX, mouseY);
        int contentMouseX = inside ? mouseX - this.x : Integer.MIN_VALUE;
        int contentMouseY = inside ? mouseY - this.y + this.scrollY : Integer.MIN_VALUE;
        this.renderContents(graphics, font, contentMouseX, contentMouseY, partialTick);
        graphics.pose().popMatrix();
        graphics.disableScissor();

        if (this.needsScrollBar()) {
            int right = this.x + this.width;
            graphics.fillGradient(right - SCROLLBAR, this.y, right, this.y + this.height, BAR_TOP, BAR_BOTTOM);
            double progress = (double) this.scrollY / this.maxScroll();
            int thumbY = this.y + (int) (3 + progress * (this.height - 16.0 - 6));
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, WidgetStyle.SCROLLER, right - SCROLLBAR, thumbY, 12, 15);
            graphics.fillGradient(this.x, this.y - 1, right, this.y + 5, 0xFF000000, 0x00000000);
            graphics.fillGradient(this.x, this.y + this.height - 5, right + 1, this.y + this.height, 0x00000000, 0xFF000000);
        }
    }

    /** Starts dragging when the scrollbar is clicked. Subclasses call this before handling their own clicks. */
    protected boolean clickScrollBar(double mouseX, double mouseY, int button) {
        int right = this.x + this.width;
        if (this.visible && button == InputConstants.MOUSE_BUTTON_LEFT && this.needsScrollBar()
            && mouseX >= right - SCROLLBAR && mouseX < right && mouseY >= this.y && mouseY < this.y + this.height) {
            this.draggingBar = true;
            this.dragScrollBar(mouseY);
            return true;
        }
        return false;
    }

    private void dragScrollBar(double mouseY) {
        // Puts the middle of the thumb under the cursor
        double track = this.height - 16.0 - 6;
        double progress = (mouseY - this.y - 3 - 7.5) / track;
        this.setScroll((int) Math.round(progress * this.maxScroll()));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean doubleClick) {
        return this.clickScrollBar(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (this.draggingBar) {
            this.dragScrollBar(mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean was = this.draggingBar;
        this.draggingBar = false;
        return was;
    }

    @Override
    public @Nullable CursorType getCursor(double mouseX, double mouseY) {
        int right = this.x + this.width;
        boolean overBar = this.needsScrollBar() && mouseX >= right - SCROLLBAR && mouseX < right
            && mouseY >= this.y && mouseY < this.y + this.height;
        return this.draggingBar || overBar ? CursorTypes.RESIZE_NS : null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.visible && this.contains(mouseX, mouseY) && this.needsScrollBar()) {
            this.setScroll(this.scrollY - (int) (amount * SCROLL_STEP));
            return true;
        }
        return false;
    }
}
