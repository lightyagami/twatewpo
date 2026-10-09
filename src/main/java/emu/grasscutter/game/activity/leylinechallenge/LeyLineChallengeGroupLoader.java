package emu.grasscutter.game.activity.leylinechallenge;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.Position;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.server.packet.send.PacketScenePointUnlockNotify;

/**
 * LeyLineChallenge entrance loader for Stygian Onslaught in Sal Terrae (Scene 3).
 */
public final class LeyLineChallengeGroupLoader {
    public static final int SCENE_ID = 3;
    public static final int GROUP_ID = 133105039;
    public static final int GADGET_CONFIG_ID = 39001;
    public static final int GADGET_ID = 73051004;
    public static final int ENTER_OPTION_ID = 69;
    public static final int SCENE_POINT_ID = 1628;

    private static final Position ENTRANCE_POS = new Position(980.196f, 262.167f, -194.816f);
    private static final float SPAWN_RANGE = 250f;

    private LeyLineChallengeGroupLoader() {}

    /** Spawn the entrance group when the player walks into range, and ensure entrance waypoint is unlocked. */
    public static void ensureNearby(Scene scene) {
        if (scene == null || scene.getId() != SCENE_ID) {
            return;
        }

        boolean anyNear = false;
        for (Player player : scene.getPlayers()) {
            if (player == null) continue;
            // Ensure map waypoint / domain entrance icon is unlocked for the player
            if (player.getUnlockedScenePoints(SCENE_ID).add(SCENE_POINT_ID)) {
                player.sendPacket(new PacketScenePointUnlockNotify(SCENE_ID, SCENE_POINT_ID));
            }
            if (player.getPosition() != null && player.getPosition().computeDistance(ENTRANCE_POS) <= SPAWN_RANGE) {
                anyNear = true;
            }
        }

        if (anyNear) {
            if (scene.getScriptManager().getGroupInstanceById(GROUP_ID) == null) {
                scene.loadDynamicGroup(GROUP_ID);
                Grasscutter.getLogger().debug("Loaded LeyLineChallenge entrance group {} in scene {}", GROUP_ID, SCENE_ID);
            }
        }
    }

    /**
     * True for the entrance gadget the worktop option belongs to.
     */
    public static boolean isEntranceGadget(EntityGadget gadget) {
        if (gadget == null) return false;
        return gadget.getGadgetId() == GADGET_ID
                || (gadget.getGroupId() == GROUP_ID && gadget.getConfigId() == GADGET_CONFIG_ID);
    }
}
