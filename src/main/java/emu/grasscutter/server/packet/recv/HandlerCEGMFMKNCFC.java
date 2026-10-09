package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.server.game.GameSession;

@Opcodes(PacketOpcodes.CEGMFMKNCFC)
public class HandlerCEGMFMKNCFC extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        var player = session.getPlayer();
        if (player == null) return;

        Grasscutter.getLogger().info("HandlerCEGMFMKNCFC: Player {} interacted with Imaginarium Theater magic book (scene {})", player.getUid(), player.getSceneId());

        // Send CENHPLPLEKE response (CmdId 24459: int32 retcode = 15; -> field 15, wire type 0, value 0 = empty or 0x78, 0x00)
        BasePacket rsp = new BasePacket(PacketOpcodes.CENHPLPLEKE);
        rsp.setData(new byte[]{0x78, 0x00});
        session.send(rsp);

        // Ensure Imaginarium Theater open states are fully unlocked on client
        for (int stateId = 4700; stateId <= 4710; stateId++) {
            player.getProgressManager().setOpenState(stateId, 1, true);
        }

        // Send dungeon entry info for Theater entrance point (pointId = 1, sceneId = 48000)
        session.getServer().getDungeonSystem().sendEntryInfoFor(player, 1, 48000);
    }
}
