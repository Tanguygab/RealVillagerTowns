package io.github.tanguygab.realvillagertowns;

import io.github.tanguygab.realvillagertowns.menus.RVTMenu;
import io.github.tanguygab.realvillagertowns.menus.VillagerMenu;
import io.github.tanguygab.realvillagertowns.menus.marry.MarryMenu;
import io.github.tanguygab.realvillagertowns.menus.procreate.ProcreateMenu;
import io.github.tanguygab.realvillagertowns.villagers.VillagerManager;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RVTListener implements Listener {

    private final RealVillagerTowns rvt;
    private final VillagerManager vm;
    private final FileConfiguration config;
    
    public RVTListener(RealVillagerTowns rvt) {
        this.rvt = rvt;
        vm = rvt.getVillagerManager();
        config = rvt.getConfig();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        for (Entity entity : e.getChunk().getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (vm.isVillagerEntity(entity)) {
                if (!vm.isVillager(entity))
                    vm.makeVillager(living);
                else rvt.disguise(living);
                continue;
            }
            if (entity instanceof Wolf && !entity.getPassengers().isEmpty() && entity.getPassengers().get(0) instanceof LivingEntity liv && vm.isVillagerEntity(liv) && rvt.followMap.get(liv) == null) {
                entity.remove();
                rvt.stopFollow(liv);
            }
        }
        if (!config.getBoolean("spawnRandomVillagers")) return;
        int r = Utils.random(config.getInt("randomVillagerChance") - 1 + 1) + 1;
        if (r != 1) return;
        World w = e.getWorld();
        Chunk c = e.getChunk();
        Location tmp = new Location(w, (c.getX() * 16), 0.0D, (c.getZ() * 16));
        Location loc = new Location(w, (c.getX() * 16), w.getHighestBlockYAt(tmp), (c.getZ() * 16));
        loc.setY(loc.getY() + 1.0D);
        if (!List.of(Material.WATER, Material.LAVA).contains(loc.getWorld().getHighestBlockAt(loc).getType()))
            e.getChunk().getWorld().spawnEntity(loc, EntityType.VILLAGER);

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p && RVTMenu.openedMenus.containsKey(p))
            e.setCancelled(RVTMenu.openedMenus.get(p).onClick(e.getCurrentItem(),e.getRawSlot(),e.getClick()));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        RVTMenu menu = RVTMenu.openedMenus.get(p);
        if (menu != null && menu.inv.equals(e.getInventory()))
            RVTMenu.openedMenus.get(p).onClose();
    }

    //still no idea how this works, found nothing that really used tradeList
    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof Player p)
            rvt.tradeList.remove(p.getName());
    }
}
