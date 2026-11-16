package org.spoutcraftredux.client.content;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.slf4j.Logger;
import org.spoutcraftredux.client.mixin.DataComponentInitializersAccessor;
import org.spoutcraftredux.client.mixin.MappedRegistryAccessor;
import org.spoutcraftredux.client.network.NotificationHandler;
import org.spoutcraftredux.protocol.content.ContentKeys;
import org.spoutcraftredux.protocol.message.Content;

/**
 * Registers the server's custom items into the built-in item registry at the server's raw ids, and removes them on
 * disconnect so nothing leaks into the next server or singleplayer.
 */
public final class ClientContent {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<Item> ITEMS = new ArrayList<>();
    private static int vanillaItemCount = -1;
    private static int vanillaInitializerCount = -1;

    private ClientContent() {
    }

    /**
     * Registers the server's content, replacing any from a previous session.
     *
     * @param minecraft the client
     * @param content   the server's content
     * @return an empty string on success, otherwise the reason it failed
     */
    public static synchronized String receive(Minecraft minecraft, Content content) {
        clear();
        List<Content.Entry> items = content.entries().stream()
            .filter(entry -> entry.registry().equals(ContentKeys.ITEM))
            .sorted(Comparator.comparingInt(Content.Entry::rawId))
            .toList();
        if (items.isEmpty()) {
            return "";
        }
        @SuppressWarnings("unchecked")
        MappedRegistryAccessor<Item> registry = (MappedRegistryAccessor<Item>) BuiltInRegistries.ITEM;
        List<?> initializers = ((DataComponentInitializersAccessor) BuiltInRegistries.DATA_COMPONENT_INITIALIZERS).spoutcraftredux$getInitializers();
        vanillaItemCount = BuiltInRegistries.ITEM.size();
        vanillaInitializerCount = initializers.size();
        registry.spoutcraftredux$setFrozen(false);
        registry.spoutcraftredux$setUnregisteredIntrusiveHolders(new IdentityHashMap<>());
        try {
            for (Content.Entry entry : items) {
                if (entry.rawId() != BuiltInRegistries.ITEM.size()) {
                    return "Item ids don't line up (" + entry.id() + " is " + entry.rawId() + " on the server but would be "
                        + BuiltInRegistries.ITEM.size() + " here). Another mod that adds items may be installed.";
                }
                Identifier id = Identifier.tryParse(entry.id());
                if (id == null || id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) || BuiltInRegistries.ITEM.containsKey(id)) {
                    return "Bad item id " + entry.id();
                }
                Item item = create(minecraft, id, entry);
                Registry.register(BuiltInRegistries.ITEM, id, item);
                ITEMS.add(item);
            }
        } catch (RuntimeException e) {
            LOGGER.error("Could not register the server's items", e);
            return "Could not register the server's items: " + e.getMessage();
        } finally {
            registry.spoutcraftredux$setUnregisteredIntrusiveHolders(null);
            registry.spoutcraftredux$setFrozen(true);
        }
        LOGGER.info("Registered {} items from the server", ITEMS.size());
        return "";
    }

    // Must build the same components as the server's SpoutCraftReduxItem
    private static Item create(Minecraft minecraft, Identifier id, Content.Entry entry) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        Item.Properties properties = new Item.Properties().setId(key)
            .stacksTo(entry.integer(ContentKeys.MAX_STACK_SIZE, 64))
            .rarity(Rarity.valueOf(entry.string(ContentKeys.RARITY, "common").toUpperCase(Locale.ROOT)));
        int maxDamage = entry.integer(ContentKeys.MAX_DAMAGE, 0);
        if (maxDamage > 0) {
            properties.durability(maxDamage);
        }
        if (entry.bool(ContentKeys.FIRE_RESISTANT, false)) {
            properties.fireResistant();
        }
        if (entry.properties().containsKey(ContentKeys.NUTRITION)) {
            FoodProperties.Builder food = new FoodProperties.Builder()
                .nutrition(entry.integer(ContentKeys.NUTRITION, 0))
                .saturationModifier(entry.number(ContentKeys.SATURATION, 0));
            properties.food((entry.bool(ContentKeys.CAN_ALWAYS_EAT, false) ? food.alwaysEdible() : food).build());
        }
        Item item = new Item(properties);
        Component name = NotificationHandler.parse(minecraft, entry.string(ContentKeys.NAME, "\"" + id.getPath() + "\""));
        Identifier model = Identifier.tryParse(entry.string(ContentKeys.MODEL, id.toString()));
        Identifier itemModel = model != null ? model : id;
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.add(key, (components, context, k) ->
            components.set(DataComponents.ITEM_NAME, name).set(DataComponents.ITEM_MODEL, itemModel));
        return item;
    }

    /** Removes every item registered for the session. */
    public static synchronized void clear() {
        if (vanillaItemCount < 0) {
            return;
        }
        @SuppressWarnings("unchecked")
        MappedRegistryAccessor<Item> registry = (MappedRegistryAccessor<Item>) BuiltInRegistries.ITEM;
        var byId = registry.spoutcraftredux$getById();
        while (byId.size() > vanillaItemCount) {
            Holder.Reference<Item> holder = byId.remove(byId.size() - 1);
            ResourceKey<Item> key = holder.key();
            registry.spoutcraftredux$getToId().removeInt(holder.value());
            registry.spoutcraftredux$getByLocation().remove(key.identifier());
            registry.spoutcraftredux$getByKey().remove(key);
            registry.spoutcraftredux$getByValue().remove(holder.value());
            registry.spoutcraftredux$getRegistrationInfos().remove(key);
        }
        List<?> initializers = ((DataComponentInitializersAccessor) BuiltInRegistries.DATA_COMPONENT_INITIALIZERS).spoutcraftredux$getInitializers();
        while (initializers.size() > vanillaInitializerCount) {
            initializers.removeLast();
        }
        if (!ITEMS.isEmpty()) {
            LOGGER.info("Removed {} items from the server", ITEMS.size());
        }
        ITEMS.clear();
        vanillaItemCount = -1;
        vanillaInitializerCount = -1;
    }
}
