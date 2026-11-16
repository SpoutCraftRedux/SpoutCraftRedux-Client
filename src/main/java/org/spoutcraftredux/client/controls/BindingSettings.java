package org.spoutcraftredux.client.controls;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

/**
 * Extra settings for Minecraft's own key bindings, saved in {@code config/spoutcraftredux/bindings.json}: the
 * Ctrl, Shift and Alt modifiers a binding needs, and whether an F3 binding works without F3.
 */
public final class BindingSettings {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Integer> MODIFIERS = new HashMap<>();
    private static final Map<String, Boolean> STANDALONE = new HashMap<>();

    private BindingSettings() {
    }

    public static int modifiers(KeyMapping mapping) {
        return MODIFIERS.getOrDefault(mapping.getName(), 0);
    }

    public static boolean isStandalone(KeyMapping mapping) {
        return STANDALONE.getOrDefault(mapping.getName(), false);
    }

    public static void set(KeyMapping mapping, int modifiers, boolean standalone) {
        if (modifiers == 0) {
            MODIFIERS.remove(mapping.getName());
        } else {
            MODIFIERS.put(mapping.getName(), modifiers);
        }
        if (standalone) {
            STANDALONE.put(mapping.getName(), true);
        } else {
            STANDALONE.remove(mapping.getName());
        }
        save();
    }

    /** Whether the binding may trigger now: true unless it needs modifiers that aren't held. */
    public static boolean allows(KeyMapping mapping) {
        int needed = modifiers(mapping);
        return needed == 0 || KeyCombo.pressedModifiers() == needed;
    }

    public static boolean isDebug(KeyMapping mapping) {
        var options = Minecraft.getInstance().options;
        return mapping.getCategory() == KeyMapping.Category.DEBUG
            && mapping != options.keyDebugModifier && mapping != options.keyDebugOverlay;
    }

    /** Whether the F3 key (or whatever the debug modifier is bound to) is held. */
    public static boolean isDebugModifierHeld() {
        KeyMapping modifier = Minecraft.getInstance().options.keyDebugModifier;
        if (modifier.isUnbound()) {
            return false;
        }
        InputConstants.Key key = InputConstants.getKey(modifier.saveString());
        return key.getType() == InputConstants.Type.KEYBOARD && InputConstants.isKeyDown(key.getValue());
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("spoutcraftredux").resolve("bindings.json");
    }

    public static void load() {
        MODIFIERS.clear();
        STANDALONE.clear();
        Path file = file();
        if (!Files.exists(file)) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                JsonObject binding = entry.getValue().getAsJsonObject();
                if (binding.has("modifiers") && binding.get("modifiers").getAsInt() != 0) {
                    MODIFIERS.put(entry.getKey(), binding.get("modifiers").getAsInt());
                }
                if (binding.has("standalone") && binding.get("standalone").getAsBoolean()) {
                    STANDALONE.put(entry.getKey(), true);
                }
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Could not read {}", file, e);
        }
    }

    private static void save() {
        JsonObject root = new JsonObject();
        MODIFIERS.forEach((name, modifiers) -> binding(root, name).addProperty("modifiers", modifiers));
        STANDALONE.forEach((name, standalone) -> binding(root, name).addProperty("standalone", standalone));
        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(root));
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", file, e);
        }
    }

    private static JsonObject binding(JsonObject root, String name) {
        if (!root.has(name)) {
            root.add(name, new JsonObject());
        }
        return root.getAsJsonObject(name);
    }
}
