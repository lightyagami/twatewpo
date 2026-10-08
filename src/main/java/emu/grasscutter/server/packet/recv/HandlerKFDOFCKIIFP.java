package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.KFDOFCKIIFPOuterClass.KFDOFCKIIFP;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketFPEDEDBJKFA;

@Opcodes(PacketOpcodes.KFDOFCKIIFP)
public class HandlerKFDOFCKIIFP extends PacketHandler {
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        KFDOFCKIIFP req = KFDOFCKIIFP.parseFrom(payload);
        Grasscutter.getLogger().info("KFDOFCKIIFP avatarIds={}", req.getAvatarIdListList());

        session.send(new PacketFPEDEDBJKFA(session.getPlayer(), req.getAvatarIdListList()));
    }
}
