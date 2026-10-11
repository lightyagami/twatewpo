package emu.grasscutter.game.ability;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.ability.actions.ActionSetAnimatorTrigger;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.Position;
import emu.grasscutter.net.proto.EntityMoveInfoOuterClass.EntityMoveInfo;
import emu.grasscutter.net.proto.MotionInfoOuterClass.MotionInfo;
import emu.grasscutter.net.proto.MotionStateOuterClass.MotionState;
import emu.grasscutter.net.proto.VectorOuterClass.Vector;
import emu.grasscutter.server.packet.send.PacketSceneEntityMoveNotify;
import emu.grasscutter.server.packet.send.PacketServerGlobalValueChangeNotify;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages Odette's (Avatar ID 10000150) summoned double (Avatar_Odette_Partner, 41150001).
 *
 * <p>In official config, the double is driven by {@code AutoAttack_Handler} on a 2-second thinkInterval,
 * which alternates between Attack01 (Plume) and Attack02 (Wing) dance strikes, and blinks into melee
 * range of enemies. Since {@code onThinkInterval} is not executed server-side in Grasscutter, this helper
 * provides the periodic AI scheduler, target scanning, blink movement, animator triggers, and DuoDance
 * recast handling.
 */
public final class OdetteAbilityHelper {
    public static final int ODETTE_AVATAR_ID = 10000150;

    public static final int ODETTE_PARTNER_GADGET_ID = 41150001; // Avatar_Odette_Partner
    public static final int ODETTE_PARTNER_MOVE_ASIST_GADGET_ID = 41150002; // Avatar_Odette_Partner_MoveAsist
    public static final int ODETTE_BULLET_FOLLOW_AVATAR = 41150003; // Avatar_Odette_Bullet_FollowAvatar
    public static final int ODETTE_DUO_DANCE_COLLIDER = 41150004; // Gadget_Odette_DuoDanceLoopAttackCollider

    public static final int SKILL_E = 11502;
    public static final int SKILL_DUO_DANCE = 11503;
    public static final int SKILL_BURST = 11505;

    public static final String GV_FOUND_PARTNER_MARK = "_ABILITY_Odette_Found_Partner_Mark";
    public static final String GV_PARTNER_IS_ACTIVE = "_ABILITY_Odette_Partner_IsActive";
    public static final String GV_FOLLOW_TARGET_IS_ACTIVE = "_ABILITY_Odette_FollowTarget_IsActive";
    public static final String GV_TALENT1_REMAIN_TIME = "_ABILITY_Odette_Talent1_PartnerRemainTime";

    private static final long DEFAULT_DURATION_MS = 15_000L;
    private static final int ATTACK_INTERVAL_SEC = 2;
    private static final float COMBAT_SEARCH_RADIUS = 25.0f;
    private static final float BLINK_THRESHOLD_DIST = 3.5f;
    private static final float BLINK_ATTACK_OFFSET = 1.8f;

    private static final Map<Integer, PartnerSession> sessions = new ConcurrentHashMap<>();

    private OdetteAbilityHelper() {}

    private static class PartnerSession {
        final int uid;
        final int partnerEntityId;
        final int moveAsistEntityId;
        final long spawnTimeMs;
        final AtomicBoolean attackToggle = new AtomicBoolean(false);
        int taskId = -1;
        long durationMs;

        PartnerSession(int uid, int partnerEntityId, int moveAsistEntityId, long durationMs) {
            this.uid = uid;
            this.partnerEntityId = partnerEntityId;
            this.moveAsistEntityId = moveAsistEntityId;
            this.spawnTimeMs = System.currentTimeMillis();
            this.durationMs = durationMs;
        }
    }

    public static void onSkillStart(Player player, EntityAvatar avatar, int skillId) {
        if (player == null || avatar == null) return;

        if (skillId == SKILL_DUO_DANCE) {
            onDuoDance(player, avatar);
        } else if (skillId == SKILL_BURST) {
            onBurst(player, avatar);
        }
    }

