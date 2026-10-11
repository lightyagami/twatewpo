package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.systems.ArtifactTransmuterSystem;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.utils.ProtoWire;

import java.io.ByteArrayOutputStream;

/**
 * MPIJBOMAAJK (3259) — 7.1.0:
 *   JAOLHCKONIP = 1  // version / set index
 *   NCLJJHCJNGB = 3  // schedule 700
 *   retcode = 11
 */
public class PacketReliquaryOfferDefineRsp extends BasePacket {

    public PacketReliquaryOfferDefineRsp(int versionIdx) {
        this(versionIdx, 0, 0);
    }

    public PacketReliquaryOfferDefineRsp(int versionIdx, int retcode, int clientSequence) {
        super(PacketOpcodes.ReliquaryOfferDefineRsp, clientSequence);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ProtoWire.writeUint32Force(out, 1, versionIdx);
        ProtoWire.writeUint32Force(out, 3, ArtifactTransmuterSystem.SCHEDULE_ID);
        ProtoWire.writeUint32Force(out, 11, retcode);
        this.setData(out.toByteArray());
    }
}
