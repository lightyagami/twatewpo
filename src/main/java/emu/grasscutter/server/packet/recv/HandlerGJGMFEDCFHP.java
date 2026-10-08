package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.GJGMFEDCFHPOuterClass.GJGMFEDCFHP;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketKFGOBCFJGFC;

@Opcodes(PacketOpcodes.GJGMFEDCFHP)
public class HandlerGJGMFEDCFHP extends PacketHandler {
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        GJGMFEDCFHP req = GJGMFEDCFHP.parseFrom(payload);
        int avatarId = req.getAvatarId();
        int elementType = req.getElementType();

        Grasscutter.getLogger().info("GJGMFEDCFHP avatarId={} elementType={}", avatarId, elementType);

        session.send(new PacketKFGOBCFJGFC(avatarId, elementType));
    }
}
