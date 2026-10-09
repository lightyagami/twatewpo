package emu.grasscutter.command.commands;

import emu.grasscutter.GameConstants;
import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.player.Player;
import java.util.List;

@Command(
        label = "setmc",
        aliases = {"mc", "gender", "setgender"},
        usage = {"[boy|girl|aether|lumine]"},
        permission = "player.setmc")
public final class SetMCCommand implements CommandHandler {

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (targetPlayer == null) {
            CommandHandler.sendMessage(sender, "Target player not found.");
            return;
        }

        if (args.isEmpty()) {
            int cur = targetPlayer.getMainCharacterId();
            String name = cur == GameConstants.MAIN_CHARACTER_MALE ? "Aether (Male)" : "Lumine (Female)";
            CommandHandler.sendMessage(targetPlayer, "Current main character: " + name + ". Usage: /setmc [boy|girl|aether|lumine]");
            return;
        }

        String choice = args.get(0).toLowerCase();
        int newId;
        String newName;

        switch (choice) {
            case "boy", "male", "aether", "10000005" -> {
                newId = GameConstants.MAIN_CHARACTER_MALE;
                newName = "Aether (Male)";
            }
            case "girl", "female", "lumine", "10000007" -> {
                newId = GameConstants.MAIN_CHARACTER_FEMALE;
                newName = "Lumine (Female)";
            }
            default -> {
                CommandHandler.sendMessage(targetPlayer, "Invalid choice. Use: /setmc [boy|girl|aether|lumine]");
                return;
            }
        }

        // Ensure target player owns the avatar
        if (targetPlayer.getAvatars().getAvatarById(newId) == null) {
            var avatar = new emu.grasscutter.game.avatar.Avatar(newId);
            targetPlayer.addAvatar(avatar, false);
        }

        targetPlayer.setMainCharacterId(newId);
        targetPlayer.setHeadImage(newId);
        targetPlayer.save();

        CommandHandler.sendMessage(targetPlayer, "Main character set to " + newName + ". Please relog to update character model.");
    }
}
