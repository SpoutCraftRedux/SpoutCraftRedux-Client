package org.spoutcraftredux.client.controls;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.spoutcraftredux.client.network.RawPayload;
import org.spoutcraftredux.client.network.SpoutCraftReduxChannels;
import org.spoutcraftredux.protocol.message.KeyBindingPressed;
import org.spoutcraftredux.protocol.message.KeyBindings;

/**
 * The key bindings the current server declared. The player's own key choices are saved per server in
 * {@code config/spoutcraftredux/server_bindings.json}. Presses are sent while no screen is open.
 */
public final class ServerKeyBindings {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<Binding> BINDINGS = new ArrayList<>();
    private static final Set<Binding> HELD = new HashSet<>();

    /** A server binding and the key it uses on this client. */
    public static final class Binding {
        private final String id;
        private final Component name;
        private final String pluginName;
        private final KeyCombo defaultKey;
        private KeyCombo key;

        Binding(String id, Component name, String pluginName, KeyCombo defaultKey, KeyCombo key) {
            this.id = id;
            this.name = name;
            this.pluginName = pluginName;
            this.defaultKey = defaultKey;
            this.key = key;
        }

        public String id() {
            return this.id;
        }

        public Component name() {
            return this.name;
        }

        public String pluginName() {
            return this.pluginName;
        }

        public KeyCombo key() {
            return this.key;
        }

        public KeyCombo defaultKey() {
            return this.defaultKey;
        }

        public void setKey(KeyCombo key) {
            this.key = key;
            save();
        }
    }

    private ServerKeyBindings() {
    }

    public static List<Binding> all() {
        return BINDINGS;
    }

    /** Replaces the list with what the server sent, applying the player's saved keys. */
    public static void receive(KeyBindings message, java.util.function.Function<String, Component> parseName) {
        BINDINGS.clear();
        HELD.clear();
        JsonObject saved = load().getAsJsonObject(serverId());
        for (KeyBindings.Binding wire : message.bindings()) {
            KeyCombo fallback = wire.defaultKey().isEmpty()
                ? KeyCombo.UNBOUND
                : new KeyCombo(InputConstants.getKey(wire.defaultKey()), wire.defaultModifiers());
            KeyCombo key = fallback;
            if (saved != null && saved.has(wire.id())) {
                JsonObject choice = saved.getAsJsonObject(wire.id());
                key = new KeyCombo(InputConstants.getKey(choice.get("key").getAsString()), choice.get("modifiers").getAsInt());
            }
            BINDINGS.add(new Binding(wire.id(), parseName.apply(wire.nameJson()), wire.pluginName(), fallback, key));
        }
    }

    public static void clear() {
        BINDINGS.clear();
        HELD.clear();
    }

    /** Called every client tick: sends presses and releases while no screen is open. */
    public static void tick(Minecraft minecraft) {
        if (BINDINGS.isEmpty() || minecraft.getConnection() == null) {
            return;
        }
        boolean inGame = minecraft.gui.screen() == null;
        for (Binding binding : BINDINGS) {
            boolean down = inGame && binding.key().isDown();
            if (down && HELD.add(binding)) {
                send(binding, true);
            } else if (!down && HELD.remove(binding)) {
                send(binding, false);
            }
        }
    }

    private static void send(Binding binding, boolean pressed) {
        byte[] data = new KeyBindingPressed(binding.id(), pressed).encode();
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new RawPayload(SpoutCraftReduxChannels.KEY_BINDING_PRESSED, data));
    }

    /** The current server's address, or "singleplayer". */
    private static String serverId() {
        ServerData server = Minecraft.getInstance().getCurrentServer();
        return server == null ? "singleplayer" : server.ip;
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("spoutcraftredux").resolve("server_bindings.json");
    }

    private static JsonObject load() {
        Path file = file();
        if (Files.exists(file)) {
            try {
                return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            } catch (IOException | RuntimeException e) {
                LOGGER.warn("Could not read {}", file, e);
            }
        }
        return new JsonObject();
    }

    private static void save() {
        JsonObject root = load();
        JsonObject server = new JsonObject();
        for (Binding binding : BINDINGS) {
            if (!binding.key().equals(binding.defaultKey())) {
                JsonObject choice = new JsonObject();
                choice.addProperty("key", binding.key().key().getName());
                choice.addProperty("modifiers", binding.key().modifiers());
                server.add(binding.id(), choice);
            }
        }
        root.add(serverId(), server);
        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(root));
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", file, e);
        }
    }
}
