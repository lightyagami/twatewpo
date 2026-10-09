package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.AIPIEIFKIFKOuterClass.AIPIEIFKIFK;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketOCBIKGCIONN;

@Opcodes(PacketOpcodes.AIPIEIFKIFK)
public class HandlerAIPIEIFKIFK extends PacketHandler {
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        AIPIEIFKIFK req = AIPIEIFKIFK.parseFrom(payload);
        Grasscutter.getLogger().info("AIPIEIFKIFK (BatchGameplayRecommendationReq) avatarId={} subReqs={}",
                req.getAvatarId(), req.getJBJIONAPIKLCount());

        session.send(new PacketOCBIKGCIONN(req));
    }
}
