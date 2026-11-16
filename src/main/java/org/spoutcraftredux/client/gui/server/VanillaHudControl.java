package org.spoutcraftredux.client.gui.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;
import org.spoutcraftredux.protocol.gui.VanillaElements;
import org.spoutcraftredux.protocol.message.VanillaHud;

/** Hides and moves vanilla HUD elements as the server asks. */
public final class VanillaHudControl {
    private static final Map<String, List<Identifier>> ELEMENTS = Map.ofEntries(
        Map.entry(VanillaElements.HOTBAR, List.of(VanillaHudElements.HOTBAR)),
        Map.entry(VanillaElements.HEALTH, List.of(VanillaHudElements.HEALTH_BAR)),
        Map.entry(VanillaElements.ARMOR, List.of(VanillaHudElements.ARMOR_BAR)),
        Map.entry(VanillaElements.FOOD, List.of(VanillaHudElements.FOOD_BAR)),
        Map.entry(VanillaElements.AIR, List.of(VanillaHudElements.AIR_BAR)),
        Map.entry(VanillaElements.MOUNT_HEALTH, List.of(VanillaHudElements.MOUNT_HEALTH)),
        Map.entry(VanillaElements.EXPERIENCE_BAR, List.of(VanillaHudElements.INFO_BAR, VanillaHudElements.INFO_BAR_FOREGROUND)),
        Map.entry(VanillaElements.EXPERIENCE_LEVEL, List.of(VanillaHudElements.EXPERIENCE_LEVEL)),
        Map.entry(VanillaElements.CROSSHAIR, List.of(VanillaHudElements.CROSSHAIR)),
        Map.entry(VanillaElements.HELD_ITEM_NAME, List.of(VanillaHudElements.HELD_ITEM_TOOLTIP)),
        Map.entry(VanillaElements.EFFECTS, List.of(VanillaHudElements.MOB_EFFECTS)),
        Map.entry(VanillaElements.BOSS_BAR, List.of(VanillaHudElements.BOSS_BAR)),
        Map.entry(VanillaElements.SCOREBOARD, List.of(VanillaHudElements.SCOREBOARD)),
        Map.entry(VanillaElements.ACTION_BAR, List.of(VanillaHudElements.OVERLAY_MESSAGE)),
        Map.entry(VanillaElements.TITLE, List.of(VanillaHudElements.TITLE_AND_SUBTITLE)),
        Map.entry(VanillaElements.CHAT, List.of(VanillaHudElements.CHAT)),
        Map.entry(VanillaElements.PLAYER_LIST, List.of(VanillaHudElements.PLAYER_LIST)),
        Map.entry(VanillaElements.SUBTITLES, List.of(VanillaHudElements.SUBTITLES)));

    private static final Map<Identifier, VanillaHud.Element> SETTINGS = new HashMap<>();

    private VanillaHudControl() {
    }

    public static void register() {
        ELEMENTS.values().stream().flatMap(List::stream).forEach(id -> HudElementRegistry.replaceElement(id, original -> wrap(id, original)));
    }

    private static HudElement wrap(Identifier id, HudElement original) {
        return (graphics, delta) -> {
            VanillaHud.Element settings = SETTINGS.get(id);
            if (settings == null) {
                original.extractRenderState(graphics, delta);
            } else if (settings.visible()) {
                graphics.pose().pushMatrix();
                graphics.pose().translate(settings.x(), settings.y());
                original.extractRenderState(graphics, delta);
                graphics.pose().popMatrix();
            }
        };
    }

    public static void receive(VanillaHud message) {
        SETTINGS.clear();
        for (VanillaHud.Element element : message.elements()) {
            // Unknown names are from a newer server
            for (Identifier id : ELEMENTS.getOrDefault(element.name(), List.of())) {
                SETTINGS.put(id, element);
            }
        }
    }

    public static void clear() {
        SETTINGS.clear();
    }
}
