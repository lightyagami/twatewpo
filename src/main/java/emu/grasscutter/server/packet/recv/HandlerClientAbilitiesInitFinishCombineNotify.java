package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.tps.TpsWeaponSystem;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.AbilityInvokeEntryOuterClass.AbilityInvokeEntry;
import emu.grasscutter.net.proto.ClientAbilitiesInitFinishCombineNotifyOuterClass.ClientAbilitiesInitFinishCombineNotify;
import emu.grasscutter.net.proto.EntityAbilityInvokeEntryOuterClass.EntityAbilityInvokeEntry;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketCombineDataNotify;
import emu.grasscutter.server.packet.send.PacketOpenStateUpdateNotify;

@Opcodes(PacketOpcodes.ClientAbilitiesInitFinishCombineNotify)
public class HandlerClientAbilitiesInitFinishCombineNotify extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        ClientAbilitiesInitFinishCombineNotify notif =
                payload.length == 0
                        ? ClientAbilitiesInitFinishCombineNotify.getDefaultInstance()
                        : ClientAbilitiesInitFinishCombineNotify.parseFrom(payload);

        Player player = session.getPlayer();

        if (player.getUnlockedCombines().isEmpty()) {
            player.getServer().getCombineSystem().onPlayerLogin(player);
        }
        session.send(new PacketCombineDataNotify(player.getUnlockedCombines()));
        session.send(new PacketOpenStateUpdateNotify(player));

        Grasscutter.getLogger().debug("Handled combine UI init for uid {}", player.getUid());

        // Call skill end in the player's ability manager.
        player.getAbilityManager().onSkillEnd(player);

        for (EntityAbilityInvokeEntry entry : notif.getEntityInvokeListList()) {
            for (AbilityInvokeEntry ability : entry.getInvokesList()) {
                player.getAbilityManager().onAbilityInvoke(ability);
                player.getClientAbilityInitFinishHandler().addEntry(ability.getForwardType(), ability);
            }

            if (entry.getInvokesList().size() > 0) {
                session.getPlayer().getClientAbilityInitFinishHandler().update(session.getPlayer());
            }
            TpsWeaponSystem.onClientAbilityInit(player, entry.getEntityId());
        }
    }
}
