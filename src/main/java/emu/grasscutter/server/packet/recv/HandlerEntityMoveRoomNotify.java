package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.EntityMoveRoomNotifyOuterClass.EntityMoveRoomNotify;
import emu.grasscutter.server.game.GameSession;

@Opcodes(PacketOpcodes.EntityMoveRoomNotify)
public class HandlerEntityMoveRoomNotify extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        EntityMoveRoomNotify req = EntityMoveRoomNotify.parseFrom(payload);
        var player = session.getPlayer();
        if (player != null && req.getDestRoomId() > 0) {
            player.getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_ENTER_ROOM, req.getDestRoomId());
        }
    }
}
