package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.PlayerDieTypeOuterClass.PlayerDieType;
import emu.grasscutter.net.proto.WorldPlayerDieNotifyOuterClass.WorldPlayerDieNotify;

public class PacketWorldPlayerDieNotify extends BasePacket {

    public PacketWorldPlayerDieNotify(PlayerDieType playerDieType, int killerId) {
        super(PacketOpcodes.WorldPlayerDieNotify);

        WorldPlayerDieNotify.Builder proto =
                WorldPlayerDieNotify.newBuilder().setDieType(playerDieType);

        if (killerId > 0) {
            proto.setMurdererEntityId(killerId);
        }

        this.setData(proto.build());
    }
}
