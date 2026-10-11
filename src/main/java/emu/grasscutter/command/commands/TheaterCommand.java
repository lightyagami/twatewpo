package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.command.CommandOutput;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.Position;
import picocli.CommandLine;

@Command(
        label = "theater",
        aliases = {"theatre"},
        permission = "player.theater",
        permissionTargeted = "player.theater.others")
public final class TheaterCommand implements CommandHandler {

    private static final int THEATER_DUNGEON_ID = 156;
    private static final int THEATER_SCENE_ID = 48000;
    private static final int THEATER_ROOM_SCENE_ID = 20261;
    private static final Position THEATER_BORN_POS = new Position(40.074f, 0.55f, 43.861f);
    private static final Position LIBRARY_EXIT_POS = new Position(2246.0f, 236.0f, -786.5f);

    @Override
    public CommandLine createCommandLine(Player sender, Player targetPlayer) {
        return new CommandLine(new Args(sender, targetPlayer));
    }

    @CommandLine.Command(name = "theater")
    private final class Args implements Runnable {
        private final Player sender;
        private final Player targetPlayer;

        private Args(Player sender, Player targetPlayer) {
            this.sender = sender;
            this.targetPlayer = targetPlayer;
        }

        @Override
        public void run() {
            if (targetPlayer == null) {
                CommandOutput.sendMessage(sender, "Target player not found.");
                return;
            }

            int curSceneId = targetPlayer.getSceneId();
            if (curSceneId == THEATER_SCENE_ID || curSceneId == THEATER_ROOM_SCENE_ID) {
                int prevScene = targetPlayer.getScene().getPrevScene();
                if (prevScene <= 0) prevScene = 3;
                targetPlayer.getWorld().transferPlayerToScene(targetPlayer, prevScene, LIBRARY_EXIT_POS);
                CommandOutput.sendMessage(sender, "Teleported out of Imaginarium Theater to Mondstadt.");
            } else {
                boolean entered = targetPlayer.getServer().getDungeonSystem().enterDungeon(targetPlayer, 0, THEATER_DUNGEON_ID, true);
                if (!entered) {
                    targetPlayer.getWorld().transferPlayerToScene(targetPlayer, THEATER_SCENE_ID, THEATER_BORN_POS);
                }
                CommandOutput.sendMessage(sender, "Teleported into Imaginarium Theater lobby.");
            }
        }
    }
}
