package emu.grasscutter.game.quest;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.packet.send.PacketFinishedParentQuestUpdateNotify;
import emu.grasscutter.utils.FileUtils;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Marks main quests finished for the CLIENT, with no quest data behind them.
 *
 * <p>The quest system can only finish a quest it has data for, and this server's resource set is
 * older than the client - the newest few hundred main quests, Snezhnaya's region gate among them,
 * are simply absent. `ParentQuest` needs nothing but an id and the finished flag for the client to
 * believe it, so the notify is forged directly.
 *
 * <p>The id list ships inside the jar for the same reason: depending on the resource set would
 * reintroduce exactly the gap this works around.
 */
public final class ForcedQuests {
    private ForcedQuests() {}

    /** Sent in batches - one notify carrying four thousand quests is a needlessly large packet. */
    private static final int BATCH = 512;

    private static List<Integer> allMainQuests;

    private static List<Integer> nonArchonMainQuests;
    private static Set<Integer> archonMainQuestSet;

    /** Main quest IDs that are Story, World, Event, etc. (everything except Archon Quests). */
    public static synchronized List<Integer> nonArchonMainQuests() {
        if (nonArchonMainQuests != null) return nonArchonMainQuests;

        var ids = new ArrayList<Integer>();
        try {
            var raw = FileUtils.readResource("/quests/non_aq_quest_ids.txt");
            for (var line : new String(raw, StandardCharsets.UTF_8).split("\\R")) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    ids.add(Integer.parseInt(line));
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (Exception e) {
            Grasscutter.getLogger().error("Could not read bundled non_aq_quest_ids.txt", e);
        }
        nonArchonMainQuests = ids;
        return ids;
    }

    private static List<Integer> pre7ArchonMainQuests;

    /** Archon Main Quest IDs prior to 7.0/7.1 (235 quests). */
    public static synchronized List<Integer> pre7ArchonMainQuests() {
        if (pre7ArchonMainQuests != null) return pre7ArchonMainQuests;

        var ids = new ArrayList<Integer>();
        try {
            var raw = FileUtils.readResource("/quests/pre_7_aq_quest_ids.txt");
            for (var line : new String(raw, StandardCharsets.UTF_8).split("\\R")) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    ids.add(Integer.parseInt(line));
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (Exception e) {
            Grasscutter.getLogger().error("Could not read bundled pre_7_aq_quest_ids.txt", e);
        }
        pre7ArchonMainQuests = ids;
        return ids;
    }

    /**
     * Skips all quests up to 7.1: marks all non-Archon quests and all pre-7 Archon quests as finished,
     * clears old active quests, and starts Chapter 1700 (Quest 7011).
     */
    public static int skipTo7(Player player) {
        var nonAq = nonArchonMainQuests();
        var pre7 = pre7ArchonMainQuests();

        var toFinish = new java.util.HashSet<Integer>(nonAq);
        toFinish.addAll(pre7);

        int newlyApplied = apply(player, toFinish);

        // Clean up any old active quests below 7000 from QuestManager
        try {
            for (var mq : player.getQuestManager().getActiveMainQuests()) {
                if (mq.getParentQuestId() < 7000) {
                    mq.finish();
                }
            }
        } catch (Exception e) {
            Grasscutter.getLogger().warn("Error cleaning up active pre-7 quests", e);
        }

        // Start Chapter 1700 (Quest 7011: Into the Wind, Into the Frigid North)
        try {
            player.getQuestManager().startMainQuest(7011);
        } catch (Exception e) {
            Grasscutter.getLogger().warn("Error starting 7.1 Archon quest 7011", e);
        }

        player.save();
        return newlyApplied;
    }

    /** Set of Archon Main Quest IDs. */
    public static synchronized Set<Integer> archonMainQuestSet() {
        if (archonMainQuestSet != null) return archonMainQuestSet;

        var ids = new java.util.HashSet<Integer>();
        try {
            var raw = FileUtils.readResource("/quests/aq_quest_ids.txt");
            for (var line : new String(raw, StandardCharsets.UTF_8).split("\\R")) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    ids.add(Integer.parseInt(line));
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (Exception e) {
            Grasscutter.getLogger().error("Could not read bundled aq_quest_ids.txt", e);
        }
        archonMainQuestSet = ids;
        return ids;
    }

    public static boolean isArchonQuest(int mainQuestId) {
        return archonMainQuestSet().contains(mainQuestId);
    }

    /** Every main quest id the client knows about, across both the old and new data sets. */
    public static synchronized List<Integer> allMainQuests() {
        if (allMainQuests != null) return allMainQuests;

        var ids = new ArrayList<Integer>();
        try {
            var raw = FileUtils.readResource("/quests/main_quest_ids.txt");
            for (var line : new String(raw, StandardCharsets.UTF_8).split("\\R")) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    ids.add(Integer.parseInt(line));
                } catch (NumberFormatException ignored) {
                    // A malformed line costs one quest, not the whole list.
                }
            }
        } catch (Exception e) {
            Grasscutter.getLogger().error("Could not read the bundled main quest id list.", e);
        }
        allMainQuests = ids;
        return ids;
    }

    /** Tells the client these are finished, without touching the player's saved state. */
    public static void notify(Player player, Collection<Integer> questIds) {
        var batch = new ArrayList<Integer>(BATCH);
        for (var id : questIds) {
            batch.add(id);
            if (batch.size() == BATCH) {
                send(player, batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) send(player, batch);
    }

    /** Marks them finished, remembers it, and tells the client. Survives a relog. */
    public static int apply(Player player, Collection<Integer> questIds) {
        var forced = player.getForcedFinishedQuests();
        int before = forced.size();
        forced.addAll(questIds);
        player.save();
        notify(player, questIds);
        return forced.size() - before;
    }

    private static void send(Player player, List<Integer> ids) {
        var arr = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) arr[i] = ids.get(i);
        player.sendPacket(new PacketFinishedParentQuestUpdateNotify(arr));
    }
}
