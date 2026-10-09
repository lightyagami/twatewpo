package emu.grasscutter.game.activity;

import emu.grasscutter.game.props.ActivityType;
import emu.grasscutter.net.proto.ActivityInfoOuterClass;

@GameActivity(ActivityType.NONE)
public class DefaultActivityHandler extends ActivityHandler {
    @Override
    public void onProtoBuild(
            PlayerActivityData playerActivityData,
            ActivityInfoOuterClass.ActivityInfo.Builder activityInfo) {
        if (activityInfo.getActivityId() == 5269) {
            int now = (int) (System.currentTimeMillis() / 1000);
            activityInfo.setBeginTime(now - 86400);
            activityInfo.setFirstDayStartTime(now - 86400);
            activityInfo.setIsStarting(true);

            var detail = emu.grasscutter.net.proto.LeyLineChallengeDetailInfo._LeyLineChallengeDetailInfo.newBuilder();
            for (int i = 1; i <= 6; i++) {
                var diff = emu.grasscutter.net.proto.MKADIBABGKHOuterClass.MKADIBABGKH.newBuilder()
                        .setDifficulty(i);
                // Mark rounds 1..3 as accessible/unlocked
                diff.addCKPHABNPOOH(1);
                diff.addCKPHABNPOOH(2);
                diff.addCKPHABNPOOH(3);
                detail.addDifficultyInfoList(diff.build());
            }
            // OFBDHIJICPL is suspected to mean is_subdued/is_closed - do NOT set it to true
            detail.setOFBDHIJICPL(false);
            detail.setLBCGNOOJDIC(true); // is_open / is_active
            detail.setGCONANDBHMP(1); // default active difficulty / index
            activityInfo.setLeyLineChallengeDetailInfo(detail.build());
        }
    }

    @Override
    public void onInitPlayerActivityData(PlayerActivityData playerActivityData) {}
}
