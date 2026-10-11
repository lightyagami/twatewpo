package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.command.CommandOutput;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.ExplorationHelper;
import java.util.*;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

/**
 * Sets exploration percentage for the world or specific nations, and awards all exploration rewards.
 */
@Command(
        label = "explore",
        aliases = {"setexplore", "exploremax", "explorereward"},
        permission = "player.explore",
        permissionTargeted = "player.explore.others",
        targetRequirement = Command.TargetRequirement.PLAYER)
public final class ExploreCommand implements CommandHandler {

    @Override
    public CommandLine createCommandLine(Player sender, Player targetPlayer) {
        return new CommandLine(new Args(sender, targetPlayer));
    }

    @CommandLine.Command(name = "explore")
    private final class Args implements Runnable {
        private final Player sender;
        private final Player targetPlayer;

        @Parameters(index = "0", arity = "0..1", paramLabel = "[nation|all|list|<percentage>]")
        private String target;

        @Parameters(index = "1", arity = "0..1", paramLabel = "[percentage]")
        private Integer percentArg;

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

            if (target != null && target.equalsIgnoreCase("list")) {
                CommandOutput.sendMessage(
                        sender,
                        "Available nations: all, " + String.join(", ", ExplorationHelper.getNationKeys()));
                return;
            }

            String nation = "all";
            int percentage = 100;

            if (target != null) {
                try {
                    percentage = Integer.parseInt(target);
                } catch (NumberFormatException e) {
                    nation = target.toLowerCase();
                    if (percentArg != null) {
                        percentage = percentArg;
                    }
                }
            }

            if (percentage < 0) percentage = 0;
            if (percentage > 100) percentage = 100;

            boolean isAll = nation.equalsIgnoreCase("all");
            List<Integer> affectedAreas = new ArrayList<>();

            if (isAll) {
                affectedAreas.addAll(ExplorationHelper.getAllAreaTotalExp().keySet());
            } else {
                var areas = ExplorationHelper.getNationAreas().get(nation);
                if (areas == null) {
                    CommandOutput.sendMessage(
                            sender,
                            "Unknown nation: " + nation + ". Available: all, " + String.join(", ", ExplorationHelper.getNationKeys()));
                    return;
                }
                affectedAreas.addAll(areas);
            }

            // Unlock scenes so map regions are unclouded and accessible
            ExplorationHelper.unlockScenesForExploration(targetPlayer);

            // Apply exploration points and notify client
            ExplorationHelper.setExploration(targetPlayer, affectedAreas, percentage);

            // Award rewards (Primogems, Adventure EXP, Mora, Sigils) if setting to 100%
            if (percentage == 100) {
                ExplorationHelper.grantExplorationRewards(sender, targetPlayer, nation);
            }

            String displayName = isAll ? "All regions" : ExplorationHelper.getNationDisplayName(nation);
            CommandOutput.sendMessage(
                    sender,
                    String.format(
                            "Set exploration of %s (%d areas) to %d%%.",
                            displayName, affectedAreas.size(), percentage));
        }
    }
}
