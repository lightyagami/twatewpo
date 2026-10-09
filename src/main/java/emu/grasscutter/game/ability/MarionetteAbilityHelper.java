package emu.grasscutter.game.ability;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.entity.EntityClientGadget;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.player.Player;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Marionette (Sandrone, Avatar ID 10000133) skill objects (Robot puppet, funnels, extra attack bullets / loops)
 * are client-owned via {@code EvtCreateGadgetNotify}.
 *
 * <p>Server {@link EntityGadget} shells create empty, duplicate copies that the caster can see.
 * Those shells lack client-side despawn logic, so they stack indefinitely with rapid bullet loops
 * and puppet re-casts, causing runaway entity counts, packet flooding, and client lag.
 */
public final class MarionetteAbilityHelper {
    public static final int MARIONETTE_AVATAR_ID = 10000133;

    public static final int EXTRA_ATTACK_BULLET_01 = 41133001;
    public static final int EXTRA_ATTACK_BULLET_02 = 41133002;
    public static final int EXTRA_ATTACK_BULLET_01_NOTARGET = 41133003;
    public static final int EXTRA_ATTACK_BULLET_02_NOTARGET = 41133004;
    public static final int FUNNEL_BULLET = 41133005;
    public static final int ROBOT_GADGET_ID = 41133006; // Avatar_MarionetteNew_Robot
    public static final int FUNNEL_BULLET_ENHANCED = 41133007;
    public static final int EXTRA_ATTACK_BULLET_LOOP = 41133008;
    public static final int EXTRA_ATTACK_BULLET_LOOP_HITSCENE = 41133010;

    private static final int MAX_ACTIVE_BULLETS = 12;

    private MarionetteAbilityHelper() {}

    /** SkillObj gadgets Marionette creates; client owns the chain. */
    public static boolean isMarionetteClientSkillObj(int gadgetId) {
        return gadgetId >= 41133001 && gadgetId <= 41133010;
    }

    public static boolean isMarionetteBullet(int gadgetId) {
        return gadgetId != ROBOT_GADGET_ID && isMarionetteClientSkillObj(gadgetId);
    }

    /**
     * Drop server shells for Marionette skill objs owned by this player's avatars.
     */
    public static void purgeServerShells(Player player) {
        if (player == null || player.getScene() == null || player.getTeamManager() == null) {
            return;
        }

        var scene = player.getScene();
        Set<Integer> avatarEntityIds = avatarEntityIds(player);

        for (GameEntity entity : new ArrayList<>(scene.getEntities().values())) {
            if (!(entity instanceof EntityGadget gadget) || entity instanceof EntityClientGadget) {
                continue;
            }
            int gadgetId = gadget.getGadgetId();
            if (!isMarionetteClientSkillObj(gadgetId)) {
                continue;
            }
            if (!ownedByPlayer(gadget.getOwner(), player, avatarEntityIds)) {
                continue;
            }
            scene.removeEntity(gadget);
            Grasscutter.getLogger()
                    .debug(
                            "[MarionetteAbility] uid={} purged server shell gadget={} entity={}",
                            player.getUid(),
                            gadgetId,
                            gadget.getId());
        }
    }

    /**
     * Enforce single active Robot summon and cap rapid bullet loops.
     */
    public static void onClientGadgetCreated(Player player, EntityClientGadget created) {
        if (player == null || created == null || !isMarionetteClientSkillObj(created.getGadgetId())) {
            return;
        }

        purgeServerShells(player);

        var scene = player.getScene();
        if (scene == null) return;

        // Ensure at most 1 Robot summon exists per player
        if (created.getGadgetId() == ROBOT_GADGET_ID) {
            for (GameEntity entity : new ArrayList<>(scene.getEntities().values())) {
                if (!(entity instanceof EntityClientGadget gadget) || gadget == created) {
                    continue;
                }
                if (gadget.getOwner() != player) {
                    continue;
                }
                if (gadget.getGadgetId() == ROBOT_GADGET_ID) {
                    scene.onPlayerDestroyGadget(gadget.getId());
                    Grasscutter.getLogger()
                            .debug(
                                    "[MarionetteAbility] uid={} removed stale robot puppet entity={}",
                                    player.getUid(),
                                    gadget.getId());
                }
            }
        } else if (isMarionetteBullet(created.getGadgetId())) {
            // Cap active bullet loop gadgets to avoid entity accumulation
            List<EntityClientGadget> bullets = new ArrayList<>();
            for (GameEntity entity : scene.getEntities().values()) {
                if (entity instanceof EntityClientGadget gadget
                        && gadget.getOwner() == player
                        && isMarionetteBullet(gadget.getGadgetId())) {
                    bullets.add(gadget);
                }
            }
            if (bullets.size() > MAX_ACTIVE_BULLETS) {
                int toRemove = bullets.size() - MAX_ACTIVE_BULLETS;
                for (int i = 0; i < toRemove; i++) {
                    EntityClientGadget oldest = bullets.get(i);
                    if (oldest != created) {
                        scene.onPlayerDestroyGadget(oldest.getId());
                    }
                }
            }
        }
    }

    /**
     * When client destroys a Marionette gadget, purge any leftover server shells.
     */
    public static void onClientGadgetDestroyed(Player player, int entityId, int gadgetId) {
        if (player == null || !isMarionetteClientSkillObj(gadgetId)) {
            return;
        }
        purgeServerShells(player);
    }

    private static Set<Integer> avatarEntityIds(Player player) {
        Set<Integer> ids = new HashSet<>();
        for (EntityAvatar avatar : player.getTeamManager().getActiveTeam()) {
            if (avatar != null) {
                ids.add(avatar.getId());
            }
        }
        return ids;
    }

    private static boolean ownedByPlayer(
            GameEntity owner, Player player, Set<Integer> avatarEntityIds) {
        if (owner == null) {
            return false;
        }
        if (avatarEntityIds.contains(owner.getId())) {
            return true;
        }
        return owner instanceof EntityAvatar avatar && avatar.getPlayer() == player;
    }
}
