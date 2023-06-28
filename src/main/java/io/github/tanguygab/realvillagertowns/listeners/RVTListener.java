package io.github.tanguygab.realvillagertowns.listeners;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.menus.RVTMenu;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.VillagerManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.List;

public record RVTListener(RealVillagerTowns rvt, VillagerManager vm) implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        for (Entity entity : e.getChunk().getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (vm.isVillagerEntity(entity)) {
                vm.makeVillager(living);
                continue;
            }
            if (entity instanceof Wolf && !entity.getPassengers().isEmpty() && entity.getPassengers().get(0) instanceof LivingEntity liv
                    && vm.isVillagerEntity(liv) && vm.getVillager(liv).getFollowed() == null) {
                entity.remove();
                vm.getVillager(liv).stopFollow();
            }
        }
        if (!rvt.getConfiguration().canSpawnRandomVillager()) return;
        World w = e.getWorld();
        Chunk c = e.getChunk();
        Location tmp = new Location(w, (c.getX() * 16), 0.0D, (c.getZ() * 16));
        Location loc = new Location(w, (c.getX() * 16), w.getHighestBlockYAt(tmp), (c.getZ() * 16));
        loc.setY(loc.getY() + 1.0D);
        if (loc.getWorld() != null && !List.of(Material.WATER, Material.LAVA).contains(loc.getWorld().getHighestBlockAt(loc).getType()))
            e.getChunk().getWorld().spawnEntity(loc, EntityType.VILLAGER);

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        RVTPlayer player;
        if (e.getWhoClicked() instanceof Player p && RVTMenu.openedMenus.containsKey(player = vm.getPlayer(p)))
            e.setCancelled(RVTMenu.openedMenus.get(player).onClick(e.getCurrentItem(), e.getRawSlot(), e.getClick()));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        //still no idea how this works, found nothing that really used tradeList
        if (e.getInventory().getHolder() instanceof Player p)
            vm.getPlayer(p).setTrading(false);

        if (!(e.getPlayer() instanceof Player p)) return;
        RVTPlayer player = vm.getPlayer(p);
        RVTMenu menu = RVTMenu.openedMenus.get(player);
        if (menu != null && menu.inv.equals(e.getInventory()))
            RVTMenu.openedMenus.get(player).onClose();
    }

}
