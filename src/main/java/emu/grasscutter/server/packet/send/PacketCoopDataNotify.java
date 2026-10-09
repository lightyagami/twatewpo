package emu.grasscutter.server.packet.send;

import emu.grasscutter.data.GameData;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.CoopCgOuterClass.CoopCg;
import emu.grasscutter.net.proto.CoopChapterOuterClass;
import emu.grasscutter.net.proto.CoopDataNotifyOuterClass;
import emu.grasscutter.net.proto.CoopPointOuterClass.CoopPoint;
import emu.grasscutter.net.proto.CoopRewardOuterClass.CoopReward;

public class PacketCoopDataNotify extends BasePacket {

    public PacketCoopDataNotify() {
        super(PacketOpcodes.CoopDataNotify);

        var proto = CoopDataNotifyOuterClass.CoopDataNotify.newBuilder();
        proto.setIsHaveProgress(false);

        GameData.getCoopChapterDataMap()
                .values()
                .forEach(
                        chapterData -> {
                            var chapter = CoopChapterOuterClass.CoopChapter.newBuilder();
                            chapter.setId(chapterData.getId());
                            chapter.setState(CoopChapterOuterClass.CoopChapter.State.State_ACCEPT);

                            var points =
                                    GameData.getCoopPointDataMap().values().stream()
                                            .filter(j -> j.getChapterId() == chapterData.getId())
                                            .toList();

                            int endCount = 0;
                            for (var pt : points) {
                                chapter.addCoopPointList(
                                        CoopPoint.newBuilder()
                                                .setId(pt.getId())
                                                .setState(CoopPoint.State.State_FINISHED));
                                if ("POINT_END".equals(pt.getType())) {
                                    endCount++;
                                    chapter.putSeenEndingMap(pt.getId(), 1);
                                }
                            }

                            chapter.setTotalEndCount(endCount);
                            chapter.setFinishedEndCount(endCount);

                            for (int r = 1; r <= endCount; r++) {
                                chapter.addCoopRewardList(
                                        CoopReward.newBuilder()
                                                .setId(chapterData.getId() * 100 + r)
                                                .setState(CoopReward.State.State_TAKEN));
                                chapter.addCoopCgList(
                                        CoopCg.newBuilder()
                                                .setId(chapterData.getId() * 100 + r)
                                                .setIsUnlock(true));
                                chapter.addCoopCgList(
                                        CoopCg.newBuilder()
                                                .setId(chapterData.getId() * 100 + 10 + r)
                                                .setIsUnlock(true));
                            }

                            proto.addChapterList(chapter);
                        });

        this.setData(proto);
    }
}
