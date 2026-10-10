package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.systems.ArtifactTransmuterSystem;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.server.game.GameSession;

/** ReliquaryOffer define / purchase artifact. */
@Opcodes(PacketOpcodes.ReliquaryOfferDefineReq)
public class HandlerReliquaryOfferDefineReq extends PacketHandler {
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        emu.grasscutter.Grasscutter.getLogger().info(
                "ReliquaryOfferDefineReq received uid={} len={} payload={}",
                session.getPlayer().getUid(),
                payload.length,
                emu.grasscutter.utils.Utils.bytesToHex(payload));
        ArtifactTransmuterSystem.handleDefine(session.getPlayer(), payload);
    }
}
