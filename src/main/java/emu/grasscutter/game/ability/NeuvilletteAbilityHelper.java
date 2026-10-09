package emu.grasscutter.game.ability;

import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.entity.EntityBaseGadget;
import emu.grasscutter.game.entity.GameEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper for Neuvillette (Avatar ID 10000087).
 *
 * <p>Handles:
 * 1. Infinite Charged Attack (Equitable Judgment):
 *    Prevents {@code _ABILITY_Neuvillette_ChargeTime} from draining down during firing,
 *    and blocks {@code PRIVATE_ExtraAttack_End} so the beam continues infinitely until
 *    manually cancelled (via jump, dash, or swap).
 * 2. Permanent Sourcewater Droplets:
 *    Prevents droplets (gadget 41087002) from despawning or dying after absorption,
 *    keeping them on the field indefinitely.
 * 3. Continuous absorption even at C0.
 */
public final class NeuvilletteAbilityHelper {
    public static final int NEUVILLETTE_AVATAR_ID = 10000087;
    public static final int SOURCEWATER_DROPLET_GADGET_ID = 41087002;
    public static final int NEUVILLETTE_C6_TALENT_ID = 876;
    public static final String CHARGE_TIME_KEY = "_ABILITY_Neuvillette_ChargeTime";
    public static final String ANIMATOR_END_TRIGGER = "PRIVATE_ExtraAttack_End";

    private NeuvilletteAbilityHelper() {}

    public static boolean isNeuvillette(GameEntity entity) {
        return entity instanceof EntityAvatar avatar
                && avatar.getAvatar() != null
                && avatar.getAvatar().getAvatarId() == NEUVILLETTE_AVATAR_ID;
    }

    public static boolean isNeuvilletteDroplet(GameEntity entity) {
        return entity instanceof EntityBaseGadget gadget
                && gadget.getGadgetId() == SOURCEWATER_DROPLET_GADGET_ID;
    }

    public static boolean isNeuvilletteDropletGadgetId(int gadgetId) {
        return gadgetId == SOURCEWATER_DROPLET_GADGET_ID;
    }

    /**
     * Prevent Neuvillette's charge time from draining down during Equitable Judgment.
     * Keeps it pinned at 3.0f so the beam duration is infinite even at C0.
     */
    public static boolean shouldPreventChargeTimeDecay(GameEntity target, String key, float delta) {
        return delta < 0 && CHARGE_TIME_KEY.equals(key) && isNeuvillette(target);
    }

    /**
     * Prevent PRIVATE_ExtraAttack_End from prematurely terminating Equitable Judgment.
     */
    public static boolean shouldBlockAnimatorTrigger(GameEntity target, String triggerName) {
        return ANIMATOR_END_TRIGGER.equals(triggerName) && isNeuvillette(target);
    }

    /**
     * Include C6 talent (876) so Neuvillette gains continuous droplet absorption
     * and extra hydro torrents while firing even if at C0.
     */
    public static List<Integer> getEffectiveTalentList(Avatar avatar) {
        if (avatar == null) return List.of();
        var set = avatar.getTalentIdList();
        List<Integer> list = set != null ? new ArrayList<>(set) : new ArrayList<>();
        if (avatar.getAvatarId() == NEUVILLETTE_AVATAR_ID && !list.contains(NEUVILLETTE_C6_TALENT_ID)) {
            list.add(NEUVILLETTE_C6_TALENT_ID);
        }
        return list;
    }
}
