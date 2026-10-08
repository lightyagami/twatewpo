package emu.grasscutter.server.packet.send;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.reliquary.ReliquarySetData;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.GameplayRecommendationReliquarySetResponse._GameplayRecommendationReliquarySetResponse;
import emu.grasscutter.net.proto.IIFCGNCJLGBOuterClass.IIFCGNCJLGB;
import emu.grasscutter.net.proto.KFGOBCFJGFCOuterClass.KFGOBCFJGFC;

public class PacketKFGOBCFJGFC extends BasePacket {

    public PacketKFGOBCFJGFC(int avatarId, int elementType) {
        super(PacketOpcodes.KFGOBCFJGFC);

        var rsp = KFGOBCFJGFC.newBuilder()
                .setAvatarId(avatarId)
                .setElementType(elementType)
                .setRetcode(0);

        var setResp = _GameplayRecommendationReliquarySetResponse.newBuilder();

        for (ReliquarySetData setData : GameData.getReliquarySetDataMap().values()) {
            if (setData == null || setData.getId() <= 0) continue;
            int id = setData.getId();
            if (id >= 14000 && id < 16000) {
                var node = IIFCGNCJLGB.newBuilder()
                        .addPHCIPICKLME(id)
                        .setPermillage(500)
                        .build();
                setResp.addReliquaryDataList(node);
            }
        }

        rsp.setILIODENICDF(setResp);
        this.setData(rsp.build());
    }
}
