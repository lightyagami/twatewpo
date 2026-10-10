package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.FinishedTalkIdListNotifyOuterClass.FinishedTalkIdListNotify;

public class PacketFinishedTalkIdListNotify extends BasePacket {

    public PacketFinishedTalkIdListNotify(Iterable<Integer> talkIds) {
        super(PacketOpcodes.FinishedTalkIdListNotify);

        FinishedTalkIdListNotify p =
                FinishedTalkIdListNotify.newBuilder()
                        .addAllFinishedTalkIdList(talkIds)
                        .build();

        this.setData(p);
    }

    public PacketFinishedTalkIdListNotify(int talkId) {
        super(PacketOpcodes.FinishedTalkIdListNotify);

        FinishedTalkIdListNotify p =
                FinishedTalkIdListNotify.newBuilder()
                        .addFinishedTalkIdList(talkId)
                        .build();

        this.setData(p);
    }
}
