package org.spoutcraftredux.client.gui.controls;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.spoutcraftredux.client.controls.KeyCombo;
import org.spoutcraftredux.client.controls.ServerKeyBindings;
import org.spoutcraftredux.client.gui.widget.WidgetStyle;

/** A key binding from the server, with the plugin that declared it on a second line. */
public final class ServerBindingItem extends ControlsItem {
    private final ServerKeyBindings.Binding binding;

    public ServerBindingItem(ControlsScreen screen, ServerKeyBindings.Binding binding) {
        super(screen);
        this.binding = binding;
    }

    @Override
    public Component getName() {
        return this.binding.name();
    }

    @Override
    public KeyCombo getKey() {
        return this.binding.key();
    }

    @Override
    public void setKey(KeyCombo key) {
        this.binding.setKey(key);
    }

    @Override
    public boolean useModifiers() {
        return true;
    }

    @Override
    public int height() {
        return 20;
    }

    @Override
    protected String letter() {
        return "B";
    }

    @Override
    protected int letterColor() {
        return 0xFF0000FF;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height) {
        super.render(graphics, font, x, y, width, height);
        int letterWidth = font.width(this.letter());
        graphics.text(font, this.binding.pluginName(), x + letterWidth + 4, y + 11, WidgetStyle.WHITE, true);
    }
}
