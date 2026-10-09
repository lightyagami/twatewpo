package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.*;
import java.io.ByteArrayOutputStream;

public class PacketChooseCurAvatarTeamRsp extends BasePacket {

    public PacketChooseCurAvatarTeamRsp(int teamId) {
        super(PacketOpcodes.ChooseCurAvatarTeamRsp);

        // 7.1 ChooseCurAvatarTeamRsp (CmdId: 29488):
        // field 5: cur_team_id (varint)
        // field 12: retcode (varint, 0 for success)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeVarintField(baos, 5, teamId);
        writeVarintField(baos, 12, 0);

        this.setData(baos.toByteArray());
    }

    private static void writeVarintField(ByteArrayOutputStream out, int fieldNumber, int value) {
        int tag = (fieldNumber << 3);
        writeRawVarint(out, tag);
        writeRawVarint(out, value);
    }

    private static void writeRawVarint(ByteArrayOutputStream out, int value) {
        while ((value & ~0x7F) != 0) {
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.write(value & 0x7F);
    }
}
