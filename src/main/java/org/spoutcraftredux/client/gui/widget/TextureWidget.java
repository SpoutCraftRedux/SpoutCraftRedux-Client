package org.spoutcraftredux.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import org.spoutcraftredux.client.gui.server.AssetCache;

/** An image from the server, stretched to the widget's size. Draws nothing until the image has arrived. */
public class TextureWidget extends Widget {
    private String sha256;

    public TextureWidget(String sha256) {
        this.sha256 = sha256;
    }

    public TextureWidget setAsset(String sha256) {
        this.sha256 = sha256;
        return this;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTick) {
        if (!this.visible || this.width <= 0 || this.height <= 0) {
            return;
        }
        AssetCache.Loaded image = AssetCache.get(this.sha256);
        if (image != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, image.texture(), this.x, this.y, 0, 0, this.width, this.height,
                image.width(), image.height(), image.width(), image.height());
        }
    }
}
