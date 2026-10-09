package emu.grasscutter.server.packet.send;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.game.quest.*;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.LockedPersonallineDataOuterClass.LockedPersonallineData;
import emu.grasscutter.net.proto.PersonalLineAllDataRspOuterClass;
import java.util.*;
import java.util.stream.Collectors;

public class PacketPersonalLineAllDataRsp extends BasePacket {

    public PacketPersonalLineAllDataRsp(
            Collection<GameMainQuest> gameMainQuestList, Set<Integer> unlockedLines, Set<Integer> forcedFinishedQuests) {
        super(PacketOpcodes.PersonalLineAllDataRsp);

        var proto = PersonalLineAllDataRspOuterClass.PersonalLineAllDataRsp.newBuilder();

        var questList =
                gameMainQuestList.stream()
                        .map(GameMainQuest::getChildQuests)
                        .map(Map::values)
                        .flatMap(Collection::stream)
                        .map(GameQuest::getSubQuestId)
                        .collect(Collectors.toSet());

        var miaoUnlocked = new ArrayList<Integer>();
        var miaoLocked = new ArrayList<Integer>();

        // Lines that are already finished or unlocked should NOT be added to lockedPersonalLineList.
        // A line in lockedPersonalLineList causes the client to show a lock icon and "Quick Start Available".
        GameData.getPersonalLineDataMap().values().stream()
                .filter(i -> !questList.contains(i.getStartQuestId()))
                .filter(i -> unlockedLines == null || !unlockedLines.contains(i.getId()))
                .filter(i -> {
                    if (forcedFinishedQuests == null || forcedFinishedQuests.isEmpty()) return true;
                    int startSub = i.getStartQuestId();
                    var qData = GameData.getQuestDataMap().get(startSub);
                    int mainId = qData != null ? qData.getMainId() : (startSub / 100);
                    return !forcedFinishedQuests.contains(mainId) && !forcedFinishedQuests.contains(startSub);
                })
                .forEach(
                        i -> {
                            proto.addLockedPersonalLineList(
                                    LockedPersonallineData.newBuilder()
                                            .setPersonalLineId(i.getId())
                                            .setLockReason(
                                                    LockedPersonallineData.LockReason.LockReason_QUEST)
                                            .build());
                            if (unlockedLines != null && unlockedLines.contains(i.getId())) {
                                miaoUnlocked.add(i.getId());
                            } else {
                                miaoLocked.add(i.getId());
                            }
                        });

        this.setData(proto);
    }

    public PacketPersonalLineAllDataRsp(
            Collection<GameMainQuest> gameMainQuestList, Set<Integer> unlockedLines) {
        this(gameMainQuestList, unlockedLines, Collections.emptySet());
    }
}
