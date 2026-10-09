package emu.grasscutter.command.commands;

import static emu.grasscutter.utils.lang.Language.translate;

import emu.grasscutter.command.*;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestManager;
import emu.grasscutter.game.quest.enums.*;
import java.util.*;
import java.util.stream.Collectors;

@Command(
        label = "quest",
        aliases = {"q"},
        usage = {"(add|finish|running|talking|debug|triggers|grouptriggers) [<questId>]", "forcefinish (<questId>|all)", "dungeons"},
        permission = "player.quest",
        permissionTargeted = "player.quest.others")
public final class QuestCommand implements CommandHandler {
    private static final List<String> SINGLE_ARG = List.of("dungeons", "list", "skip_to_7", "skipto7", "skip7");
    private static final Set<String> WORD_ARGS = Set.of("all", "non_aq", "story", "world", "side", "pre_7", "pre7", "skip_to_7");
    /** `forcefinish all` or `forcefinish non_aq` has a word where the id goes, so the id parse must be skipped. */
    private static boolean isWordArg(List<String> args) {
        return args.size() > 1 && WORD_ARGS.contains(args.get(1).toLowerCase());
    }

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (args.isEmpty()) {
            sendUsageMessage(sender);
            return;
        }

        var cmd = args.get(0).toLowerCase();
        int questId = -1;

        if (!SINGLE_ARG.contains(cmd) && !isWordArg(args)) {
            try {
                questId = Integer.parseInt(args.get(1));
            } catch (Exception e) {
                CommandHandler.sendMessage(sender, translate(sender, "commands.quest.invalid_id"));
                return;
            }
        }

        switch (cmd) {
            // Marks a main quest finished on the CLIENT only, for quests this server has no data
            // for. Region barriers are quest-gated, and a region released after the resource set
            // was cut cannot be reached with `finish`. Does not persist across a relog.
            case "skip_to_7", "skipto7", "skip7" -> {
                int added = emu.grasscutter.game.quest.ForcedQuests.skipTo7(targetPlayer);
                CommandHandler.sendMessage(
                        sender,
                        "Skipped to 7.1! Marked all side quests and pre-7 Archon quests as finished ("
                                + added + " newly finished). Started 7.1 Archon Quest 7011 ('Into the Wind, Into the Frigid North').");
            }
            case "forcefinish" -> {
                // Marks main quests finished for the CLIENT. The quest system cannot finish what it
                // has no data for, and this resource set is older than the client, so region gates
                // like Snezhnaya's are unreachable any other way. Persisted and replayed on login.
                List<Integer> ids;
                String arg1 = args.size() > 1 ? args.get(1).toLowerCase() : "";
                if ("all".equals(arg1)) {
                    ids = emu.grasscutter.game.quest.ForcedQuests.allMainQuests();
                } else if ("non_aq".equals(arg1) || "story".equals(arg1) || "world".equals(arg1) || "side".equals(arg1)) {
                    ids = emu.grasscutter.game.quest.ForcedQuests.nonArchonMainQuests();
                } else if ("pre_7".equals(arg1) || "pre7".equals(arg1) || "skip_to_7".equals(arg1)) {
                    ids = emu.grasscutter.game.quest.ForcedQuests.pre7ArchonMainQuests();
                } else {
                    ids = List.of(questId);
                }
                int added = emu.grasscutter.game.quest.ForcedQuests.apply(targetPlayer, ids);
                CommandHandler.sendMessage(
                        sender,
                        "Force-finished " + ids.size() + " main quest(s), " + added + " newly."
                                + " This is saved and re-sent on every login.");
            }
            case "unforcefinish", "unforce", "unfinish_forced" -> {
                int removed = emu.grasscutter.game.quest.ForcedQuests.unforce(targetPlayer, List.of(questId));
                CommandHandler.sendMessage(
                        sender,
                        "Removed main quest " + questId + " from forced-finished quests ("
                                + removed + " removed).");
            }
            case "add", "start" -> {
                // With questing off the quest is created but its update reaches the client empty,
                // so the command reported a success the player never saw.
                if (!QuestManager.isQuestingActive()) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.quest.questing_off"));
                    return;
                }
                var quest = targetPlayer.getQuestManager().addQuest(questId);

                if (quest != null) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.quest.added", questId));
                    return;
                }

                if (emu.grasscutter.data.GameData.getMainQuestDataMap().containsKey(questId)) {
                    targetPlayer.getQuestManager().startMainQuest(questId);
                    CommandHandler.sendMessage(sender, "Started main quest " + questId + ".");
                    return;
                }

