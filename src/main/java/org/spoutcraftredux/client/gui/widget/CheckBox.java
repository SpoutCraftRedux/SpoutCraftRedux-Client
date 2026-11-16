package org.spoutcraftredux.client.gui.widget;

import com.mojang.blaze3d.platform.cursor.CursorType;
import org.jspecify.annotations.Nullable;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A 20x20 box with a mark: green when checked, faint when not. The label sits to the right at 22,7 without shadow.
 */
public class CheckBox extends Widget {
    private Component text;
    private boolean checked;
    private Consumer<Boolean> onChange = value -> {
    };

    public CheckBox(Component text) {
        this.text = text;
        this.width = 100;
        this.height = 20;
    }

    public CheckBox setChecked(boolean checked) {
        this.checked = checked;
        return this;
    }

    public boolean isChecked() {
        return this.checked;
    }

    public CheckBox setText(Component text) {
        this.text = text;
        return this;
    }

    public CheckBox onChange(Consumer<Boolean> onChange) {
        this.onChange = onChange;
        return this;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }
        boolean hovered = this.enabled && this.contains(mouseX, mouseY);
        drawBox(graphics, this.x, this.y, this.enabled, hovered);
        int color = WidgetStyle.textColor(this.enabled, hovered);
        int mark = this.checked ? 0xFF00FF00 : WidgetStyle.withAlpha(color, 0.2F);
        graphics.blit(RenderPipelines.GUI_TEXTURED, WidgetStyle.BOX_CHECK, this.x, this.y, 0, 0, 20, 20, 20, 20, mark);
        graphics.text(font, this.text, this.x + 22, this.y + 7, color, false);
    }

    static void drawBox(GuiGraphicsExtractor graphics, int x, int y, boolean enabled, boolean hovered) {
        Identifier box = !enabled ? WidgetStyle.BOX_DISABLED : hovered ? WidgetStyle.BOX_HOVER : WidgetStyle.BOX_NORMAL;
        graphics.blit(RenderPipelines.GUI_TEXTURED, box, x, y, 0, 0, 20, 20, 20, 20);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean doubleClick) {
        if (this.visible && this.enabled && button == InputConstants.MOUSE_BUTTON_LEFT && this.contains(mouseX, mouseY)) {
            WidgetStyle.playClick();
            this.checked = !this.checked;
            this.onChange.accept(this.checked);
            return true;
        }
        return false;
    }

    @Override
    public @Nullable CursorType getCursor(double mouseX, double mouseY) {
        return this.clickCursor(mouseX, mouseY);
    }
}
