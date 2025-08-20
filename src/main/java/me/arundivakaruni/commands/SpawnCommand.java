package me.arundivakaruni.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.*;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.utils.time.TimeUnit;

import java.util.List;


public class SpawnCommand extends Command {

    public SpawnCommand() {
        super("spawn");

        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                EntityCreature entity = new EntityCreature(EntityType.ZOMBIE);
                entity.setInstance(player.getInstance(), player.getPosition().add(1, 0, 1));

                entity.addAIGroup(
                        List.of(new MeleeAttackGoal(entity, 1.2, 20, TimeUnit.SERVER_TICK)),
                        List.of(new ClosestEntityTarget(entity, 32, entity1 -> entity1 instanceof Player))
                );

                entity.eventNode().addListener(EntitySpawnEvent.class, event ->
                        player.getInstance().sendMessage(Component.text("The zombie has spawned")));

                entity.eventNode().addListener(EntityDeathEvent.class, event ->
                        player.getInstance().sendMessage(Component.text("The zombie has died")));
            }
        });

        var entityNode = EventNode.type("entity_node", EventFilter.ENTITY);
        entityNode.addListener(PlayerEntityInteractEvent.class, event -> {
            if (event.getHand() != PlayerHand.MAIN || event.getTarget() != entityNode) return;
            event.getPlayer().sendMessage(event.getHand().name());
            event.getPlayer().sendMessage(Component.text("You have interacted with the zombie"));
            if (event.getTarget() instanceof LivingEntity livingEntity) {
                livingEntity.kill();
            }
        });
        MinecraftServer.getGlobalEventHandler().addChild(entityNode);

        var entityNameArg = ArgumentType.EntityType("entityType");

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                EntityType entityType = context.get(entityNameArg);
                Entity entity = new Entity(entityType);
                entity.setInstance(player.getInstance(), player.getPosition().add(1, 0, 1));
            }
        }, entityNameArg);
    }
}
