package emu.grasscutter.server.packet.send;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.reliquary.ReliquarySetData;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.GameplayRecommendationReliquarySetResponse._GameplayRecommendationReliquarySetResponse;
import emu.grasscutter.net.proto.GetGameplayRecommendationRspOuterClass.GetGameplayRecommendationRsp;
import emu.grasscutter.net.proto.IIFCGNCJLGBOuterClass.IIFCGNCJLGB;
import java.util.ArrayList;
import java.util.List;

public class PacketGetGameplayRecommendationRsp extends BasePacket {

    public PacketGetGameplayRecommendationRsp(int avatarId, int type) {
        super(PacketOpcodes.GetGameplayRecommendationRsp);

        var rsp = GetGameplayRecommendationRsp.newBuilder()
                .setAvatarId(avatarId)
                .setType(type)
                .setRetcode(0);

        var setResp = _GameplayRecommendationReliquarySetResponse.newBuilder();

        // Populate with all 5-star artifact sets
        for (ReliquarySetData setData : GameData.getReliquarySetDataMap().values()) {
            if (setData == null || setData.getId() <= 0) continue;
            // Only 5-star sets (14xxx and 15xxx)
            int id = setData.getId();
            if ((id >= 14000 && id < 16000)) {
                var node = IIFCGNCJLGB.newBuilder()
                        .addPHCIPICKLME(id)
                        .setPermillage(500)
                        .build();
                setResp.addReliquaryDataList(node);
            }
        }

        rsp.setReliquarySetResponse(setResp);
        this.setData(rsp.build());
    }
}
