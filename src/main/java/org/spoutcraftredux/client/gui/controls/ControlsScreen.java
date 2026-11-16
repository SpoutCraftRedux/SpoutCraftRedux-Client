package org.spoutcraftredux.client.gui.controls;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spoutcraftredux.client.controls.KeyCombo;
import org.spoutcraftredux.client.controls.ServerKeyBindings;
import org.spoutcraftredux.client.controls.Shortcut;
import org.spoutcraftredux.client.controls.Shortcuts;
import org.spoutcraftredux.client.gui.widget.Button;
import org.spoutcraftredux.client.gui.widget.CheckBox;
import org.spoutcraftredux.client.gui.widget.Label;
import org.spoutcraftredux.client.gui.widget.ListView;
import org.spoutcraftredux.client.gui.widget.SafeButton;
import org.spoutcraftredux.client.gui.widget.ScrollArea;
import org.spoutcraftredux.client.gui.widget.TextField;
import org.spoutcraftredux.client.gui.widget.WidgetScreen;

/**
 * Every key binding in one list: Minecraft's, SpoutCraft Redux's own, the server's and the player's shortcuts.
 * Double-click a row, then press a key (with Ctrl, Shift or Alt where allowed) or a mouse button. Escape unbinds.
 */
public class ControlsScreen extends WidgetScreen {
    private Label title;
    private TextField search;
    private ScrollArea filter;
    private CheckBox showVanilla;
    private CheckBox showSpoutCraftRedux;
    private CheckBox showCustom;
    private CheckBox showShortcuts;
    private ListView view;
    private Button add;
    private Button edit;
    private SafeButton remove;
    private Label description;
    private Button done;

    private @Nullable ControlsItem editing;

    public ControlsScreen(@Nullable Screen parent) {
        super(Component.literal("Controls"), parent);
    }

    @Override
    protected void createWidgets() {
        this.title = this.add(new Label(Component.literal("Controls")));
        this.search = this.add(new TextField().setPlaceholder(Component.literal("Search").withStyle(ChatFormatting.GRAY)));
        this.search.onChange(text -> this.refresh());

        this.showVanilla = this.filterBox("Minecraft Bindings", ChatFormatting.YELLOW);
        this.showSpoutCraftRedux = this.filterBox("SpoutCraft Redux", ChatFormatting.RED);
        this.showCustom = this.filterBox("Custom Bindings", ChatFormatting.BLUE);
        this.showShortcuts = this.filterBox("Shortcuts", ChatFormatting.GREEN);
        this.filter = this.add(new ScrollArea());
        this.filter.add(this.showVanilla).add(this.showSpoutCraftRedux).add(this.showCustom).add(this.showShortcuts);

        this.view = this.add(new ListView());
        this.view.onSelect(item -> this.updateButtons());
        this.view.onActivate(item -> {
            ControlsItem row = (ControlsItem) item;
            this.editing = this.editing == row ? null : row;
        });

        this.add = this.add(new Button(Component.literal("Add Shortcut")));
        this.add.onClick(() -> this.openEditor(new Shortcut()));
        this.edit = this.add(new Button(Component.literal("Edit")));
        this.edit.setTooltip(Component.literal("Edit Shortcut"));
        this.edit.onClick(this::editSelected);
        this.remove = this.add(new SafeButton(Component.literal("Delete"), this::deleteSelected));
        this.remove.setTooltip(Component.literal("Remove Shortcut"));
        this.description = this.add(new Label(Component.literal("Double-click an item, then press the key (combination).")).setWrap(true));
        this.done = this.add(new Button(Component.literal("Done")));
        this.done.onClick(this::onClose);

        this.refresh();
    }

    private CheckBox filterBox(String text, ChatFormatting color) {
        CheckBox box = new CheckBox(Component.literal(text).withStyle(color)).setChecked(true);
        box.onChange(checked -> this.refresh());
        return box;
    }

