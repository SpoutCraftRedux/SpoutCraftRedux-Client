package org.spoutcraftredux.client.gui.dev;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spoutcraftredux.client.gui.widget.Button;
import org.spoutcraftredux.client.gui.widget.CheckBox;
import org.spoutcraftredux.client.gui.widget.Gradient;
import org.spoutcraftredux.client.gui.widget.ItemWidget;
import org.spoutcraftredux.client.gui.widget.Label;
import org.spoutcraftredux.client.gui.widget.ListView;
import org.spoutcraftredux.client.gui.widget.ScrollArea;
import org.spoutcraftredux.client.gui.widget.Slider;
import org.spoutcraftredux.client.gui.widget.TextField;
import org.spoutcraftredux.client.gui.widget.WidgetScreen;

/** Shows every widget, for checking how they look. Only reachable in a development environment. */
public class WidgetDemoScreen extends WidgetScreen {
    private Label title;
    private Button button;
    private Button disabled;
    private CheckBox checked;
    private CheckBox unchecked;
    private TextField field;
    private TextField empty;
    private Slider slider;
    private Gradient gradient;
    private ItemWidget item;
    private ListView list;
    private ScrollArea area;

    public WidgetDemoScreen() {
        super(Component.literal("Widgets"), null);
    }

    @Override
    protected void createWidgets() {
        this.title = this.add(new Label(Component.literal("Widgets")));
        this.button = this.add(new Button(Component.literal("Button")));
        this.disabled = (Button) this.add(new Button(Component.literal("Disabled"))).setEnabled(false);
        this.checked = this.add(new CheckBox(Component.literal("Checked")).setChecked(true));
        this.unchecked = this.add(new CheckBox(Component.literal("Unchecked")));
        this.field = this.add(new TextField().setText("Some text"));
        this.empty = this.add(new TextField().setPlaceholder(Component.literal("Search...")));
        this.slider = this.add(new Slider(Component.literal("Slider: 40%")).setPosition(0.4F));
        this.gradient = this.add(new Gradient(0xFF3050A0, 0xFF101030));
        this.item = this.add(new ItemWidget(new ItemStack(Items.DIAMOND_SWORD)));
        List<ListView.Item> rows = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            rows.add(new ListView.TextItem(Component.literal("Row " + i), Component.literal("Second line " + i)));
        }
        this.list = this.add(new ListView().setItems(rows).setSelected(1));
        this.area = this.add(new ScrollArea());
        this.area.add(new CheckBox(Component.literal("Minecraft Bindings")).setChecked(true).setBounds(5, 5, 100, 20));
        this.area.add(new CheckBox(Component.literal("Custom Bindings")).setChecked(true).setBounds(5, 30, 100, 20));
        this.area.add(new CheckBox(Component.literal("Shortcuts")).setBounds(5, 55, 100, 20));
    }

    @Override
    protected void layoutWidgets() {
        this.title.setX(this.width / 2 - this.font.width("Widgets") / 2).setY(10);
        this.button.setBounds(10, 30, 120, 20);
        this.disabled.setBounds(10, 55, 120, 20);
        this.checked.setBounds(10, 80, 100, 20);
        this.unchecked.setBounds(10, 105, 100, 20);
        this.field.setBounds(10, 131, 120, 20);
        this.empty.setBounds(10, 157, 120, 20);
        this.slider.setBounds(10, 182, 120, 20);
        this.gradient.setBounds(10, 207, 50, 20);
        this.item.setBounds(70, 207, 20, 20);
        this.list.setBounds(145, 30, 150, 120);
        this.area.setBounds(145, 160, 150, 70);
    }
}
