package me.arundivakaruni.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class WeaponsCommand extends Command {

    public WeaponsCommand() {
        super("weapon");

        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {

                ItemStack sword = ItemStack.builder(Material.DIAMOND_SWORD)
                        .build();

                player.getInventory().addItemStack(sword);
            }
            else{
                sender.sendMessage("You must be a player to use this command!");
            }
        });
    }
}
