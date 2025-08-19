package me.arundivakaruni.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class SetHealthCommand extends Command {

    public SetHealthCommand() {
        super("sethealth");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /sethealth <amount>");
        });

        var healthAmountArg = ArgumentType.Float("healthAmount");

        addSyntax((sender, context) -> {
            float newHealth = context.get(healthAmountArg);

            if (newHealth < 0 || newHealth > 20) {
                sender.sendMessage("Health must be between 0 and 20");
                return;
            }

            if (sender instanceof Player player) {
                player.setHealth(newHealth);
                sender.sendMessage("Health set to " + newHealth);
            }
        }, healthAmountArg);
    }
}