                CommandHandler.sendMessage(sender, translate(sender, "commands.quest.not_found"));
            }
            case "finish" -> {
                var quest = targetPlayer.getQuestManager().getQuestById(questId);

                if (quest == null) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.quest.not_found"));
                    return;
                }

                quest.finish();

                CommandHandler.sendMessage(sender, translate(sender, "commands.quest.finished", questId));
            }
            case "running" -> {
                var quest = targetPlayer.getQuestManager().getQuestById(questId);
                if (quest == null) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.quest.not_found"));
                    return;
                }

                CommandHandler.sendMessage(
                        sender,
                        translate(
                                sender,
                                "commands.quest.running",
                                questId,
                                translate(
                                        sender,
                                        switch (quest.state) {
                                            case QUEST_STATE_NONE, NONE -> "commands.quest.state.none";
                                            case QUEST_STATE_UNSTARTED, UNSTARTED -> "commands.quest.state.unstarted";
                                            case QUEST_STATE_UNFINISHED, UNFINISHED -> "commands.quest.state.unfinished";
                                            case QUEST_STATE_FINISHED, FINISHED -> "commands.quest.state.finished";
                                            case QUEST_STATE_FAILED, FAILED -> "commands.quest.state.failed";
                                        }),
                                quest.getState().getValue()));
            }
            case "talking" -> {
                var mainQuest = targetPlayer.getQuestManager().getMainQuestByTalkId(questId);
                if (mainQuest == null) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.quest.not_found"));
                    return;
                }

                var talk = mainQuest.getTalks().get(questId);
                CommandHandler.sendMessage(
                        sender,
                        translate(
                                sender,
                                "commands.quest.talking",
                                questId,
                                talk == null
                                        ? translate(sender, "commands.quest.state.not_exists")
                                        : translate(sender, "commands.quest.state.exists"),
                                mainQuest.getParentQuestId(),
                                mainQuest.getState().getValue()));
            }
            case "dungeons" -> {
                var dungeons = targetPlayer.getPlayerProgress().getCompletedDungeons();
                CommandHandler.sendMessage(
                        sender,
                        "Dungeons completed: "
                                + String.join(", ", dungeons.intStream().mapToObj(String::valueOf).toList()));
            }
            case "debug" -> {
                var loggedQuests = targetPlayer.getQuestManager().getLoggedQuests();
                var shouldAdd = !loggedQuests.contains(questId);

                if (shouldAdd) loggedQuests.add(questId);
                else loggedQuests.remove(questId);

                CommandHandler.sendMessage(
                        sender,
                        "Quest %s will %s."
                                .formatted(questId, shouldAdd ? "now be logged" : "no longer be logged"));
            }
            case "triggers" -> {
                var quest = targetPlayer.getQuestManager().getQuestById(questId);
                if (quest == null) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.quest.not_found"));
                    return;
                }

                CommandHandler.sendMessage(
                        sender,
                        "Triggers registered for %s: %s."
                                .formatted(questId, String.join(", ", quest.getTriggers().keySet())));
            }
            case "grouptriggers" -> {
                var scene = targetPlayer.getScene();
                var scriptManager = scene.getScriptManager();

                var group = scriptManager.getGroupById(questId);
                if (group == null) {
                    CommandHandler.sendMessage(sender, "The group does not exist.");
                    return;
                }

                CommandHandler.sendMessage(
                        sender,
                        group.triggers.entrySet().stream()
                                .map(entry -> "%s: %s".formatted(entry.getKey(), entry.getValue()))
                                .collect(Collectors.joining(", ")));
            }
            case "list" -> {
                var questManager = targetPlayer.getQuestManager();
                var mainQuests = questManager.getActiveMainQuests();
                var logged = questManager.getLoggedQuests();
                List<String> allQuestIds =
                        mainQuests.stream()
                                .filter(quest -> logged.isEmpty() || logged.contains(quest.getParentQuestId()))
                                .filter(quest -> quest.getState() != ParentQuestState.PARENT_QUEST_STATE_FINISHED)
                                .map(quest -> quest.getChildQuests().values())
                                .flatMap(Collection::stream)
                                .filter(quest -> quest.getState() == QuestState.QUEST_STATE_UNFINISHED)
                                .map(q -> q.getMainQuest().getParentQuestId() + ":" + q.getSubQuestId())
                                .toList();

                CommandHandler.sendMessage(
                        sender,
                        "Quests: "
                                + (allQuestIds.isEmpty() ? "(no active quests)" : String.join(", ", allQuestIds)));
            }
            default -> this.sendUsageMessage(sender);
        }
    }
}
