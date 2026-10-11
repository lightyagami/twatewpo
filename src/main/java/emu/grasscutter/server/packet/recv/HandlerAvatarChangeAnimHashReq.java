package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.AvatarChangeAnimHashReqOuterClass.AvatarChangeAnimHashReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketAvatarChangeAnimHashRsp;

@Opcodes(PacketOpcodes.AvatarChangeAnimHashReq)
public class HandlerAvatarChangeAnimHashReq extends PacketHandler {
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        var req = AvatarChangeAnimHashReq.parseFrom(payload);
        var player = session.getPlayer();
        if (player != null && player.getAvatars() != null) {
            var avatar = player.getAvatars().getAvatarByGuid(req.getAvatarGuid());
            if (avatar != null) {
                avatar.setAnimHash(req.getAnimHash());
            }
        }
        Grasscutter.getLogger().debug("AvatarChangeAnimHashReq guid={} animHash={}", req.getAvatarGuid(), req.getAnimHash());
        session.send(new PacketAvatarChangeAnimHashRsp(req.getAvatarGuid(), req.getAnimHash()));
    }
}
