package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.entity.*;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.SceneEntityDrownReqOuterClass.SceneEntityDrownReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketSceneEntityDrownRsp;

@Opcodes(PacketOpcodes.SceneEntityDrownReq)
public class HandlerSceneEntityDrownReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        SceneEntityDrownReq req = SceneEntityDrownReq.parseFrom(payload);

        int entityId = req.getEntityId();
        if (session.getPlayer() == null || session.getPlayer().getScene() == null) {
            return;
        }

        if (entityId == 0 && session.getPlayer().getTeamManager() != null) {
            var curAvatar = session.getPlayer().getTeamManager().getCurrentAvatarEntity();
            if (curAvatar != null) {
                entityId = curAvatar.getId();
            }
        }

        var scene = session.getPlayer().getScene();
        GameEntity entity = scene.getEntityById(entityId);

        if (entity != null) {
            if (entity instanceof EntityMonster || entity instanceof EntityAvatar) {
                entity.setFightProperty(FightProperty.FIGHT_PROP_CUR_HP, 0);
                scene.killEntity(entity, 0);
            } else if (entity instanceof EntityClientGadget || entity instanceof EntityGadget) {
                scene.removeEntity(entity);
            }
        }

        // Always acknowledge drown requests to satisfy the client actor state machine
        // and prevent endless retry storms.
        session.send(new PacketSceneEntityDrownRsp(entityId));
        scene.broadcastPacket(new PacketSceneEntityDrownRsp(entityId));
    }
}
