package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.RewardData;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.AvatarFetterLevelRewardReqOuterClass.AvatarFetterLevelRewardReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.*;
import io.netty.buffer.ByteBufUtil;
import com.google.protobuf.UnknownFieldSet;

import java.util.Map;

@Opcodes(PacketOpcodes.AvatarFetterLevelRewardReq)
public class HandlerAvatarFetterLevelRewardReq extends PacketHandler {
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        Player player = session.getPlayer();
        if (player == null) return;

        try {
            AvatarFetterLevelRewardReq req = AvatarFetterLevelRewardReq.parseFrom(payload);
            Grasscutter.getLogger().info("AvatarFetterLevelRewardReq received: guid={}, fetterLevel={}, unknownFields={}, hex={}",
                    req.getAvatarGuid(), req.getFetterLevel(), req.getUnknownFields(), ByteBufUtil.hexDump(payload));

            long avatarGuid = req.getAvatarGuid();
            int fetterLevel = req.getFetterLevel();

            // Extract any values from unknown fields if fields shifted in 7.1
            if (!req.getUnknownFields().asMap().isEmpty()) {
                for (Map.Entry<Integer, UnknownFieldSet.Field> entry : req.getUnknownFields().asMap().entrySet()) {
                    UnknownFieldSet.Field field = entry.getValue();
                    if (!field.getVarintList().isEmpty()) {
                        long val = field.getVarintList().get(0);
                        if (avatarGuid == 0 && (val > 100 || val > 10000000)) {
                            avatarGuid = val;
                        } else if (fetterLevel == 0 && val >= 1 && val <= 10) {
                            fetterLevel = (int) val;
                        }
                    }
                }
            }

            // 1. Try to find avatar by guid
            Avatar avatar = player.getAvatars().getAvatarByGuid(avatarGuid);

            // 2. Try by avatarId if avatarGuid was an id
            if (avatar == null && avatarGuid != 0) {
                avatar = player.getAvatars().getAvatarById((int) avatarGuid);
            }

            // 3. Fallback: current active avatar
            if (avatar == null && player.getTeamManager().getCurrentAvatarEntity() != null) {
                Avatar active = player.getTeamManager().getCurrentAvatarEntity().getAvatar();
                if (active != null && active.getFetterLevel() >= 10 && !player.getNameCardList().contains(active.getNameCardId())) {
                    avatar = active;
                }
            }

            // 4. Fallback: search any avatar with fetterLevel >= 10 whose namecard is unearned
            if (avatar == null) {
                for (Avatar a : player.getAvatars()) {
                    if (a.getFetterLevel() >= 10 && a.getNameCardId() != 0 && !player.getNameCardList().contains(a.getNameCardId())) {
                        avatar = a;
                        break;
                    }
                }
            }

            if (avatar == null) {
                Grasscutter.getLogger().warn("AvatarFetterLevelRewardReq: avatar not found for guid={}", avatarGuid);
                session.send(new PacketAvatarFetterLevelRewardRsp(avatarGuid, fetterLevel));
                return;
            }

            long finalGuid = avatar.getGuid();
            int targetFetterLevel = fetterLevel > 0 ? fetterLevel : avatar.getFetterLevel();

            // Verify avatar has reached level 10
            if (avatar.getFetterLevel() < 10) {
                Grasscutter.getLogger().warn("AvatarFetterLevelRewardReq: avatar {} fetter level {} < 10", avatar.getAvatarId(), avatar.getFetterLevel());
                session.send(new PacketAvatarFetterLevelRewardRsp(finalGuid, targetFetterLevel));
                return;
            }

            int rewardId = avatar.getNameCardRewardId();
            int cardId = avatar.getNameCardId();

            if (cardId == 0 && rewardId != 0) {
                RewardData card = GameData.getRewardDataMap().get(rewardId);
                if (card != null && card.getRewardItemList() != null && !card.getRewardItemList().isEmpty()) {
                    cardId = card.getRewardItemList().get(0).getItemId();
                }
            }

            Grasscutter.getLogger().info("AvatarFetterLevelRewardReq: granting reward for avatarId={}, rewardId={}, cardId={}",
                    avatar.getAvatarId(), rewardId, cardId);

            if (cardId != 0) {
                player.addNameCard(cardId);
                try {
                    GameItem item = new GameItem(cardId);
                    player.getInventory().addItem(item, ActionReason.FetterLevelReward);
                } catch (Exception e) {
                    Grasscutter.getLogger().warn("Could not add namecard as GameItem: {}", e.getMessage());
                }
                player.sendPacket(new PacketUnlockNameCardNotify(cardId));
                player.save();
            }

            session.send(new PacketAvatarFetterDataNotify(avatar));
            session.send(new PacketAvatarFetterLevelRewardRsp(finalGuid, 10, rewardId));
        } catch (Exception ex) {
            Grasscutter.getLogger().error("Error handling AvatarFetterLevelRewardReq", ex);
        }
    }
}
