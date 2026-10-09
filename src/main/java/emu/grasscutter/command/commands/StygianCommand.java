package emu.grasscutter.command.commands;

import emu.grasscutter.command.*;
import emu.grasscutter.game.activity.leylinechallenge.LeyLineChallengeEnterFlow;
import emu.grasscutter.game.player.Player;
import java.util.List;

@Command(
        label = "stygian",
        aliases = {"onslaught", "leylinechallenge"},
        usage = {"[1-6]"},
        permission = "player.stygian",
        permissionTargeted = "player.stygian.others")
public final class StygianCommand implements CommandHandler {

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (targetPlayer == null) {
            CommandHandler.sendMessage(sender, "Target player not found.");
            return;
        }

        int difficulty = 1;
        if (!args.isEmpty()) {
            try {
                difficulty = Integer.parseInt(args.get(0));
            } catch (NumberFormatException ignored) {}
        }
        if (difficulty < 1 || difficulty > 6) difficulty = 1;

        LeyLineChallengeEnterFlow.enter(targetPlayer, difficulty, false);
        int dungeonId = LeyLineChallengeEnterFlow.DUNGEON_IDS[difficulty - 1];
        CommandHandler.sendMessage(sender, "Entering Stygian Onslaught tier " + difficulty + " (dungeon " + dungeonId + ").");
    }
}
