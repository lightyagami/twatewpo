package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.utils.ProtoWire;

import java.io.ByteArrayOutputStream;

/**
 * ReliquaryDust SelectRsp (7.1.0: CmdId 9200 JHFKFGNEJNJ { int32 retcode = 2; }).
 */
public class PacketReliquaryDustSelectRsp extends BasePacket {
    public static final int OPCODE = 9200;

    public PacketReliquaryDustSelectRsp(int retcode) {
        this(OPCODE, 2, retcode, 0, null, 0L);
    }

    public PacketReliquaryDustSelectRsp(int retcode, int clientSequence) {
        this(OPCODE, 2, retcode, clientSequence, null, 0L);
    }

    public PacketReliquaryDustSelectRsp(int retcode, byte[] requestHeader) {
        this(OPCODE, 2, retcode, 0, requestHeader, 0L);
    }

    public PacketReliquaryDustSelectRsp(
            int opcode, int retcodeField, int retcode, int clientSequence) {
        this(opcode, retcodeField, retcode, clientSequence, null, 0L);
    }

    public PacketReliquaryDustSelectRsp(
            int opcode, int retcodeField, int retcode, byte[] requestHeader, long guid) {
        this(opcode, retcodeField, retcode, 0, requestHeader, guid);
    }

    public PacketReliquaryDustSelectRsp(
            int opcode, int retcodeField, int retcode, int clientSequence, byte[] requestHeader, long guid) {
        super(opcode, clientSequence);
        if (requestHeader != null && requestHeader.length > 0) {
            this.setHeader(requestHeader);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (guid != 0L) {
            if (opcode == 8036) {
                ProtoWire.writeUint64Force(out, 4, guid);
            } else if (opcode == 6541) {
                ProtoWire.writeUint64Force(out, 14, guid);
            } else if (opcode == 4711) {
                ProtoWire.writeUint64Force(out, 8, guid);
            }
        }
        ProtoWire.writeUint32Force(out, retcodeField, retcode);
        this.setData(out.toByteArray());
    }
}
