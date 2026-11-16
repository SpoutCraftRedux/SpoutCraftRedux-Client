package org.spoutcraftredux.client.controls;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.util.Util;
import org.slf4j.Logger;

/**
 * The player's shortcuts: loaded from and saved to {@code config/spoutcraftredux/shortcuts.json}, and run when
 * their key is released while no screen is open.
 */
public final class Shortcuts {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<Shortcut> SHORTCUTS = new ArrayList<>();
    private static final Set<Shortcut> HELD = new HashSet<>();
    private static final Deque<Pending> QUEUE = new ArrayDeque<>();

    private record Pending(String command, long sendAt) {
    }

    private Shortcuts() {
    }

    public static List<Shortcut> all() {
        return SHORTCUTS;
    }

    /** Adds the shortcut, or replaces it if it is already in the list. */
    public static void put(Shortcut shortcut) {
        if (!SHORTCUTS.contains(shortcut)) {
            SHORTCUTS.add(shortcut);
        }
        save();
    }

    public static void remove(Shortcut shortcut) {
        SHORTCUTS.remove(shortcut);
        save();
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("spoutcraftredux").resolve("shortcuts.json");
    }

    public static void load() {
        SHORTCUTS.clear();
        Path file = file();
        if (!Files.exists(file)) {
            return;
        }
        try {
            JsonArray array = JsonParser.parseString(Files.readString(file)).getAsJsonArray();
            for (JsonElement element : array) {
                JsonObject object = element.getAsJsonObject();
                Shortcut shortcut = new Shortcut();
                shortcut.setTitle(object.get("title").getAsString());
                shortcut.setKey(new KeyCombo(InputConstants.getKey(object.get("key").getAsString()), object.get("modifiers").getAsInt()));
                shortcut.setDelayMillis(object.get("delay").getAsInt());
                for (JsonElement command : object.getAsJsonArray("commands")) {
                    shortcut.getCommands().add(command.getAsString());
                }
                SHORTCUTS.add(shortcut);
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Could not read {}", file, e);
        }
    }

    public static void save() {
        JsonArray array = new JsonArray();
        for (Shortcut shortcut : SHORTCUTS) {
            JsonObject object = new JsonObject();
            object.addProperty("title", shortcut.getTitle());
            object.addProperty("key", shortcut.getKey().key().getName());
            object.addProperty("modifiers", shortcut.getKey().modifiers());
            object.addProperty("delay", shortcut.getDelayMillis());
            JsonArray commands = new JsonArray();
            shortcut.getCommands().forEach(commands::add);
            object.add("commands", commands);
            array.add(object);
        }
        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(array));
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", file, e);
        }
    }

    /** Called every client tick. */
    public static void tick(Minecraft minecraft) {
        ClientPacketListener connection = minecraft.getConnection();
        if (connection == null) {
            HELD.clear();
            QUEUE.clear();
            return;
        }
        if (minecraft.gui.screen() == null) {
            for (Shortcut shortcut : SHORTCUTS) {
                boolean down = shortcut.getKey().isDown();
                // Fires on release, so holding the key doesn't repeat it
                if (!down && HELD.remove(shortcut)) {
                    run(shortcut);
                } else if (down) {
                    HELD.add(shortcut);
                }
            }
        } else {
            HELD.clear();
        }

        long now = Util.getMillis();
        while (!QUEUE.isEmpty() && QUEUE.peek().sendAt() <= now) {
            send(connection, QUEUE.poll().command());
        }
    }

    private static void run(Shortcut shortcut) {
        long at = Util.getMillis();
        for (String command : shortcut.getCommands()) {
            QUEUE.add(new Pending(command, at));
            at += shortcut.getDelayMillis();
        }
    }

    private static void send(ClientPacketListener connection, String command) {
        if (command.startsWith("/")) {
            connection.sendCommand(command.substring(1));
        } else {
            connection.sendChat(command);
        }
    }
}
