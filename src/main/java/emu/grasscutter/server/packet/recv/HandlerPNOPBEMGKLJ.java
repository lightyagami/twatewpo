package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.player.PlayerRelicFilterData;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.BFGLPLNLINPOuterClass.BFGLPLNLINP;
import emu.grasscutter.net.proto.JKAFINDKMDBOuterClass.JKAFINDKMDB;
import emu.grasscutter.net.proto.PNOPBEMGKLJOuterClass.PNOPBEMGKLJ;
import emu.grasscutter.server.game.GameSession;
import java.util.ArrayList;

@Opcodes(PacketOpcodes.PNOPBEMGKLJ)
public class HandlerPNOPBEMGKLJ extends PacketHandler {
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        PNOPBEMGKLJ req = PNOPBEMGKLJ.parseFrom(payload);
        Player player = session.getPlayer();
        if (player == null) return;

        if (req.hasKGKCHFINFFB()) {
            JKAFINDKMDB config = req.getKGKCHFINFFB();
            int avatarId = config.getAvatarId();

            PlayerRelicFilterData filterData = new PlayerRelicFilterData(avatarId);
            filterData.setRelicSetIdList(new ArrayList<>(config.getEBMLKHEOOCOList()));

            var slotList = new ArrayList<PlayerRelicFilterData.SlotFilter>();
            for (BFGLPLNLINP sfProto : config.getFEGGOKBFGHCList()) {
                slotList.add(new PlayerRelicFilterData.SlotFilter(
                        sfProto.getEquipType(),
                        sfProto.getBMOICHMNCJC(),
                        sfProto.getAffixListList()
                ));
            }
            filterData.setSlotFilters(slotList);

            player.setRelicFilterData(avatarId, filterData);
            player.save();

            Grasscutter.getLogger().info("Saved artifact filter scheme for avatarId={} sets={} slots={}",
                    avatarId, filterData.getRelicSetIdList(), filterData.getSlotFilters().size());
        }
    }
}
