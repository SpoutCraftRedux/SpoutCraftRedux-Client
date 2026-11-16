package org.spoutcraftredux.client.gui.controls;

import net.minecraft.network.chat.Component;
import org.spoutcraftredux.client.controls.KeyCombo;
import org.spoutcraftredux.client.controls.Shortcut;
import org.spoutcraftredux.client.controls.Shortcuts;

/** A player-made shortcut. */
public final class ShortcutItem extends ControlsItem {
    private final Shortcut shortcut;

    public ShortcutItem(ControlsScreen screen, Shortcut shortcut) {
        super(screen);
        this.shortcut = shortcut;
    }

    public Shortcut shortcut() {
        return this.shortcut;
    }

    @Override
    public Component getName() {
        return Component.literal(this.shortcut.getTitle());
    }

    @Override
    public KeyCombo getKey() {
        return this.shortcut.getKey();
    }

    @Override
    public void setKey(KeyCombo key) {
        this.shortcut.setKey(key);
        Shortcuts.save();
    }

    @Override
    public boolean useModifiers() {
        return true;
    }

    @Override
    protected String letter() {
        return "S";
    }

    @Override
    protected int letterColor() {
        return 0xFF00FF00;
    }
}
