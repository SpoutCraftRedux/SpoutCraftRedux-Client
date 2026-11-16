package org.spoutcraftredux.client.gui.server;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.spoutcraftredux.client.network.RawPayload;
import org.spoutcraftredux.client.network.SpoutCraftReduxChannels;
import org.spoutcraftredux.protocol.message.AssetData;
import org.spoutcraftredux.protocol.message.AssetRequest;

/**
 * Images from servers, cached on disk by their SHA-256 in {@code spoutcraftredux/cache}. Every image is checked
 * against its hash before use, so a cache file or a download can't be swapped for something else.
 */
public final class AssetCache {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_SIZE = 2048;

    /** A loaded image and its size in pixels. */
    public record Loaded(Identifier texture, int width, int height) {
    }

    private static final Map<String, Loaded> LOADED = new HashMap<>();
    private static final Set<String> REQUESTED = new HashSet<>();
    private static final Map<String, byte[]> PARTIAL = new HashMap<>();

    private AssetCache() {
    }

    /** Gets an image, asking the server for it the first time. Returns null until it has arrived. */
    public static @Nullable Loaded get(String sha256) {
        Loaded loaded = LOADED.get(sha256);
        if (loaded != null || !REQUESTED.add(sha256)) {
            return loaded;
        }
        Path file = file(sha256);
        if (Files.exists(file)) {
            try {
                byte[] bytes = Files.readAllBytes(file);
                if (sha256(bytes).equals(sha256)) {
                    return load(sha256, bytes);
                }
            } catch (IOException e) {
                LOGGER.warn("Could not read cached image {}", file, e);
            }
        }
        ClientPlayNetworking.send(new RawPayload(SpoutCraftReduxChannels.ASSET_REQUEST, new AssetRequest(sha256).encode()));
        return null;
    }

    /** Collects a piece of an image from the server and loads the image once it is complete. */
    public static void receive(AssetData data) {
        if (!REQUESTED.contains(data.sha256()) || LOADED.containsKey(data.sha256())) {
            return;
        }
        byte[] buffer = PARTIAL.computeIfAbsent(data.sha256(), sha -> new byte[data.totalSize()]);
        if (buffer.length != data.totalSize()) {
            PARTIAL.remove(data.sha256());
            return;
        }
        System.arraycopy(data.bytes(), 0, buffer, data.offset(), data.bytes().length);
        if (data.offset() + data.bytes().length < data.totalSize()) {
            return;
        }
        PARTIAL.remove(data.sha256());
        if (!sha256(buffer).equals(data.sha256())) {
            LOGGER.warn("Dropping image {}: its contents don't match its hash", data.sha256());
            return;
        }
        try {
            Path file = file(data.sha256());
            Files.createDirectories(file.getParent());
            Files.write(file, buffer);
        } catch (IOException e) {
            LOGGER.warn("Could not cache image {}", data.sha256(), e);
        }
        load(data.sha256(), buffer);
    }

    private static @Nullable Loaded load(String sha256, byte[] bytes) {
        try {
            NativeImage image = NativeImage.read(bytes);
            if (image.getWidth() > MAX_SIZE || image.getHeight() > MAX_SIZE) {
                LOGGER.warn("Dropping image {}: {}x{} is larger than {}x{}", sha256, image.getWidth(), image.getHeight(), MAX_SIZE, MAX_SIZE);
                image.close();
                return null;
            }
            Identifier id = Identifier.fromNamespaceAndPath("spoutcraftredux", "server_asset/" + sha256);
            Loaded loaded = new Loaded(id, image.getWidth(), image.getHeight());
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> "SpoutCraft Redux " + sha256, image));
            LOADED.put(sha256, loaded);
            return loaded;
        } catch (IOException e) {
            LOGGER.warn("Could not load image {}", sha256, e);
            return null;
        }
    }

    /** Forgets requests on disconnect so the next server can be asked again. Loaded images stay. */
    public static void clearRequests() {
        REQUESTED.retainAll(LOADED.keySet());
        PARTIAL.clear();
    }

    private static Path file(String sha256) {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("spoutcraftredux").resolve("cache").resolve(sha256 + ".png");
    }

    private static String sha256(byte[] bytes) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
