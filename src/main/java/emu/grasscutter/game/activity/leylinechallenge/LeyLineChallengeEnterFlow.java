package emu.grasscutter.game.activity.leylinechallenge;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.player.Player;

public final class LeyLineChallengeEnterFlow {

    public static final int[] DUNGEON_IDS = {20000, 20001, 20002, 20003, 20004, 20005};

    private LeyLineChallengeEnterFlow() {}

    /**
     * Enter the challenge.
     *
     * @param player the entering player
     * @param difficulty 1-based difficulty tier chosen on the activity page (1=Normal, 2=Hard, 3=Master)
     * @param fromWorktop {@code true} when this is the private-server worktop fallback rather than
     *     the official activity-page {@code EnterReq}
     */
    public static void enter(Player player, int difficulty, boolean fromWorktop) {
        if (player == null) return;

        int index = Math.max(1, Math.min(difficulty, DUNGEON_IDS.length)) - 1;
        int dungeonId = DUNGEON_IDS[index];

        Grasscutter.getLogger().info("Entering Stygian Onslaught (LeyLineChallenge) dungeon {} (difficulty {}) for player {}",
                dungeonId, difficulty, player.getUid());

        player.getServer().getDungeonSystem().enterDungeon(player, 0, dungeonId, true);
    }
}