    public static void onPartnerCreated(Player player, EntityGadget partner) {
        if (player == null || partner == null) return;
        int uid = player.getUid();

        clearPlayerState(player);

        int moveAsistId = 0;
        if (partner.getOwner() instanceof EntityGadget parentGadget
                && parentGadget.getGadgetId() == ODETTE_PARTNER_MOVE_ASIST_GADGET_ID) {
            moveAsistId = parentGadget.getId();
        }

        PartnerSession session = new PartnerSession(uid, partner.getId(), moveAsistId, DEFAULT_DURATION_MS);
        sessions.put(uid, session);

        updatePartnerGlobalValues(player, 1.0f);

        ActionSetAnimatorTrigger.broadcastAnimatorParam(partner, "CanDoSkill", 4, true);
        ActionSetAnimatorTrigger.broadcastAnimatorParam(partner, "Born", 9, true);

        session.taskId = Grasscutter.getGameServer().getScheduler().scheduleDelayedRepeatingTask(
                () -> tickPartner(session), 1, ATTACK_INTERVAL_SEC);
    }

    private static void tickPartner(PartnerSession session) {
        Player player = Grasscutter.getGameServer().getPlayerByUid(session.uid);
        if (player == null || !player.isOnline() || player.getScene() == null) {
            cleanupSession(session, true);
            return;
        }

        var scene = player.getScene();
        GameEntity entity = scene.getEntityById(session.partnerEntityId);
        if (!(entity instanceof EntityGadget partner) || !partner.isAlive()) {
            cleanupSession(session, true);
            return;
        }

        long elapsed = System.currentTimeMillis() - session.spawnTimeMs;
        if (elapsed >= session.durationMs) {
            cleanupSession(session, true);
            return;
        }

        EntityMonster target = findNearestMonster(partner);

        if (target != null && target.getPosition() != null) {
            Position monsterPos = target.getPosition();
            Position partnerPos = partner.getPosition();
            float dx = monsterPos.getX() - partnerPos.getX();
            float dz = monsterPos.getZ() - partnerPos.getZ();
            float dist = (float) Math.sqrt(dx * dx + dz * dz);

            if (dist > BLINK_THRESHOLD_DIST && dist > 0.01f) {
                float newX = monsterPos.getX() - (dx / dist) * BLINK_ATTACK_OFFSET;
                float newZ = monsterPos.getZ() - (dz / dist) * BLINK_ATTACK_OFFSET;
                Position newPos = new Position(newX, monsterPos.getY(), newZ);
                Position newRot = faceToward(newPos, monsterPos);

                teleportEntity(partner, newPos, newRot);
            } else {
                partner.getRotation().set(faceToward(partnerPos, monsterPos));
            }

            boolean isAttack01 = session.attackToggle.getAndSet(!session.attackToggle.get());
            String triggerName = isAttack01 ? "DoAttack01" : "DoAttack02";
            ActionSetAnimatorTrigger.broadcastAnimatorParam(partner, triggerName, 9, true);
        } else {
            EntityAvatar avatar = player.getTeamManager().getCurrentAvatarEntity();
            if (avatar != null && avatar.getPosition() != null && partner.getPosition() != null) {
                float distToAvatar = (float) partner.getPosition().computeDistance(avatar.getPosition());
                if (distToAvatar > 12.0f) {
                    Position newPos = new Position(
                            avatar.getPosition().getX() + 1.2f,
                            avatar.getPosition().getY(),
                            avatar.getPosition().getZ() + 1.2f);
                    Position newRot = faceToward(newPos, avatar.getPosition());
                    teleportEntity(partner, newPos, newRot);
                }
            }
        }
    }

    private static void onDuoDance(Player player, EntityAvatar avatar) {
        PartnerSession session = sessions.get(player.getUid());
        if (session == null || player.getScene() == null) return;

        GameEntity entity = player.getScene().getEntityById(session.partnerEntityId);
        if (!(entity instanceof EntityGadget partner) || !partner.isAlive()) return;

        Position partnerPos = partner.getPosition();
        Position newAvatarPos = new Position(partnerPos.getX() + 0.8f, partnerPos.getY(), partnerPos.getZ() + 0.8f);
        teleportEntity(avatar, newAvatarPos, faceToward(newAvatarPos, partnerPos));

        ActionSetAnimatorTrigger.broadcastAnimatorParam(partner, "DoAttackSpecial", 9, true);
    }

