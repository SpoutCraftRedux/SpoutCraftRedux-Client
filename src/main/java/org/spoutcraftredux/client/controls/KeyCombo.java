package org.spoutcraftredux.client.controls;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

/** A key or mouse button plus the Ctrl, Shift and Alt modifiers that must be held with it. */
public record KeyCombo(InputConstants.Key key, int modifiers) {
    public static final int CTRL = 1;
    public static final int SHIFT = 2;
    public static final int ALT = 4;
    public static final KeyCombo UNBOUND = new KeyCombo(InputConstants.UNKNOWN, 0);

    public boolean isUnbound() {
        return this.key.equals(InputConstants.UNKNOWN);
    }

    /** On macOS, Cmd counts as Ctrl, the same as Minecraft's own shortcuts. */
    private static final boolean CMD_IS_CTRL = Util.getPlatform() == Util.OS.OSX;

    private static boolean isCtrl(int value) {
        return value == InputConstants.KEY_LCONTROL || value == InputConstants.KEY_RCONTROL
            || CMD_IS_CTRL && (value == InputConstants.KEY_LGUI || value == InputConstants.KEY_RGUI);
    }

    public static boolean isModifierKey(InputConstants.Key key) {
        if (key.getType() != InputConstants.Type.KEYBOARD) {
            return false;
        }
        int value = key.getValue();
        return isCtrl(value)
            || value == InputConstants.KEY_LSHIFT || value == InputConstants.KEY_RSHIFT
            || value == InputConstants.KEY_LALT || value == InputConstants.KEY_RALT;
    }

    private static @Nullable InputWithModifiers currentInput;
    private static int heldFromEvents;

    /** Keeps track of Ctrl, Shift and Alt from key events, in case the event's own flags are missing. */
    public static void onKey(InputConstants.Key key, boolean down) {
        if (!isModifierKey(key)) {
            return;
        }
        int value = key.getValue();
        int bit = isCtrl(value) ? CTRL
            : value == InputConstants.KEY_LSHIFT || value == InputConstants.KEY_RSHIFT ? SHIFT : ALT;
        heldFromEvents = down ? heldFromEvents | bit : heldFromEvents & ~bit;
    }

    /** While a key or mouse event is handled, modifiers are read from that event. */
    public static void setCurrentInput(@Nullable InputWithModifiers input) {
        currentInput = input;
    }

    public static int modifiersOf(InputWithModifiers input) {
        int modifiers = 0;
        if (input.hasControlDown()) {
            modifiers |= CTRL;
        }
        if (input.hasShiftDown()) {
            modifiers |= SHIFT;
        }
        if (input.hasAltDown()) {
            modifiers |= ALT;
        }
        return modifiers;
    }

    /** The modifiers held right now. */
    public static int pressedModifiers() {
        int modifiers = heldFromEvents;
        if (currentInput != null) {
            return modifiers | modifiersOf(currentInput);
        }
        if (InputConstants.isKeyDown(InputConstants.KEY_LCONTROL) || InputConstants.isKeyDown(InputConstants.KEY_RCONTROL)
            || CMD_IS_CTRL && (InputConstants.isKeyDown(InputConstants.KEY_LGUI) || InputConstants.isKeyDown(InputConstants.KEY_RGUI))) {
            modifiers |= CTRL;
        }
        if (InputConstants.isKeyDown(InputConstants.KEY_LSHIFT) || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT)) {
            modifiers |= SHIFT;
        }
        if (InputConstants.isKeyDown(InputConstants.KEY_LALT) || InputConstants.isKeyDown(InputConstants.KEY_RALT)) {
            modifiers |= ALT;
        }
        return modifiers;
    }

    /** Whether the key and exactly these modifiers are held right now. */
    public boolean isDown() {
        if (this.isUnbound() || pressedModifiers() != this.modifiers) {
            return false;
        }
        if (this.key.getType() == InputConstants.Type.MOUSE) {
            var mouse = Minecraft.getInstance().mouseHandler;
            return switch (this.key.getValue()) {
                case InputConstants.MOUSE_BUTTON_LEFT -> mouse.isLeftPressed();
                case InputConstants.MOUSE_BUTTON_MIDDLE -> mouse.isMiddlePressed();
                case InputConstants.MOUSE_BUTTON_RIGHT -> mouse.isRightPressed();
                default -> false;
            };
        }
        return InputConstants.isKeyDown(this.key.getValue());
    }

    public Component describe() {
        if (this.isUnbound()) {
            return Component.literal("Unbound");
        }
        StringBuilder prefix = new StringBuilder();
        if ((this.modifiers & CTRL) != 0) {
            prefix.append(CMD_IS_CTRL ? "Cmd+" : "Ctrl+");
        }
        if ((this.modifiers & SHIFT) != 0) {
            prefix.append("Shift+");
        }
        if ((this.modifiers & ALT) != 0) {
            prefix.append("Alt+");
        }
        return Component.literal(prefix.toString()).append(this.key.getDisplayName());
    }
}
