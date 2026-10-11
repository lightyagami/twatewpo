package emu.grasscutter.command.commands;

import static emu.grasscutter.utils.lang.Language.translate;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.command.CommandOutput;
import emu.grasscutter.data.GameData;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.packet.send.PacketAvatarFetterDataNotify;
import java.util.ArrayList;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        label = "setFetterLevel",
        aliases = {"setfetterlvl", "setfriendship", "friendship", "friendmax"},
        permission = "player.setfetterlevel",
        permissionTargeted = "player.setfetterlevel.others")
public final class SetFetterLevelCommand implements CommandHandler {
    @Override
    public CommandLine createCommandLine(Player sender, Player targetPlayer) {
        return new CommandLine(new Args(sender, targetPlayer));
    }

    @CommandLine.Command(name = "setFetterLevel")
    private static final class Args implements Runnable {
        private final Player sender;
        private final Player targetPlayer;

        @Parameters(index = "0", defaultValue = "10", paramLabel = "<level|max|all>")
        private String target;

        @Option(names = {"-a", "--all"}, description = "Apply to all avatars")
        private boolean all;

        private Args(Player sender, Player targetPlayer) {
            this.sender = sender;
            this.targetPlayer = targetPlayer;
        }

        @Override
        public void run() {
            int level = 10;
            boolean applyAll = all;

            if (target != null) {
                String lower = target.toLowerCase();
                if ("all".equals(lower)) {
                    applyAll = true;
                    level = 10;
                } else if ("max".equals(lower)) {
                    level = 10;
                } else {
                    try {
                        level = Integer.parseInt(target);
                    } catch (NumberFormatException e) {
                        CommandOutput.sendMessage(
                                sender, translate(sender, "commands.setFetterLevel.range_error"));
                        return;
                    }
                }
            }

            if (level < 0 || level > 10) {
                CommandOutput.sendMessage(
                        sender, translate(sender, "commands.setFetterLevel.range_error"));
                return;
            }

            if (applyAll) {
                var avatars = new ArrayList<Avatar>();
                targetPlayer.getAvatars().forEach(avatars::add);
                if (avatars.isEmpty()) {
                    CommandOutput.sendMessage(sender, "No characters found.");
                    return;
                }

                for (Avatar avatar : avatars) {
                    avatar.setFetterLevel(level);
                    if (level != 10) {
                        var data = GameData.getAvatarFetterLevelDataMap().get(level);
                        avatar.setFetterExp(data != null ? data.getExp() : 0);
                    }
                    avatar.save();
                }

                targetPlayer.sendPacket(new PacketAvatarFetterDataNotify(avatars));
                CommandOutput.sendMessage(
                        sender, "Set friendship level of all " + avatars.size() + " character(s) to " + level + ".");
            } else {
                var entity = targetPlayer.getTeamManager().getCurrentAvatarEntity();
                if (entity == null) {
                    CommandOutput.sendMessage(sender, "No character is currently active.");
                    return;
                }
                Avatar avatar = entity.getAvatar();
                avatar.setFetterLevel(level);
                if (level != 10) {
                    var data = GameData.getAvatarFetterLevelDataMap().get(level);
                    avatar.setFetterExp(data != null ? data.getExp() : 0);
                }
                avatar.save();
                targetPlayer.sendPacket(new PacketAvatarFetterDataNotify(avatar));
                CommandOutput.sendMessage(
                        sender, translate(sender, "commands.setFetterLevel.success", level));
            }
        }
    }
}
