package org.spoutcraftredux.client.gui.controls;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spoutcraftredux.client.controls.BindingSettings;
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
        if (this.mapping.isUnbound()) {
            return KeyCombo.UNBOUND;
        }
        return new KeyCombo(InputConstants.getKey(this.mapping.saveString()), BindingSettings.modifiers(this.mapping));
    }

    /** An F3 action still bound as an F3 combination rather than on its own. */
    private boolean needsDebugModifier() {
        return BindingSettings.isDebug(this.mapping) && !BindingSettings.isStandalone(this.mapping);
    }

    /** Debug bindings, including F3 itself, unless the player made this one a key of its own. */
    private boolean inDebugGroup() {
        return this.mapping.getCategory() == KeyMapping.Category.DEBUG && !BindingSettings.isStandalone(this.mapping);
    }

    @Override
    protected Component describeKey() {
        KeyMapping modifier = Minecraft.getInstance().options.keyDebugModifier;
        if (this.needsDebugModifier() && !this.mapping.isUnbound() && !modifier.isUnbound()) {
            return Component.empty().append(modifier.getTranslatedKeyMessage()).append("+").append(super.describeKey());
        }
        return super.describeKey();
    }

    @Override
    public void setKey(KeyCombo key) {
        // An F3 action pressed without F3 held becomes a key of its own
        boolean standalone = BindingSettings.isDebug(this.mapping) && !key.isUnbound() && !BindingSettings.isDebugModifierHeld();
        this.mapping.setKey(key.key());
        BindingSettings.set(this.mapping, key.modifiers(), standalone);
        Minecraft.getInstance().options.save();
        KeyMapping.resetMapping();
    }

    @Override
    public boolean waitsFor(InputConstants.Key key) {
        if (BindingSettings.isDebug(this.mapping)) {
            KeyMapping modifier = Minecraft.getInstance().options.keyDebugModifier;
            if (!modifier.isUnbound() && key.equals(InputConstants.getKey(modifier.saveString()))) {
                return true;
            }
        }
        return super.waitsFor(key);
    }

    @Override
    public boolean conflicts(ControlsItem other) {
        // The F3 group overlaps on purpose, and spectator bindings only work in spectator mode
        if (this.inDebugGroup() || other instanceof VanillaBindingItem vanilla && vanilla.inDebugGroup()) {
            return false;
        }
        KeyMapping.Category otherContext = other instanceof VanillaBindingItem vanilla ? context(vanilla.mapping) : null;
        return context(this.mapping) == otherContext && super.conflicts(other);
    }

    private static KeyMapping.@Nullable Category context(KeyMapping mapping) {
        return mapping.getCategory() == KeyMapping.Category.SPECTATOR ? KeyMapping.Category.SPECTATOR : null;
    }

    @Override
    public boolean useModifiers() {
        return true;
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
