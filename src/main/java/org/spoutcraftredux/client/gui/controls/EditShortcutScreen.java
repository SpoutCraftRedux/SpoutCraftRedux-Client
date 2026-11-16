package org.spoutcraftredux.client.gui.controls;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spoutcraftredux.client.controls.KeyCombo;
import org.spoutcraftredux.client.controls.Shortcut;
import org.spoutcraftredux.client.controls.Shortcuts;
import org.spoutcraftredux.client.gui.widget.Button;
import org.spoutcraftredux.client.gui.widget.Label;
import org.spoutcraftredux.client.gui.widget.ListView;
import org.spoutcraftredux.client.gui.widget.TextField;
import org.spoutcraftredux.client.gui.widget.WidgetScreen;

/**
 * Edits a shortcut: its name, its key, the chat messages or commands it sends, and the delay between them.
 * Labels turn red while something required is missing.
 */
public class EditShortcutScreen extends WidgetScreen {
    private static final int RED = 0xFFFF0000;
    private static final int WHITE = 0xFFFFFFFF;

    private final ControlsScreen controls;
    private final Shortcut original;
    private final Shortcut shortcut;

    private Button record;
    private Label nameLabel;
    private TextField name;
    private Label commandLabel;
    private TextField command;
    private Label delayLabel;
    private TextField delay;
    private ListView commands;
    private Button done;
    private Button add;
    private Button edit;
    private Button remove;

    private boolean recording;
    private int editingIndex = -1;

    public EditShortcutScreen(ControlsScreen controls, Shortcut shortcut) {
        super(Component.literal("Edit Shortcut"), controls);
        this.controls = controls;
        this.original = shortcut;
        this.shortcut = shortcut.copy();
    }

    /** A command in the list, with "Command" or "Chat Message" under it. */
    private record CommandItem(String command) implements ListView.Item {
        @Override
        public int height() {
            return 23;
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height) {
            graphics.text(font, this.command, x + 2, y + 2, 0xFFFFFFFF, true);
            graphics.text(font, this.command.startsWith("/") ? "Command" : "Chat Message", x + 2, y + 13, 0xFFAAAAAA, true);
        }
    }

    @Override
    protected void createWidgets() {
        this.record = this.add(new Button(Component.empty()));
        this.record.onClick(() -> {
            this.recording = !this.recording;
            this.updateRecord();
        });
        this.nameLabel = this.add(new Label(Component.literal("Name:")));
        this.name = this.add(new TextField().setText(this.shortcut.getTitle()).setPlaceholder(Component.literal("Enter a name here")));
        this.commandLabel = this.add(new Label(Component.literal("Command:")));
        this.command = this.add(new TextField().setPlaceholder(Component.literal("Enter new command here, then click \"Add Command\"")));
        this.delayLabel = this.add(new Label(Component.literal("Delay (ms)")));
        this.delay = this.add(new TextField().setText(Integer.toString(this.shortcut.getDelayMillis())));
        this.delay.setFilter(text -> text.chars().allMatch(Character::isDigit)).setMaxLength(6);

        this.commands = this.add(new ListView());
        this.commands.onSelect(item -> this.updateButtons());
        this.commands.onActivate(item -> this.editCommand(this.commands.items().indexOf(item)));

        this.done = this.add(new Button(Component.literal("Done")));
        this.done.onClick(this::finish);
        this.add = this.add(new Button(Component.literal("Add Command")));
        this.add.onClick(this::addCommand);
        this.edit = this.add(new Button(Component.literal("Edit Command")));
        this.edit.onClick(() -> this.editCommand(this.selectedIndex()));
        this.remove = this.add(new Button(Component.literal("Remove Command")));
        this.remove.onClick(() -> {
            int index = this.selectedIndex();
            if (index >= 0) {
                this.shortcut.getCommands().remove(index);
                this.updateCommands();
            }
        });

        this.updateRecord();
        this.updateCommands();
    }

