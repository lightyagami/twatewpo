package emu.grasscutter.command.commands;

import emu.grasscutter.GameConstants;
import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.command.CommandOutput;
import emu.grasscutter.game.player.Player;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

@Command(
        label = "setmc",
        aliases = {"mc", "gender", "setgender"},
        permission = "player.setmc")
public final class SetMCCommand implements CommandHandler {

    @Override
    public CommandLine createCommandLine(Player sender, Player targetPlayer) {
        return new CommandLine(new Args(sender, targetPlayer));
    }

    @CommandLine.Command(name = "setmc")
    private final class Args implements Runnable {
        private final Player sender;
        private final Player targetPlayer;

        @Parameters(index = "0", arity = "0..1", paramLabel = "[boy|girl|aether|lumine]")
        private String choice;

        private Args(Player sender, Player targetPlayer) {
            this.sender = sender;
            this.targetPlayer = targetPlayer;
        }

        @Override
        public void run() {
            if (targetPlayer == null) {
                CommandOutput.sendMessage(sender, "Target player not found.");
                return;
            }

            if (choice == null) {
                int cur = targetPlayer.getMainCharacterId();
                String name = cur == GameConstants.MAIN_CHARACTER_MALE ? "Aether (Male)" : "Lumine (Female)";
                CommandOutput.sendMessage(targetPlayer, "Current main character: " + name + ". Usage: /setmc [boy|girl|aether|lumine]");
                return;
            }

            String lower = choice.toLowerCase();
            int newId;
            String newName;

            switch (lower) {
                case "boy", "male", "aether", "10000005" -> {
                    newId = GameConstants.MAIN_CHARACTER_MALE;
                    newName = "Aether (Male)";
                }
                case "girl", "female", "lumine", "10000007" -> {
                    newId = GameConstants.MAIN_CHARACTER_FEMALE;
                    newName = "Lumine (Female)";
                }
                default -> {
                    CommandOutput.sendMessage(targetPlayer, "Invalid choice. Use: /setmc [boy|girl|aether|lumine]");
                    return;
                }
            }

            if (targetPlayer.getAvatars().getAvatarById(newId) == null) {
                var avatar = new emu.grasscutter.game.avatar.Avatar(newId);
                targetPlayer.addAvatar(avatar, false);
            }

            targetPlayer.setMainCharacterId(newId);
            targetPlayer.setHeadImage(newId);
            targetPlayer.save();

            CommandOutput.sendMessage(targetPlayer, "Main character set to " + newName + ". Please relog to update character model.");
        }
    }
}
