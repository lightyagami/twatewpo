package emu.grasscutter.game.world;

import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.server.packet.send.PacketGetAreaExplorePointRsp;
import emu.grasscutter.server.packet.send.PacketSceneAreaUnlockNotify;
import java.util.*;

public final class ExplorationHelper {

    /** All 71 exploration areas and their maximum total points from ExploreAreaTotalExpExcelConfigData.json. */
    private static final Map<Integer, Integer> AREA_TOTAL_EXP = new LinkedHashMap<>();

    /** Predefined nation -> area IDs list from CityConfigData.json. */
    private static final Map<String, List<Integer>> NATION_AREAS = new LinkedHashMap<>();

    /** Element Sigil itemId per nation. */
    private static final Map<String, List<Integer>> NATION_SIGILS = new LinkedHashMap<>();

    /** Display name per nation key. */
    private static final Map<String, String> NATION_NAMES = new LinkedHashMap<>();

    static {
        // Area total exp map
        int[] areaIds = new int[] {
            1, 10, 1000, 11, 12, 13, 14, 17, 18, 19, 2, 20, 200, 21, 22, 23, 24, 25, 26, 27, 28, 29, 3, 30, 300, 31,
            32, 33, 34, 35, 36, 37, 38, 39, 4, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 5, 51, 52, 53, 55, 56, 57, 58,
            59, 6, 600, 7, 70, 71, 72, 73, 74, 75, 76, 77, 8, 800, 9, 900, 910, 920
        };
        int[] totalExps = new int[] {
            273, 470, 3304, 333, 247, 199, 258, 196, 308, 113, 146, 1625, 306, 2391, 1426, 3294, 1544, 248, 2179,
            3110, 1232, 332, 218, 2357, 463, 2381, 5341, 971, 2712, 4363, 1942, 3290, 2759, 2356, 211, 305, 269, 804,
            2990, 1675, 2096, 2820, 43, 1657, 2611, 450, 3283, 4210, 1083, 1379, 3905, 2835, 1274, 1383, 571, 3348,
            274, 2386, 4258, 2231, 2149, 2632, 1564, 446, 118, 185, 2392, 286, 4613, 1200, 513
        };
        for (int i = 0; i < areaIds.length; i++) {
            AREA_TOTAL_EXP.put(areaIds[i], totalExps[i]);
        }

        // Nations and their areas
        NATION_AREAS.put("mondstadt", List.of(1, 2, 3, 4, 10, 77));
        NATION_AREAS.put("liyue", List.of(5, 6, 7, 8, 9, 19, 300, 40, 41, 47));
        NATION_AREAS.put("inazuma", List.of(11, 12, 13, 14, 17, 18, 200));
        NATION_AREAS.put("sumeru", List.of(20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 32, 30, 31));
        NATION_AREAS.put("fontaine", List.of(33, 34, 35, 36, 37, 38, 39, 42, 600));
        NATION_AREAS.put("natlan", List.of(43, 44, 45, 46, 48, 49, 51, 52, 53, 800));
        NATION_AREAS.put("nodkrai", List.of(70, 71, 72, 73, 74, 75, 76, 900, 910, 920));
        NATION_AREAS.put("snezhnaya", List.of(55, 56, 57, 58, 59));
        NATION_AREAS.put("firmament", List.of(1000));

        // Display names
        NATION_NAMES.put("mondstadt", "Mondstadt");
        NATION_NAMES.put("liyue", "Liyue");
        NATION_NAMES.put("inazuma", "Inazuma");
        NATION_NAMES.put("sumeru", "Sumeru");
        NATION_NAMES.put("fontaine", "Fontaine");
        NATION_NAMES.put("natlan", "Natlan");
        NATION_NAMES.put("nodkrai", "Nod-Krai");
        NATION_NAMES.put("snezhnaya", "Snezhnaya");
        NATION_NAMES.put("firmament", "Ancient Firmament");

        // Sigil IDs (Item IDs: 301=Anemo, 302=Geo, 303=Electro, 304=Dendro, 305=Hydro, 306=Pyro, 307=Cryo)
        NATION_SIGILS.put("mondstadt", List.of(301));
        NATION_SIGILS.put("liyue", List.of(302));
        NATION_SIGILS.put("inazuma", List.of(303));
        NATION_SIGILS.put("sumeru", List.of(304));
        NATION_SIGILS.put("fontaine", List.of(305));
        NATION_SIGILS.put("natlan", List.of(306));
        NATION_SIGILS.put("nodkrai", List.of(306, 307));
        NATION_SIGILS.put("snezhnaya", List.of(307));
        NATION_SIGILS.put("firmament", List.of(307));
    }

