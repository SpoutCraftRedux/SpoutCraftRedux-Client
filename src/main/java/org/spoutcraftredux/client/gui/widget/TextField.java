package org.spoutcraftredux.client.gui.widget;

import java.util.function.Consumer;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;

/**
 * A single-line text field: a black box with a grey 1 pixel border, text 4 pixels in, and an underscore as the
 * cursor. The placeholder shows while it is empty and unfocused.
 */
public class TextField extends Widget {
    private static final int PADDING = 4;
    private static final int BORDER = 0xFFA0A0A0;
    private static final int FIELD = 0xFF000000;

    private String text = "";
    private Component placeholder = Component.empty();
    private int maxLength = 256;
    private int cursor;
    private Consumer<String> onChange = value -> {
    };
    private Consumer<String> onSubmit = value -> {
    };

    public TextField() {
        this.width = 150;
        this.height = 20;
    }

    public TextField setText(String text) {
        this.text = text.length() > this.maxLength ? text.substring(0, this.maxLength) : text;
        this.cursor = this.text.length();
        return this;
    }

    public String getText() {
        return this.text;
    }

    public TextField setPlaceholder(Component placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public TextField setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public TextField onChange(Consumer<String> onChange) {
        this.onChange = onChange;
        return this;
    }

    public TextField onSubmit(Consumer<String> onSubmit) {
        this.onSubmit = onSubmit;
        return this;
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    @Override
    public boolean wantsTextInput() {
        return true;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }
        graphics.fill(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, BORDER);
        graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, FIELD);

        int textX = this.x + PADDING;
        int textY = this.y + PADDING;
        int color = this.enabled ? WidgetStyle.TEXT : WidgetStyle.TEXT_DISABLED;
        int inner = this.width - PADDING * 2;

        // Keep the cursor in view by scrolling long text to the left
        int start = 0;
        while (start < this.cursor && font.width(this.text.substring(start, this.cursor) + "_") > inner) {
            start++;
        }
        String visible = font.plainSubstrByWidth(this.text.substring(start), inner);
        if (!this.text.isEmpty()) {
            graphics.text(font, visible, textX, textY, color, true);
        } else if (!this.focused) {
            graphics.text(font, this.placeholder, textX, textY, color, true);
        }

        boolean blink = (Util.getMillis() / 300L) % 2L == 0L;
        if (this.enabled && this.focused && blink) {
            int cursorX = textX + font.width(this.text.substring(start, this.cursor));
            graphics.text(font, "_", cursorX, textY + 1, color, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean doubleClick) {
        return this.visible && this.enabled && this.contains(mouseX, mouseY);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!this.focused || !this.enabled || !event.isAllowedChatCharacter()) {
            return false;
        }
        this.insert(event.codepointAsString());
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.focused || !this.enabled) {
            return false;
        }
        if (event.isPaste()) {
            this.insert(StringUtil.filterText(Minecraft.getInstance().keyboardHandler.getClipboard()));
            return true;
        }
        switch (event.key()) {
            case InputConstants.KEY_BACKSPACE -> {
                if (this.cursor > 0) {
                    this.text = this.text.substring(0, this.cursor - 1) + this.text.substring(this.cursor);
                    this.cursor--;
                    this.onChange.accept(this.text);
                }
                return true;
            }
            case InputConstants.KEY_DELETE -> {
                if (this.cursor < this.text.length()) {
                    this.text = this.text.substring(0, this.cursor) + this.text.substring(this.cursor + 1);
                    this.onChange.accept(this.text);
                }
                return true;
            }
            case InputConstants.KEY_LEFT -> {
                this.cursor = Math.max(0, this.cursor - 1);
                return true;
            }
            case InputConstants.KEY_RIGHT -> {
                this.cursor = Math.min(this.text.length(), this.cursor + 1);
                return true;
            }
            case InputConstants.KEY_HOME -> {
                this.cursor = 0;
                return true;
            }
            case InputConstants.KEY_END -> {
                this.cursor = this.text.length();
                return true;
            }
            case InputConstants.KEY_RETURN -> {
                this.onSubmit.accept(this.text);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void insert(String typed) {
        int room = this.maxLength - this.text.length();
        if (room <= 0 || typed.isEmpty()) {
            return;
        }
        String piece = typed.length() > room ? typed.substring(0, room) : typed;
        this.text = this.text.substring(0, this.cursor) + piece + this.text.substring(this.cursor);
        this.cursor += piece.length();
        this.onChange.accept(this.text);
    }
}