    @Override
    protected void layoutWidgets() {
        int labelWidth = 75;
        int top = 10;
        int left = 10;
        int right = left + labelWidth + 10;
        this.record.setBounds(this.width - 160, top - 3, 150, 20);
        this.nameLabel.setBounds(left, top + 3, labelWidth, 20);
        this.name.setBounds(right, top - 1, this.width - right - 150 - 20, 16);
        top += 23;
        this.commandLabel.setBounds(left, top + 3, labelWidth, 20);
        this.command.setBounds(right, top - 1, this.width - right - 10, 16);
        top += 23;
        this.delayLabel.setBounds(left, top + 3, labelWidth, 20);
        this.delay.setBounds(right, top - 1, this.width - right - 10, 16);
        top += 23;
        this.commands.setBounds(0, top, this.width, this.height - top - 30);
        this.done.setBounds(10, this.height - 25, 50, 20);
        this.add.setBounds(70, this.height - 25, 100, 20);
        this.edit.setBounds(180, this.height - 25, 100, 20);
        this.remove.setBounds(290, this.height - 25, 100, 20);
    }

    @Override
    public void tick() {
        super.tick();
        this.add.setEnabled(!this.name.getText().isEmpty() && !this.command.getText().isEmpty());
        this.nameLabel.setColor(this.name.getText().isEmpty() ? RED : WHITE);
        this.delayLabel.setColor(this.delay.getText().isEmpty() ? RED : WHITE);
        this.commandLabel.setColor(this.shortcut.getCommands().isEmpty() ? RED : WHITE);
        this.updateRecord();
    }

    private void updateRecord() {
        String text;
        if (this.recording) {
            text = "> Press a key! <";
        } else if (this.shortcut.getKey().isUnbound()) {
            text = "Click Here!";
        } else {
            text = "Key: " + this.shortcut.getKey().describe().getString();
        }
        this.record.setText(Component.literal(text).withStyle(this.shortcut.getKey().isUnbound() ? ChatFormatting.RED : ChatFormatting.WHITE));
    }

    private int selectedIndex() {
        ListView.Item selected = this.commands.getSelected();
        return selected == null ? -1 : this.commands.items().indexOf(selected);
    }

    private void updateCommands() {
        this.commands.setItems(this.shortcut.getCommands().stream().map(CommandItem::new).toList());
        this.updateButtons();
    }

    private void updateButtons() {
        int index = this.selectedIndex();
        this.edit.setEnabled(index >= 0);
        this.remove.setEnabled(index >= 0 && this.editingIndex < 0);
    }

    private void editCommand(int index) {
        if (index >= 0) {
            this.editingIndex = index;
            this.command.setText(this.shortcut.getCommands().get(index));
            this.add.setText(Component.literal("Finish Editing"));
            this.updateButtons();
        }
    }

    private void addCommand() {
        if (this.editingIndex >= 0) {
            this.shortcut.getCommands().set(this.editingIndex, this.command.getText());
            this.editingIndex = -1;
            this.add.setText(Component.literal("Add Command"));
        } else {
            this.shortcut.getCommands().add(this.command.getText());
        }
        this.command.setText("");
        this.updateCommands();
    }

    private void finish() {
        this.shortcut.setTitle(this.name.getText());
        this.shortcut.setDelayMillis(this.delay.getText().isEmpty() ? 0 : Integer.parseInt(this.delay.getText()));
        if (!this.shortcut.getTitle().isEmpty() && !this.shortcut.getKey().isUnbound()) {
            this.original.setTitle(this.shortcut.getTitle());
            this.original.setKey(this.shortcut.getKey());
            this.original.setDelayMillis(this.shortcut.getDelayMillis());
            this.original.getCommands().clear();
            this.original.getCommands().addAll(this.shortcut.getCommands());
            Shortcuts.put(this.original);
        }
        this.minecraft.gui.setScreen(this.controls);
        this.controls.refresh();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        InputConstants.Key key = InputConstants.getKey(event);
        if (this.recording && !KeyCombo.isModifierKey(key)) {
            this.shortcut.setKey(event.isEscape() ? KeyCombo.UNBOUND : new KeyCombo(key, KeyCombo.pressedModifiers()));
            this.recording = false;
            this.updateRecord();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.recording && !this.record.contains(event.x(), event.y())) {
            this.shortcut.setKey(new KeyCombo(InputConstants.Type.MOUSE.getOrCreate(event.button()), KeyCombo.pressedModifiers()));
            this.recording = false;
            this.updateRecord();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
