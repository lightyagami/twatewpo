package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.player.PlayerRelicFilterData;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.BFGLPLNLINPOuterClass.BFGLPLNLINP;
import emu.grasscutter.net.proto.FPEDEDBJKFAOuterClass.FPEDEDBJKFA;
import emu.grasscutter.net.proto.JKAFINDKMDBOuterClass.JKAFINDKMDB;
import java.util.List;

public class PacketFPEDEDBJKFA extends BasePacket {

    public PacketFPEDEDBJKFA(Player player, List<Integer> avatarIdList) {
        super(PacketOpcodes.FPEDEDBJKFA);

        var rsp = FPEDEDBJKFA.newBuilder().setRetcode(0);

        if (player != null && avatarIdList != null) {
            for (int avatarId : avatarIdList) {
                PlayerRelicFilterData filterData = player.getRelicFilterData(avatarId);
                var jka = JKAFINDKMDB.newBuilder().setAvatarId(avatarId);
                if (filterData != null) {
                    if (filterData.getRelicSetIdList() != null) {
                        jka.addAllEBMLKHEOOCO(filterData.getRelicSetIdList());
                    }
                    if (filterData.getSlotFilters() != null) {
                        for (PlayerRelicFilterData.SlotFilter sf : filterData.getSlotFilters()) {
                            var bf = BFGLPLNLINP.newBuilder()
                                    .setEquipType(sf.getEquipType())
                                    .setBMOICHMNCJC(sf.getBmoichmncjc());
                            if (sf.getAffixList() != null) {
                                bf.addAllAffixList(sf.getAffixList());
                            }
                            jka.addFEGGOKBFGHC(bf);
                        }
                    }
                }
                rsp.addParamList(jka);
            }
        }

        this.setData(rsp.build());
    }
}
