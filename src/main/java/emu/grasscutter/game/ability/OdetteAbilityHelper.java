package emu.grasscutter.game.ability;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.entity.EntityClientGadget;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.player.Player;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class OdetteAbilityHelper {
    public static final int ODETTE_AVATAR_ID = 10000150;

    public static final int ODETTE_PARTNER_GADGET_ID = 41150001; // Avatar_Odette_Partner
    public static final int ODETTE_PARTNER_MOVE_ASIST_GADGET_ID = 41150002; // Avatar_Odette_Partner_MoveAsist
    public static final int ODETTE_BULLET_FOLLOW_AVATAR = 41150003; // Avatar_Odette_Bullet_FollowAvatar
    public static final int ODETTE_DUO_DANCE_COLLIDER = 41150004; // Gadget_Odette_DuoDanceLoopAttackCollider

    private OdetteAbilityHelper() {}

    public static boolean isOdetteClientSkillObj(int gadgetId) {
        return gadgetId >= ODETTE_PARTNER_GADGET_ID && gadgetId <= ODETTE_DUO_DANCE_COLLIDER;
    }

    public static void purgeServerShells(Player player) {
        if (player == null || player.getScene() == null || player.getTeamManager() == null) {
            return;
        }

        var scene = player.getScene();
        Set<Integer> avatarEntityIds = new HashSet<>();
        for (var avatarEntity : player.getTeamManager().getActiveTeam()) {
            if (avatarEntity != null) {
                avatarEntityIds.add(avatarEntity.getId());
            }
        }

        for (GameEntity entity : new ArrayList<>(scene.getEntities().values())) {
            if (!(entity instanceof EntityGadget gadget) || entity instanceof EntityClientGadget) {
                continue;
            }
            int gadgetId = gadget.getGadgetId();
            if (!isOdetteClientSkillObj(gadgetId)) {
                continue;
            }
            if (gadget.getOwner() != null && !ownedByPlayer(gadget.getOwner(), avatarEntityIds)) {
                continue;
            }
            scene.removeEntity(gadget);
            Grasscutter.getLogger()
                    .debug(
                            "[OdetteAbility] uid={} purged server shell gadget={} entity={}",
                            player.getUid(),
                            gadgetId,
                            gadget.getId());
        }
    }

    public static void onClientGadgetCreated(Player player, EntityClientGadget created) {
        if (player == null || created == null || !isOdetteClientSkillObj(created.getGadgetId())) {
            return;
        }

        purgeServerShells(player);
    }

    public static void onClientGadgetDestroyed(Player player, int entityId, int gadgetId) {
        if (player == null || !isOdetteClientSkillObj(gadgetId)) {
            return;
        }

        purgeServerShells(player);
    }

    private static boolean ownedByPlayer(GameEntity owner, Set<Integer> avatarEntityIds) {
        if (owner == null) {
            return false;
        }
        return avatarEntityIds.contains(owner.getId());
    }
}
