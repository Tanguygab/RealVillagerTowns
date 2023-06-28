package io.github.tanguygab.realvillagertowns.listeners;

import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;
import io.github.tanguygab.realvillagertowns.villagers.VillagerManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public record VillagerListener(VillagerManager vm) implements Listener {

    @EventHandler
    public void onLivingEntitySpawn(CreatureSpawnEvent e) {
        LivingEntity v = e.getEntity();
        if (vm.isVillagerEntity(e.getEntity()) && !vm.isVillager(e.getEntity()))
            vm.makeVillager(v);
    }

    @EventHandler
    public void onLivingEntityDie(EntityDeathEvent e) {
        if (vm.isVillager(e.getEntity())) vm.villagerDeath(e.getEntity());
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        if (!e.getEntity().getPassengers().isEmpty() && e.getEntity().getPassengers().get(0) instanceof LivingEntity entity
                && vm.isVillager(entity) && vm.getVillager(entity).getFollowed() != null)
            e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerTarget(EntityTargetEvent e) {
        Entity entity = e.getEntity();
        if (!vm.isVillager(entity) || !(e.getTarget() instanceof Player p)) return;
        RVTVillager villager = vm.getVillager(entity);
        RVTPlayer player = vm.getPlayer(p);
        if (player.getLikes().contains(villager.getUniqueId()) && (player.getHappiness(villager) > 25 || villager.getDrunk() >= 1))
            e.setCancelled(true);
    }

}
