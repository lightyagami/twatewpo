package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketCoopDataNotify;
import emu.grasscutter.server.packet.send.PacketPersonalLineAllDataRsp;

@Opcodes(PacketOpcodes.PersonalLineAllDataReq)
public class HandlerPersonalLineAllDataReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        var player = session.getPlayer();
        session.send(
                new PacketPersonalLineAllDataRsp(
                        player.getQuestManager().getMainQuests().values(),
                        player.getPersonalLineList(),
                        player.getForcedFinishedQuests()));
        session.send(new PacketCoopDataNotify());
    }
}
