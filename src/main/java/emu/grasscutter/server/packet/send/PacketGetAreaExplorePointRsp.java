package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.GetAreaExplorePointRspOuterClass.GetAreaExplorePointRsp;
import java.util.List;

public class PacketGetAreaExplorePointRsp extends BasePacket {

    public PacketGetAreaExplorePointRsp(Player player, List<Integer> areaIds) {
        super(PacketOpcodes.GetAreaExplorePointRsp);

        GetAreaExplorePointRsp.Builder p = GetAreaExplorePointRsp.newBuilder();
        for (int areaId : areaIds) {
            p.addAreaIdList(areaId);
            p.addExplorePointList(player != null ? player.getExploreAreaPoint(areaId) : 0);
        }

        this.setData(p);
    }
}

