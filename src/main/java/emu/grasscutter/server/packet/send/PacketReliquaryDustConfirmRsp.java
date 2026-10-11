package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.utils.ProtoWire;

import java.io.ByteArrayOutputStream;

/** S2C confirm rsp — retcode (7.1: HBGAHEIKJCH 22317, retcode field 5). */
public class PacketReliquaryDustConfirmRsp extends BasePacket {
    public static final int OPCODE = 22317;

    public PacketReliquaryDustConfirmRsp(long guid, int retcode) {
        this(guid, retcode, 0, null);
    }

    public PacketReliquaryDustConfirmRsp(
            long guid, int retcode, int clientSequence, byte[] requestHeader) {
        super(OPCODE, clientSequence);
        if (requestHeader != null && requestHeader.length > 0) {
            this.setHeader(requestHeader);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ProtoWire.writeUint32Force(out, 5, retcode);
        this.setData(out.toByteArray());
    }
}
