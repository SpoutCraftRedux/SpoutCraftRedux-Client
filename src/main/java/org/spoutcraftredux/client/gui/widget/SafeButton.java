package org.spoutcraftredux.client.gui.widget;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

/** A button that asks "Really?" first, and only acts on a second click within three seconds. */
public class SafeButton extends Button {
    private static final long TIMEOUT_MILLIS = 3000L;
    private static final Component WARNING = Component.literal("Really?").withStyle(ChatFormatting.RED);

    private final Component label;
    private final Runnable action;
    private long armedAt = -1L;

    public SafeButton(Component label, Runnable action) {
        super(label);
        this.label = label;
        this.action = action;
        this.onClick(this::clicked);
    }

    private void clicked() {
        if (this.isArmed()) {
            this.armedAt = -1L;
            this.setText(this.label);
            this.action.run();
        } else {
            this.armedAt = Util.getMillis();
            this.setText(WARNING);
        }
    }

    private boolean isArmed() {
        return this.armedAt >= 0 && Util.getMillis() - this.armedAt < TIMEOUT_MILLIS;
    }

    public void disarm() {
        this.armedAt = -1L;
        this.setText(this.label);
    }

    @Override
    public void tick() {
        this.setText(this.isArmed() ? WARNING : this.label);
    }
}
