package emu.grasscutter.command.commands;

import emu.grasscutter.command.*;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.Position;
import java.util.List;

@Command(
        label = "theater",
        aliases = {"theatre"},
        usage = {""},
        permission = "player.theater",
        permissionTargeted = "player.theater.others")
public final class TheaterCommand implements CommandHandler {

    private static final int THEATER_DUNGEON_ID = 156;
    private static final int THEATER_SCENE_ID = 48000;
    private static final int THEATER_ROOM_SCENE_ID = 20261;
    private static final Position THEATER_BORN_POS = new Position(40.074f, 0.55f, 43.861f);
    private static final Position LIBRARY_EXIT_POS = new Position(2246.0f, 236.0f, -786.5f);

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (targetPlayer == null) {
            CommandHandler.sendMessage(sender, "Target player not found.");
            return;
        }

        int curSceneId = targetPlayer.getSceneId();
        if (curSceneId == THEATER_SCENE_ID || curSceneId == THEATER_ROOM_SCENE_ID) {
            int prevScene = targetPlayer.getScene().getPrevScene();
            if (prevScene <= 0) prevScene = 3;
            targetPlayer.getWorld().transferPlayerToScene(targetPlayer, prevScene, LIBRARY_EXIT_POS);
            CommandHandler.sendMessage(sender, "Teleported out of Imaginarium Theater to Mondstadt.");
        } else {
            boolean entered = targetPlayer.getServer().getDungeonSystem().enterDungeon(targetPlayer, 0, THEATER_DUNGEON_ID, true);
            if (!entered) {
                targetPlayer.getWorld().transferPlayerToScene(targetPlayer, THEATER_SCENE_ID, THEATER_BORN_POS);
            }
            CommandHandler.sendMessage(sender, "Teleported into Imaginarium Theater lobby.");
        }
    }
}
