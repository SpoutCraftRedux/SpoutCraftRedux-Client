package org.spoutcraftredux.client.gui.controls;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.spoutcraftredux.client.controls.KeyCombo;

/** A Minecraft key binding, including those added by other mods. */
public final class VanillaBindingItem extends ControlsItem {
    private final KeyMapping mapping;

    public VanillaBindingItem(ControlsScreen screen, KeyMapping mapping) {
        super(screen);
        this.mapping = mapping;
    }

    @Override
    public Component getName() {
        return Component.translatable(this.mapping.getName());
    }

    @Override
    public KeyCombo getKey() {
        return this.mapping.isUnbound() ? KeyCombo.UNBOUND : new KeyCombo(InputConstants.getKey(this.mapping.saveString()), 0);
    }

    @Override
    protected Component describeKey() {
        Options options = Minecraft.getInstance().options;
        boolean needsModifier = this.mapping.getCategory() == KeyMapping.Category.DEBUG
            && this.mapping != options.keyDebugModifier && this.mapping != options.keyDebugOverlay;
        if (needsModifier && !this.mapping.isUnbound() && !options.keyDebugModifier.isUnbound()) {
            // F3 combinations: shown as the debug modifier plus this key, like F3+L
            return Component.empty().append(options.keyDebugModifier.getTranslatedKeyMessage()).append("+").append(super.describeKey());
        }
        return super.describeKey();
    }

    @Override
    public void setKey(KeyCombo key) {
        this.mapping.setKey(key.key());
        Minecraft.getInstance().options.save();
        KeyMapping.resetMapping();
    }

    @Override
    public boolean conflicts(ControlsItem other) {
        // Minecraft overlaps some F3 combinations on purpose, and spectator bindings only work in spectator mode
        if (this.mapping.getCategory() == KeyMapping.Category.DEBUG
            || other instanceof VanillaBindingItem vanilla && vanilla.mapping.getCategory() == KeyMapping.Category.DEBUG) {
            return false;
        }
        KeyMapping.Category otherContext = other instanceof VanillaBindingItem vanilla ? context(vanilla.mapping) : null;
        return context(this.mapping) == otherContext && super.conflicts(other);
    }

    private static KeyMapping.Category context(KeyMapping mapping) {
        KeyMapping.Category category = mapping.getCategory();
        return category == KeyMapping.Category.SPECTATOR ? category : null;
    }

    @Override
    public boolean useModifiers() {
        return false;
    }

    @Override
    protected String letter() {
        return "V";
    }

    @Override
    protected int letterColor() {
        return 0xFFFFFF00;
    }
}
