package org.spoutcraftredux.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * A scrollable list. The selected row gets a white outline around a black fill. Rows start 5 pixels down and are
 * drawn 5 pixels in from the left.
 */
public class ListView extends Scrollable {
    private static final int TOP = 5;

    private final List<Item> items = new ArrayList<>();
    private int selected = -1;
    private Consumer<Item> onSelect = item -> {
    };
    private Consumer<Item> onActivate = item -> {
    };

    /** A row in the list. */
    public interface Item {
        default int height() {
            return 20;
        }

        void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height);
    }

    /** A row with a white title and a grey line of text under it, both without shadow. */
    public record TextItem(Component title, Component text) implements Item {
        @Override
        public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height) {
            graphics.text(font, this.title, x + 2, y + 2, WidgetStyle.WHITE, false);
            graphics.text(font, this.text, x + 2, y + 10, WidgetStyle.LIST_SUBTITLE, false);
        }
    }

    public ListView setItems(List<? extends Item> items) {
        this.items.clear();
        this.items.addAll(items);
        if (this.selected >= this.items.size()) {
            this.selected = -1;
        }
        this.setScroll(this.scrollY);
        return this;
    }

    public List<Item> items() {
        return this.items;
    }

    public @Nullable Item getSelected() {
        return this.selected >= 0 ? this.items.get(this.selected) : null;
    }

    public ListView setSelected(int index) {
        this.selected = index >= 0 && index < this.items.size() ? index : -1;
        return this;
    }

    public ListView onSelect(Consumer<Item> onSelect) {
        this.onSelect = onSelect;
        return this;
    }

    public ListView onActivate(Consumer<Item> onActivate) {
        this.onActivate = onActivate;
        return this;
    }

    @Override
    protected int contentHeight() {
        int total = TOP;
        for (Item item : this.items) {
            total += item.height();
        }
        return total;
    }

    @Override
    protected void renderContents(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTick) {
        int viewport = this.viewportWidth();
        int top = this.scrollY;
        int bottom = this.scrollY + this.height + TOP;
        int rowY = TOP;
        for (int i = 0; i < this.items.size(); i++) {
            Item item = this.items.get(i);
            int rowHeight = item.height();
            if (rowY >= top - rowHeight && rowY <= bottom) {
                if (i == this.selected) {
                    graphics.fill(4, rowY - 1, viewport - 3, rowY + rowHeight + 1, WidgetStyle.WHITE);
                    graphics.fill(5, rowY, viewport - 4, rowY + rowHeight, 0xFF000000);
                }
                item.render(graphics, font, 5, rowY, viewport - 9, rowHeight);
            }
            rowY += rowHeight;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean doubleClick) {
        if (!this.visible || button != 0 || !this.inViewport(mouseX, mouseY)) {
            return false;
        }
        double localY = mouseY - this.y + this.scrollY;
        int rowY = TOP;
        for (int i = 0; i < this.items.size(); i++) {
            int rowHeight = this.items.get(i).height();
            if (localY >= rowY && localY < rowY + rowHeight) {
                this.selected = i;
                this.onSelect.accept(this.items.get(i));
                if (doubleClick) {
                    this.onActivate.accept(this.items.get(i));
                }
                return true;
            }
            rowY += rowHeight;
        }
        return true;
    }
}
