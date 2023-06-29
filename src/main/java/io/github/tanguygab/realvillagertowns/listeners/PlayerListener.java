package io.github.tanguygab.realvillagertowns.listeners;

import io.github.tanguygab.realvillagertowns.Utils;
import io.github.tanguygab.realvillagertowns.villagers.enums.entity.Mood;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;
import io.github.tanguygab.realvillagertowns.villagers.VillagerManager;
import io.github.tanguygab.realvillagertowns.villagers.enums.speeches.Speech;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public record PlayerListener(VillagerManager vm) implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        vm.loadPlayer(p);
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        vm.savePlayer(vm.getPlayer(p));
        vm.getPlayers().remove(p.getUniqueId());
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Projectile pr && pr.getShooter() instanceof Entity shooter && vm.isVillagerEntity(e.getEntity()) && vm.isVillagerEntity(shooter)) {
            e.setCancelled(true);
            return;
        }
        if (!(e.getDamager() instanceof Player p) || !vm.isVillager(e.getEntity())) return;
        RVTVillager villager = vm.getVillager(e.getEntity());
        if (villager.getEntity().getHealth() - e.getDamage() <= 0.0D) return;

        if (villager.getMood() == Mood.ANGRY) {
            int swing = Utils.random(-1, 1);
            if (swing < 0) swing = 0;
            swing *= -1;
            villager.swingMood(swing);
        } else villager.setMood(Mood.ANGRY,1);

        Speech.PUNCH.send(vm.getPlayer(p),villager);
    }

}
