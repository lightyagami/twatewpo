package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.world.Position;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.server.game.GameSession;

@Opcodes(PacketOpcodes.TheaterLobbySceneJumpReq)
public class HandlerTheaterLobbySceneJumpReq extends PacketHandler {

    private static final int THEATER_DUNGEON_ID = 156;
    private static final int THEATER_SCENE_ID = 48000;
    private static final int THEATER_ROOM_SCENE_ID = 20261;
    private static final Position THEATER_BORN_POS = new Position(40.074f, 0.55f, 43.861f);
    private static final Position LIBRARY_EXIT_POS = new Position(2246.0f, 236.0f, -786.5f);

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        var player = session.getPlayer();
        if (player == null) return;

        int curSceneId = player.getSceneId();
        Grasscutter.getLogger().info("TheaterLobbySceneJumpReq received for player {} in scene {}", player.getUid(), curSceneId);

        if (curSceneId == THEATER_SCENE_ID || curSceneId == THEATER_ROOM_SCENE_ID) {
            // Player is inside the Theater Lobby and jumping out back to the library/open world
            int prevScene = player.getScene().getPrevScene();
            if (prevScene <= 0) prevScene = 3;
            player.getWorld().transferPlayerToScene(player, prevScene, LIBRARY_EXIT_POS);
        } else {
            // Player is outside and jumping into the Theater Lobby
            boolean entered = player.getServer().getDungeonSystem().enterDungeon(player, 0, THEATER_DUNGEON_ID, true);
            if (!entered) {
                player.getWorld().transferPlayerToScene(player, THEATER_SCENE_ID, THEATER_BORN_POS);
            }
        }
    }
}
