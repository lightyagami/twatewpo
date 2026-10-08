package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.GetGameplayRecommendationReqOuterClass.GetGameplayRecommendationReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketGetGameplayRecommendationRsp;

@Opcodes(PacketOpcodes.GetGameplayRecommendationReq)
public class HandlerGetGameplayRecommendationReq extends PacketHandler {
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        GetGameplayRecommendationReq req = GetGameplayRecommendationReq.parseFrom(payload);
        int avatarId = req.getAvatarId();
        int type = req.getType();

        Grasscutter.getLogger().info("GetGameplayRecommendationReq avatarId={} type={} detailCase={}",
                avatarId, type, req.getDetailCase());

        session.send(new PacketGetGameplayRecommendationRsp(avatarId, type));
    }
}