    private ExplorationHelper() {}

    public static Map<Integer, Integer> getAllAreaTotalExp() {
        return Collections.unmodifiableMap(AREA_TOTAL_EXP);
    }

    public static Map<String, List<Integer>> getNationAreas() {
        return Collections.unmodifiableMap(NATION_AREAS);
    }

    public static Set<String> getNationKeys() {
        return NATION_AREAS.keySet();
    }

    public static int getTotalExpForArea(int areaId) {
        return AREA_TOTAL_EXP.getOrDefault(areaId, 0);
    }

    public static String getNationDisplayName(String key) {
        return NATION_NAMES.getOrDefault(key.toLowerCase(), key);
    }

    /**
     * Unlock scene areas across all relevant scenes for the player.
     */
    public static void unlockScenesForExploration(Player player) {
        int[] scenes = new int[] {3, 5, 6, 11, 101, 102, 103};
        for (int sceneId : scenes) {
            try {
                var unlocked = player.getUnlockedSceneAreas(sceneId);
                for (int area = 1; area <= 1000; area++) {
                    unlocked.add(area);
                }
                if (player.isOnline() && player.getSession() != null) {
                    player.sendPacket(new PacketSceneAreaUnlockNotify(sceneId, unlocked));
                }
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Set exploration percentage for a list of areas and send packets to the player.
     */
    public static void setExploration(Player player, Collection<Integer> areaIds, int percentage) {
        if (percentage < 0) percentage = 0;
        if (percentage > 100) percentage = 100;

        List<Integer> validAreaIds = new ArrayList<>();
        for (int areaId : areaIds) {
            int totalExp = getTotalExpForArea(areaId);
            if (totalExp <= 0) continue;
            int points = (int) Math.round((double) totalExp * percentage / 100.0);
            player.setExploreAreaPoint(areaId, points);
            validAreaIds.add(areaId);
        }

        player.save();

        if (!validAreaIds.isEmpty() && player.isOnline() && player.getSession() != null) {
            player.sendPacket(new PacketGetAreaExplorePointRsp(player, validAreaIds));
        }
    }

    /**
     * Award exploration rewards for a nation or all nations.
     * Rewards include Primogems, Adventure EXP, Sigils, and Mora.
     */
    public static void grantExplorationRewards(Player sender, Player targetPlayer, String nationKey) {
        boolean all = nationKey == null || nationKey.equalsIgnoreCase("all");

        List<String> targetNations = new ArrayList<>();
        if (all) {
            targetNations.addAll(NATION_AREAS.keySet());
        } else {
            String lower = nationKey.toLowerCase();
            if (NATION_AREAS.containsKey(lower)) {
                targetNations.add(lower);
            }
        }

        int totalPrimos = 0;
        int totalExp = 0;
        long totalMora = 0;
        Map<Integer, Integer> totalSigils = new HashMap<>();

        for (String n : targetNations) {
            // Per nation rewards:
            // ~5,000 Primogems, ~20,000 Adventure EXP, ~2,000,000 Mora, ~400 Sigils
            int primos = 5000;
            int exp = 20000;
            long mora = 2_000_000L;

            totalPrimos += primos;
            totalExp += exp;
            totalMora += mora;

            var sigils = NATION_SIGILS.get(n);
            if (sigils != null) {
                for (int sigilId : sigils) {
                    totalSigils.put(sigilId, totalSigils.getOrDefault(sigilId, 0) + 400);
                }
            }
        }

        // Grant Primogems
        targetPlayer.setPrimogems(targetPlayer.getPrimogems() + totalPrimos);

        // Grant Adventure EXP
        targetPlayer.addExpDirectly(totalExp);

        // Grant Mora (Item 202)
        targetPlayer.getInventory().addItem(202, (int) Math.min(totalMora, Integer.MAX_VALUE), ActionReason.SubfieldDrop);

        // Grant Sigils
        for (Map.Entry<Integer, Integer> entry : totalSigils.entrySet()) {
            targetPlayer.getInventory().addItem(entry.getKey(), entry.getValue(), ActionReason.SubfieldDrop);
        }

        targetPlayer.save();

        CommandHandler.sendMessage(
                sender,
                String.format(
                        "Granted exploration rewards: %d Primogems, %d Adventure EXP, %,d Mora, and elemental sigils.",
                        totalPrimos, totalExp, totalMora));
    }
}