    @Override
    protected void layoutWidgets() {
        int top = 5;
        int titleWidth = this.font.width(this.title.getText());
        this.title.setBounds(this.width / 2 - titleWidth / 2, top + 7, titleWidth, 11);
        this.search.setBounds(5, top, 130, 20);

        top += 25;
        int listHeight = this.height - top - 55;
        this.filter.setBounds(5, top, 130, listHeight);
        this.view.setBounds(140, top, this.width - 145, listHeight);
        int boxWidth = this.filter.viewportWidth() - 10;
        this.showVanilla.setBounds(5, 5, boxWidth, 20);
        this.showSpoutCraftRedux.setBounds(5, 30, boxWidth, 20);
        this.showCustom.setBounds(5, 55, boxWidth, 20);
        this.showShortcuts.setBounds(5, 80, boxWidth, 20);

        top += 5 + listHeight;
        int totalWidth = Math.min(this.width - 10, 200 * 3 + 10);
        int cellWidth = (totalWidth - 10) / 3;
        int left = this.width / 2 - totalWidth / 2;
        int center = left + 5 + cellWidth;
        int right = center + 5 + cellWidth;
        this.add.setBounds(left, top, cellWidth, 20);
        this.edit.setBounds(center, top, cellWidth, 20);
        this.remove.setBounds(right, top, cellWidth, 20);

        top += 25;
        this.description.setBounds(left, top, cellWidth * 2 + 5, 20);
        this.done.setBounds(right, top, cellWidth, 20);
    }

    /** Rebuilds the list from the filters and the search text. */
    public void refresh() {
        List<ControlsItem> items = new ArrayList<>();
        if (this.showVanilla.isChecked()) {
            for (KeyMapping mapping : this.minecraft.options.keyMappings) {
                items.add(new VanillaBindingItem(this, mapping));
            }
        }
        if (this.showCustom.isChecked()) {
            for (ServerKeyBindings.Binding binding : ServerKeyBindings.all()) {
                items.add(new ServerBindingItem(this, binding));
            }
        }
        if (this.showShortcuts.isChecked()) {
            for (Shortcut shortcut : Shortcuts.all()) {
                items.add(new ShortcutItem(this, shortcut));
            }
        }
        for (ControlsItem item : items) {
            // Both sides must agree, so exceptions like F3 combinations hold whichever row asks
            item.setConflicting(items.stream().anyMatch(other -> other != item && item.conflicts(other) && other.conflicts(item)));
        }
        String query = this.search.getText().toLowerCase(Locale.ROOT);
        if (!query.isEmpty()) {
            items.removeIf(item -> !item.getName().getString().toLowerCase(Locale.ROOT).contains(query));
        }
        this.view.setItems(items);
        this.editing = null;
        this.updateButtons();
    }

    private @Nullable ControlsItem selected() {
        return (ControlsItem) this.view.getSelected();
    }

    private void updateButtons() {
        this.edit.setEnabled(this.selected() != null);
        this.remove.disarm();
    }

    public boolean isEditing(ControlsItem item) {
        return this.editing == item;
    }

    private void editSelected() {
        ControlsItem item = this.selected();
        if (item instanceof ShortcutItem shortcut) {
            this.openEditor(shortcut.shortcut());
        } else if (item != null) {
            this.editing = item;
        }
    }

    private void deleteSelected() {
        ControlsItem item = this.selected();
        if (item instanceof ShortcutItem shortcut) {
            Shortcuts.remove(shortcut.shortcut());
        } else if (item != null) {
            item.setKey(KeyCombo.UNBOUND);
        }
        this.refresh();
    }

    private void openEditor(Shortcut shortcut) {
        this.minecraft.gui.setScreen(new EditShortcutScreen(this, shortcut));
    }

    private void finishEdit(KeyCombo key) {
        ControlsItem item = this.editing;
        if (item != null) {
            item.setKey(item.useModifiers() ? key : new KeyCombo(key.key(), 0));
        }
        this.refresh();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.editing == null) {
            return super.keyPressed(event);
        }
        InputConstants.Key key = InputConstants.getKey(event);
        if (event.isEscape()) {
            this.finishEdit(KeyCombo.UNBOUND);
        } else if (this.editing.waitsFor(key)) {
            // Wait: the modifier may be part of a combination, or the key itself if released alone
            return true;
        } else {
            this.finishEdit(new KeyCombo(key, KeyCombo.pressedModifiers()));
        }
        return true;
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        InputConstants.Key key = InputConstants.getKey(event);
        if (this.editing != null && this.editing.useModifiers() && KeyCombo.isModifierKey(key)) {
            // Only Ctrl, Shift and Alt can be bound on their own here; F3 always waits for another key
            // A modifier released on its own becomes the key
            this.finishEdit(new KeyCombo(key, 0));
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.editing != null) {
            this.finishEdit(new KeyCombo(InputConstants.Type.MOUSE.getOrCreate(event.button()), KeyCombo.pressedModifiers()));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        this.minecraft.options.save();
        super.onClose();
    }
}
