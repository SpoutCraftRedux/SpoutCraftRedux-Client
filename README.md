# SpoutCraft Redux Client

The Fabric mod for SpoutCraft Redux. It talks to the [SpoutCraft Redux server](https://github.com/SpoutCraftRedux/SpoutCraftRedux-Server) so the server can add GUIs, HUD widgets, sky settings, keybinds and more to your game. On any other server it does nothing.

Right now it completes the handshake: it answers the server's hello, agrees on a protocol version and keeps the session until you disconnect. The features build on that in later phases.

Targets Minecraft 26.3, Fabric Loader 0.20.1 and Fabric API 0.166.2+26.3. Requires Java 25.

## Getting the code

The protocol lives in its own repository and is a submodule here, so clone with:

```
git clone --recurse-submodules https://github.com/SpoutCraftRedux/SpoutCraftRedux-Client.git
```

If you already cloned without it, run `git submodule update --init`.

## Building and running

```
./gradlew build          # jar in build/libs/
./gradlew runClient      # dev client
./gradlew runJoinLocal   # dev client that joins localhost:25565
```

`runJoinLocal` pairs with the server's `runDevServer`. The dev client uses an offline account, so set `online-mode=false` and `white-list=false` on that server.

## CI and downloads

`.github/workflows/build.yml` builds every push and pull request. A push to `main` also uploads the mod jar to https://api.spoutcraftredux.org as build `<run number>` of project `spoutcraftredux-fabric`, version `26.3`. It then shows up on https://spoutcraftredux.org/downloads. The mod isn't published to Maven.

The workflow uses these repository secrets:

| Secret | Used for |
|---|---|
| `DOWNLOADS_API_TOKEN` | Upload token for api.spoutcraftredux.org |
| `DOWNLOADS_API_TOKEN_STAGING` | Upload token for staging-api.spoutcraftredux.org (not used by CI yet) |

## License

LGPL-3.0. See [LICENSE](LICENSE) and [COPYING](COPYING).

The checkbox, radio button and arrow textures in `src/main/resources/assets/spoutcraftredux/textures/gui/widget/` come from the original [Spoutcraft](https://github.com/spoutcraft/Spoutcraft) client, also LGPL-3.0. Every other texture the mod draws with comes from the player's own Minecraft install; none are shipped.
