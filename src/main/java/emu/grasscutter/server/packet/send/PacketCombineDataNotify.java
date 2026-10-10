package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.CombineDataNotifyOuterClass.CombineDataNotify;

public class PacketCombineDataNotify extends BasePacket {

    public PacketCombineDataNotify(Iterable<Integer> unlockedCombines) {
        super(PacketOpcodes.CombineDataNotify);

        var unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
        var f9 = com.google.protobuf.UnknownFieldSet.Field.newBuilder();
        for (int id : unlockedCombines) {
            f9.addVarint(id);
        }
        unknownFields.addField(9, f9.build());

        CombineDataNotify proto =
                CombineDataNotify.newBuilder()
                        .addAllCombineIdList(unlockedCombines)
                        .setUnknownFields(unknownFields.build())
                        .build();

        this.setData(proto);
    }
}
