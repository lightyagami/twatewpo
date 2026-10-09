package emu.grasscutter.command.commands;

import static emu.grasscutter.utils.lang.Language.translate;

import emu.grasscutter.command.*;
import emu.grasscutter.data.GameData;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.packet.send.PacketAvatarFetterDataNotify;
import java.util.List;

@Command(
        label = "setFetterLevel",
        usage = {"<level> [all]", "all <level>", "max [all]"},
        aliases = {"setfetterlvl", "setfriendship", "friendship", "friendmax"},
        permission = "player.setfetterlevel",
        permissionTargeted = "player.setfetterlevel.others")
public final class SetFetterLevelCommand implements CommandHandler {

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        boolean isFriendMaxAlias = false;
        // Check if invoked via friendmax alias or with no args when label/alias is friendmax
        // (args might be empty for /friendmax, which defaults to level 10 for all or current)

        int fetterLevel = 10;
        boolean all = false;

        if (args.isEmpty()) {
            // Default to max (10) for active character
            fetterLevel = 10;
        } else if (args.size() == 1) {
            String arg0 = args.get(0).toLowerCase();
            if ("all".equals(arg0)) {
                all = true;
                fetterLevel = 10;
            } else if ("max".equals(arg0)) {
                fetterLevel = 10;
            } else {
                try {
                    fetterLevel = Integer.parseInt(arg0);
                } catch (NumberFormatException e) {
                    sendUsageMessage(sender);
                    return;
                }
            }
        } else if (args.size() == 2) {
            String arg0 = args.get(0).toLowerCase();
            String arg1 = args.get(1).toLowerCase();
            if ("all".equals(arg0)) {
                all = true;
                if ("max".equals(arg1)) {
                    fetterLevel = 10;
                } else {
                    try {
                        fetterLevel = Integer.parseInt(arg1);
                    } catch (NumberFormatException e) {
                        sendUsageMessage(sender);
                        return;
                    }
                }
            } else if ("all".equals(arg1)) {
                all = true;
                if ("max".equals(arg0)) {
                    fetterLevel = 10;
                } else {
                    try {
                        fetterLevel = Integer.parseInt(arg0);
                    } catch (NumberFormatException e) {
                        sendUsageMessage(sender);
                        return;
                    }
                }
            } else {
                sendUsageMessage(sender);
                return;
            }
        } else {
            sendUsageMessage(sender);
            return;
        }

        if (fetterLevel < 0 || fetterLevel > 10) {
            CommandHandler.sendMessage(
                    sender, translate(sender, "commands.setFetterLevel.range_error"));
            return;
        }

        if (all) {
            var avatars = new java.util.ArrayList<Avatar>();
            targetPlayer.getAvatars().forEach(avatars::add);
            if (avatars.isEmpty()) {
                CommandHandler.sendMessage(sender, "No characters found.");
                return;
            }

            for (Avatar avatar : avatars) {
                avatar.setFetterLevel(fetterLevel);
                if (fetterLevel != 10) {
                    var data = GameData.getAvatarFetterLevelDataMap().get(fetterLevel);
                    avatar.setFetterExp(data != null ? data.getExp() : 0);
                }
                avatar.save();
            }

            targetPlayer.sendPacket(new PacketAvatarFetterDataNotify(avatars));
            CommandHandler.sendMessage(
                    sender, "Set friendship level of all " + avatars.size() + " character(s) to " + fetterLevel + ".");
        } else {
            var entity = targetPlayer.getTeamManager().getCurrentAvatarEntity();
            if (entity == null) {
                CommandHandler.sendMessage(sender, "No character is currently active.");
                return;
            }
            Avatar avatar = entity.getAvatar();

            avatar.setFetterLevel(fetterLevel);
            if (fetterLevel != 10) {
                var data = GameData.getAvatarFetterLevelDataMap().get(fetterLevel);
                avatar.setFetterExp(data != null ? data.getExp() : 0);
            }
            avatar.save();

            targetPlayer.sendPacket(new PacketAvatarFetterDataNotify(avatar));
            CommandHandler.sendMessage(
                    sender, translate(sender, "commands.setFetterLevel.success", fetterLevel));
        }
    }
}