    private static void onBurst(Player player, EntityAvatar avatar) {
        PartnerSession session = sessions.get(player.getUid());
        if (session == null || player.getScene() == null) return;

        GameEntity entity = player.getScene().getEntityById(session.partnerEntityId);
        if (!(entity instanceof EntityGadget partner) || !partner.isAlive()) return;

        session.durationMs += DEFAULT_DURATION_MS;

        ActionSetAnimatorTrigger.broadcastAnimatorParam(partner, "DoBurst", 9, true);
    }

    private static EntityMonster findNearestMonster(EntityGadget partner) {
        var scene = partner.getScene();
        if (scene == null || partner.getPosition() == null) return null;

        Position refPos = partner.getPosition();
        EntityMonster best = null;
        float minSq = COMBAT_SEARCH_RADIUS * COMBAT_SEARCH_RADIUS;

        for (GameEntity entity : scene.getEntities().values()) {
            if (entity instanceof EntityMonster monster && monster.isAlive() && monster.getPosition() != null) {
                float dsq = (float) refPos.computeDistance(monster.getPosition());
                if (dsq < minSq) {
                    minSq = dsq;
                    best = monster;
                }
            }
        }
        return best;
    }

    private static void teleportEntity(GameEntity entity, Position newPos, Position newRot) {
        if (entity == null || entity.getScene() == null) return;

        entity.getPosition().set(newPos);
        entity.getRotation().set(newRot);

        MotionInfo motionInfo = MotionInfo.newBuilder()
                .setPos(newPos.toProto())
                .setRot(newRot.toProto())
                .setSpeed(Vector.newBuilder().build())
                .setState(MotionState.MotionState_MOTION_STANDBY)
                .build();
        EntityMoveInfo moveInfo = EntityMoveInfo.newBuilder()
                .setEntityId(entity.getId())
                .setMotionInfo(motionInfo)
                .setSceneTime(entity.getScene().getSceneTimeSeconds())
                .build();
        entity.getScene().broadcastPacket(new PacketSceneEntityMoveNotify(moveInfo));
    }

    private static Position faceToward(Position from, Position to) {
        float dx = to.getX() - from.getX();
        float dz = to.getZ() - from.getZ();
        if (Math.abs(dx) < 0.01f && Math.abs(dz) < 0.01f) {
            return new Position(0.0f, 0.0f, 0.0f);
        }
        float yaw = (float) Math.toDegrees(Math.atan2(dx, dz));
        return new Position(0.0f, yaw, 0.0f);
    }

    private static void updatePartnerGlobalValues(Player player, float value) {
        if (player == null || player.getTeamManager() == null) return;
        EntityAvatar avatar = player.getTeamManager().getCurrentAvatarEntity();
        if (avatar == null) return;

        player.sendPacket(new PacketServerGlobalValueChangeNotify(avatar, GV_FOUND_PARTNER_MARK, value));
        player.sendPacket(new PacketServerGlobalValueChangeNotify(avatar, GV_PARTNER_IS_ACTIVE, value));
        player.sendPacket(new PacketServerGlobalValueChangeNotify(avatar, GV_FOLLOW_TARGET_IS_ACTIVE, value));
    }

    private static void cleanupSession(PartnerSession session, boolean removeEntities) {
        if (session.taskId >= 0) {
            try {
                Grasscutter.getGameServer().getScheduler().cancelTask(session.taskId);
            } catch (Throwable ignored) {}
            session.taskId = -1;
        }

        sessions.remove(session.uid);

        Player player = Grasscutter.getGameServer().getPlayerByUid(session.uid);
        if (player != null) {
            updatePartnerGlobalValues(player, 0.0f);

            if (removeEntities && player.getScene() != null) {
                var scene = player.getScene();
                GameEntity p = scene.getEntityById(session.partnerEntityId);
                if (p != null) scene.removeEntity(p);
                if (session.moveAsistEntityId > 0) {
                    GameEntity m = scene.getEntityById(session.moveAsistEntityId);
                    if (m != null) scene.removeEntity(m);
                }
            }
        }
    }

    public static void clearPlayerState(Player player) {
        if (player == null) return;
        PartnerSession session = sessions.remove(player.getUid());
        if (session != null) {
            cleanupSession(session, true);
        }
    }

    public static void clearEntityState(int entityId) {
        for (PartnerSession s : sessions.values()) {
            if (s.partnerEntityId == entityId || s.moveAsistEntityId == entityId) {
                cleanupSession(s, false);
                break;
            }
        }
    }
}
