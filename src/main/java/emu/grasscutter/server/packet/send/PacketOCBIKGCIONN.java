package emu.grasscutter.server.packet.send;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.avatar.AvatarSkillDepotData;
import emu.grasscutter.data.excels.reliquary.ReliquarySetData;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.AIPIEIFKIFKOuterClass.AIPIEIFKIFK;
import emu.grasscutter.net.proto.GameplayRecommendationReliquarySetResponse._GameplayRecommendationReliquarySetResponse;
import emu.grasscutter.net.proto.GameplayRecommendationSkillResponseOuterClass.GameplayRecommendationSkillResponse;
import emu.grasscutter.net.proto.GetGameplayRecommendationReqOuterClass.GetGameplayRecommendationReq;
import emu.grasscutter.net.proto.GetGameplayRecommendationRspOuterClass.GetGameplayRecommendationRsp;
import emu.grasscutter.net.proto.IIFCGNCJLGBOuterClass.IIFCGNCJLGB;
import emu.grasscutter.net.proto.OCBIKGCIONNOuterClass.OCBIKGCIONN;

public class PacketOCBIKGCIONN extends BasePacket {

    public PacketOCBIKGCIONN(AIPIEIFKIFK req) {
        super(PacketOpcodes.OCBIKGCIONN);

        var builder = OCBIKGCIONN.newBuilder()
                .setAvatarId(req.getAvatarId())
                .setKFNDPKDFJAB(req.getKFNDPKDFJAB())
                .setLAFAECIBDKF(req.getLAFAECIBDKF())
                .setReason(req.getReason())
                .setRetcode(0);

        for (GetGameplayRecommendationReq subReq : req.getJBJIONAPIKLList()) {
            int subAvatarId = subReq.getAvatarId() > 0 ? subReq.getAvatarId() : req.getAvatarId();
            var rsp = GetGameplayRecommendationRsp.newBuilder()
                    .setAvatarId(subAvatarId)
                    .setType(subReq.getType())
                    .setRetcode(0);

            if (subReq.hasSkillRequest()) {
                var skillResp = GameplayRecommendationSkillResponse.newBuilder();
                int depotId = subReq.getSkillRequest().getSkillDepotId();
                AvatarSkillDepotData depot = GameData.getAvatarSkillDepotDataMap().get(depotId);
                if (depot != null) {
                    if (depot.getSkills() != null) {
                        for (int skillId : depot.getSkills()) {
                            if (skillId > 0) skillResp.addSkillIdList(skillId);
                        }
                    }
                    if (depot.getEnergySkill() > 0) {
                        skillResp.addSkillIdList(depot.getEnergySkill());
                    }
                }
                rsp.setSkillResponse(skillResp);
            } else if (subReq.hasReliquarySetRequest() || subReq.getType() == 2) {
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
                rsp.setReliquarySetResponse(setResp);
            }

            builder.addBMKPJPNCADB(rsp);
        }

        this.setData(builder.build());
    }
}
