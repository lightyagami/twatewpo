package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.ExplorationHelper;
import java.util.*;

/**
 * Sets exploration percentage for the world or specific nations, and awards all exploration rewards.
 */
@Command(
        label = "explore",
        aliases = {"setexplore", "exploremax", "explorereward"},
        usage = {
            "", // all nations 100%
            "all [percentage]", // all nations specified percentage (default 100)
            "<nation> [percentage]", // specific nation specified percentage (default 100)
            "list" // list all available nations
        },
        permission = "player.explore",
        permissionTargeted = "player.explore.others",
        targetRequirement = Command.TargetRequirement.PLAYER)
public final class ExploreCommand implements CommandHandler {

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (!args.isEmpty() && args.get(0).equalsIgnoreCase("list")) {
            CommandHandler.sendMessage(
                    sender,
                    "Available nations: all, " + String.join(", ", ExplorationHelper.getNationKeys()));
            return;
        }

        String target = "all";
        int percentage = 100;

        if (args.size() == 1) {
            String arg0 = args.get(0);
            try {
                percentage = Integer.parseInt(arg0);
            } catch (NumberFormatException e) {
                target = arg0.toLowerCase();
            }
        } else if (args.size() >= 2) {
            target = args.get(0).toLowerCase();
            try {
                percentage = Integer.parseInt(args.get(1));
            } catch (NumberFormatException e) {
                CommandHandler.sendMessage(sender, "Invalid percentage: " + args.get(1));
                return;
            }
        }

        if (percentage < 0) percentage = 0;
        if (percentage > 100) percentage = 100;

        boolean isAll = target.equalsIgnoreCase("all");
        List<Integer> affectedAreas = new ArrayList<>();

        if (isAll) {
            affectedAreas.addAll(ExplorationHelper.getAllAreaTotalExp().keySet());
        } else {
            var areas = ExplorationHelper.getNationAreas().get(target);
            if (areas == null) {
                CommandHandler.sendMessage(
                        sender,
                        "Unknown nation: " + target + ". Available: all, " + String.join(", ", ExplorationHelper.getNationKeys()));
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
            ExplorationHelper.grantExplorationRewards(sender, targetPlayer, target);
        }

        String displayName = isAll ? "All regions" : ExplorationHelper.getNationDisplayName(target);
        CommandHandler.sendMessage(
                sender,
                String.format(
                        "Set exploration of %s (%d areas) to %d%%.",
                        displayName, affectedAreas.size(), percentage));
    }
}
