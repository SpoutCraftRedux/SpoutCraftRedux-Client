package org.spoutcraftredux.client.gui.widget;

import com.mojang.blaze3d.platform.cursor.CursorType;
import org.jspecify.annotations.Nullable;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spoutcraftredux.client.gui.server.AssetCache;

/**
 * A button drawn from the 200x20 button texture: the left and right halves are drawn so the edges stay sharp, and
 * the whole button is scaled when it is wider than 200 or not 20 pixels tall. Text sits 6 pixels down, with shadow.
 * A {@link Style} can swap the texture for server images or flat colours.
 */
public class Button extends Widget {
    /**
     * Custom looks; null parts keep the vanilla look. Hover and disabled fall back to the normal value.
     *
     * @param border image pixels kept unstretched at each edge, 0 to stretch the whole image
     */
    public record Style(@Nullable String texture, @Nullable String hoverTexture, @Nullable String disabledTexture, int border,
                        @Nullable Integer background, @Nullable Integer hoverBackground, @Nullable Integer disabledBackground,
                        @Nullable Integer color, @Nullable Integer hoverColor, @Nullable Integer disabledColor, boolean shadow) {
        public static final Style VANILLA = new Style(null, null, null, 0, null, null, null, null, null, null, true);
    }

    private Component text;
    private Align align = Align.CENTER;
    private Style style = Style.VANILLA;
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

    public Button setStyle(Style style) {
        this.style = style;
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
        if (!this.drawCustomBackground(graphics, hovered)) {
            Identifier sprite = !this.enabled ? WidgetStyle.BUTTON_DISABLED : hovered ? WidgetStyle.BUTTON_HOVER : WidgetStyle.BUTTON;
            drawButtonBackground(graphics, sprite, this.x, this.y, this.width, this.height);
        }

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
        graphics.text(font, shown, left, 6, this.textColor(hovered), this.style.shadow());
        graphics.pose().popMatrix();
    }

    private static <T> @Nullable T pick(boolean enabled, boolean hovered, @Nullable T normal, @Nullable T hover, @Nullable T disabled) {
        if (!enabled && disabled != null) {
            return disabled;
        }
        if (enabled && hovered && hover != null) {
            return hover;
        }
        return normal;
    }

    private int textColor(boolean hovered) {
        Integer color = pick(this.enabled, hovered, this.style.color(), this.style.hoverColor(), this.style.disabledColor());
        return color != null ? color : WidgetStyle.textColor(this.enabled, hovered);
    }

    private boolean drawCustomBackground(GuiGraphicsExtractor graphics, boolean hovered) {
        String sha = pick(this.enabled, hovered, this.style.texture(), this.style.hoverTexture(), this.style.disabledTexture());
        AssetCache.Loaded image = sha == null ? null : AssetCache.get(sha);
        if (image != null) {
            drawNineSlice(graphics, image, this.x, this.y, this.width, this.height, this.style.border());
            return true;
        }
        Integer fill = pick(this.enabled, hovered, this.style.background(), this.style.hoverBackground(), this.style.disabledBackground());
        if (fill != null) {
            graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, fill);
            return true;
        }
        // An image still downloading shows the vanilla button meanwhile
        return false;
    }

    /** Draws an image with its edges kept at their own size and the middle stretched. */
    static void drawNineSlice(GuiGraphicsExtractor graphics, AssetCache.Loaded image, int x, int y, int width, int height, int border) {
        int iw = image.width();
        int ih = image.height();
        int b = Math.max(0, Math.min(border, Math.min(Math.min(iw, ih) / 2, Math.min(width, height) / 2)));
        if (b == 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, image.texture(), x, y, 0, 0, width, height, iw, ih, iw, ih);
            return;
        }
        int[] dx = {x, x + b, x + width - b};
        int[] dw = {b, width - 2 * b, b};
        int[] sx = {0, b, iw - b};
        int[] sw = {b, iw - 2 * b, b};
        int[] dy = {y, y + b, y + height - b};
        int[] dh = {b, height - 2 * b, b};
        int[] sy = {0, b, ih - b};
        int[] sh = {b, ih - 2 * b, b};
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (dw[col] > 0 && dh[row] > 0 && sw[col] > 0 && sh[row] > 0) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, image.texture(), dx[col], dy[row], sx[col], sy[row],
                        dw[col], dh[row], sw[col], sh[row], iw, ih);
                }
            }
        }
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

    @Override
    public @Nullable CursorType getCursor(double mouseX, double mouseY) {
        return this.clickCursor(mouseX, mouseY);
    }
}
