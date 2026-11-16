package org.spoutcraftredux.client.controls;

import java.util.ArrayList;
import java.util.List;

/** A player-made key that sends chat messages or commands, with an optional delay between them. */
public final class Shortcut {
    private String title = "";
    private KeyCombo key = KeyCombo.UNBOUND;
    private final List<String> commands = new ArrayList<>();
    private int delayMillis;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public KeyCombo getKey() {
        return this.key;
    }

    public void setKey(KeyCombo key) {
        this.key = key;
    }

    public List<String> getCommands() {
        return this.commands;
    }

    public int getDelayMillis() {
        return this.delayMillis;
    }

    public void setDelayMillis(int delayMillis) {
        this.delayMillis = Math.max(0, delayMillis);
    }

    public Shortcut copy() {
        Shortcut copy = new Shortcut();
        copy.title = this.title;
        copy.key = this.key;
        copy.commands.addAll(this.commands);
        copy.delayMillis = this.delayMillis;
        return copy;
    }
}
