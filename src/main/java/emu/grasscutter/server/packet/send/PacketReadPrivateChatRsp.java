package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.utils.ProtoWire;

import java.io.ByteArrayOutputStream;

/** ReadPrivateChatRsp (CmdId: 21003, int32 retcode = 13). */
public class PacketReadPrivateChatRsp extends BasePacket {
    public PacketReadPrivateChatRsp(int clientSequence) {
        super(PacketOpcodes.ReadPrivateChatRsp, clientSequence);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ProtoWire.writeUint32Force(out, 13, 0);
        this.setData(out.toByteArray());
    }
}
