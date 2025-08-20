package me.arundivakaruni;

import me.arundivakaruni.commands.SetHealthCommand;
import me.arundivakaruni.commands.WeaponsCommand;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.*;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0,
                40, Block.GRASS_BLOCK));

        //Standard minecraft lighting
        instanceContainer.setChunkSupplier(LightingChunk::new);

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });

        //Breaking and picking up blocks
        globalEventHandler.addListener(PlayerBlockBreakEvent.class, event -> {
            //Getting the material
            var material = event.getBlock().registry().material();
            if (material != null) {
                //Creating a new itemStack of the material
                var itemStack = ItemStack.of(material);
                //Creating a new itemEntity so that it can actually appear on the ground
                ItemEntity itemEntity = new ItemEntity(itemStack);
                //Setting the instance of where the item spawns
                itemEntity.setInstance(event.getInstance(), event.getBlockPosition().add(0.5, 0.5, 0.5));
                //Setting the pickup delay, as for some reason block doesn't get picked up if delay is 0
                itemEntity.setPickupDelay(Duration.ofMillis(500));
            }
        });

        //This can have event listeners of any type
        EventNode<Event> allNode = EventNode.all("all");
        allNode.addListener(PickupItemEvent.class, event -> {
            var itemStack = event.getItemStack();
            if (event.getLivingEntity() instanceof Player player) {
                player.getInventory().addItemStack(itemStack);
            }
        });

        //Player dropping/throwing blocks
        var playerNode = EventNode.type("players", EventFilter.PLAYER);
        playerNode.addListener(ItemDropEvent.class, event -> {
            ItemEntity itemEntity = new ItemEntity(event.getItemStack());
            itemEntity.setInstance(event.getInstance(), event.getPlayer().getPosition());
            itemEntity.setVelocity(event.getPlayer().getPosition().add(0, 1, 0).direction().mul(8));
            itemEntity.setPickupDelay(Duration.ofSeconds(1));
        });
        allNode.addChild(playerNode);

        globalEventHandler.addChild(allNode);

        //Initializing the commands classes
        MinecraftServer.getCommandManager().register(new SetHealthCommand());
        MinecraftServer.getCommandManager().register(new WeaponsCommand());

        //To save the world after disconnection
        var scheduler = MinecraftServer.getSchedulerManager();
        scheduler.buildShutdownTask(() -> {
            instanceManager.getInstances().forEach(Instance::saveChunksToStorage);
        });

        //Repeatedly saving the world
        var task = scheduler.buildTask(() -> {
                    System.out.println("Saving all instances...");
            instanceManager.getInstances().forEach(Instance::saveChunksToStorage);
        })
                .repeat(30, TimeUnit.SECONDS.toChronoUnit())
                .delay(Duration.ofMinutes(1))
                .schedule();

        //Enables online mode, skins, etc
        MojangAuth.init();

        minecraftServer.start("0.0.0.0", 25566);
    }
}