package org.spoutcraftredux.client;

import java.util.Set;
import org.jspecify.annotations.Nullable;

/** Everything a SpoutCraft Redux server has set up on this client. Cleared on every disconnect. */
public final class SpoutCraftReduxSession {
    private static @Nullable SpoutCraftReduxSession current;

    private final int protocol;
    private final Set<String> serverCapabilities;

    private SpoutCraftReduxSession(int protocol, Set<String> serverCapabilities) {
        this.protocol = protocol;
        this.serverCapabilities = Set.copyOf(serverCapabilities);
    }

    public static @Nullable SpoutCraftReduxSession current() {
        return current;
    }

    static SpoutCraftReduxSession start(int protocol, Set<String> serverCapabilities) {
        end();
        current = new SpoutCraftReduxSession(protocol, serverCapabilities);
        return current;
    }

    static void end() {
        current = null;
    }

    public int protocol() {
        return protocol;
    }

    public boolean serverSupports(String capability) {
        return serverCapabilities.contains(capability);
    }
}
