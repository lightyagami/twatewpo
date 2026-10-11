package emu.grasscutter.game.ability;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.entity.EntityClientGadget;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.player.Player;
import java.util.ArrayList;

public final class OdetteAbilityHelper {
    public static final int ODETTE_AVATAR_ID = 10000150;

    public static final int ODETTE_PARTNER_GADGET_ID = 41150001; // Avatar_Odette_Partner
    public static final int ODETTE_PARTNER_MOVE_ASIST_GADGET_ID = 41150002; // Avatar_Odette_Partner_MoveAsist

    private OdetteAbilityHelper() {}

    public static boolean isOdetteClientSkillObj(int gadgetId) {
        return gadgetId == ODETTE_PARTNER_GADGET_ID || gadgetId == ODETTE_PARTNER_MOVE_ASIST_GADGET_ID;
    }

    public static void purgeServerShells(Player player) {
        if (player == null || player.getScene() == null) {
            return;
        }

        var scene = player.getScene();
        for (GameEntity entity : new ArrayList<>(scene.getEntities().values())) {
            if (!(entity instanceof EntityGadget gadget) || entity instanceof EntityClientGadget) {
                continue;
            }
            int gadgetId = gadget.getGadgetId();
            if (!isOdetteClientSkillObj(gadgetId)) {
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
}
