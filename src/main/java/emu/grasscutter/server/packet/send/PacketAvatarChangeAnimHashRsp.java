package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.AvatarChangeAnimHashRspOuterClass.AvatarChangeAnimHashRsp;

public class PacketAvatarChangeAnimHashRsp extends BasePacket {
    public PacketAvatarChangeAnimHashRsp(long avatarGuid, int animHash) {
        super(PacketOpcodes.AvatarChangeAnimHashRsp);

        var proto = AvatarChangeAnimHashRsp.newBuilder()
                .setRetcode(0)
                .setAvatarGuid(avatarGuid)
                .setAnimHash(animHash)
                .build();

        this.setData(proto);
    }
}
