package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.command.CommandOutput;
import emu.grasscutter.game.activity.leylinechallenge.LeyLineChallengeEnterFlow;
import emu.grasscutter.game.player.Player;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

@Command(
        label = "stygian",
        aliases = {"onslaught", "leylinechallenge"},
        permission = "player.stygian",
        permissionTargeted = "player.stygian.others")
public final class StygianCommand implements CommandHandler {

    @Override
    public CommandLine createCommandLine(Player sender, Player targetPlayer) {
        return new CommandLine(new Args(sender, targetPlayer));
    }

    @CommandLine.Command(name = "stygian")
    private final class Args implements Runnable {
        private final Player sender;
        private final Player targetPlayer;

        @Parameters(index = "0", arity = "0..1", defaultValue = "1", paramLabel = "[1-6]")
        private int difficulty;

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

            int diff = (difficulty < 1 || difficulty > 6) ? 1 : difficulty;
            LeyLineChallengeEnterFlow.enter(targetPlayer, diff, false);
            int dungeonId = LeyLineChallengeEnterFlow.DUNGEON_IDS[diff - 1];
            CommandOutput.sendMessage(sender, "Entering Stygian Onslaught tier " + diff + " (dungeon " + dungeonId + ").");
        }
    }
}
