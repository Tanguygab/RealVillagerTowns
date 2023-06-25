package io.github.tanguygab.realvillagertowns.listeners;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.menus.VillagerMenu;
import io.github.tanguygab.realvillagertowns.menus.marry.MarryMenu;
import io.github.tanguygab.realvillagertowns.menus.procreate.ProcreateMenu;
import io.github.tanguygab.realvillagertowns.villagers.Gender;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;
import io.github.tanguygab.realvillagertowns.villagers.VillagerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.UUID;

public class PlayerInteractListener implements Listener {

    private final RealVillagerTowns rvt;
    private final VillagerManager vm;
    private final FileConfiguration config;

    public PlayerInteractListener(RealVillagerTowns rvt) {
        this.rvt = rvt;
        vm = rvt.getVillagerManager();
        config = rvt.getConfig();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (!(e.getRightClicked() instanceof LivingEntity clicked)) return;
        if (!rvt.tradeList.contains(p.getName())
                && vm.isVillagerEntity(e.getRightClicked())
                && vm.USE_VILLAGER_INTERACTIONS
                && !rvt.interactList.contains(p.getUniqueId().toString())
                && !clicked.hasMetadata("NPC")
                && !clicked.hasMetadata("shopkeeper")) {
            e.setCancelled(true);
            RVTVillager villager = vm.getVillager(clicked);
            if (villager.getInHand() == Material.POPPY) {
                UUID likes = villager.getLikes();
                if (p.getUniqueId() == likes) {
                    p.getInventory().addItem(new ItemStack(Material.BLUE_ORCHID));
                    String trait = villager.getTrait();
                    Gender pGender = vm.getPlayer(p).getGender();
                    String nice = "beautiful", type = "girl";
                    if (pGender == Gender.MALE) {
                        nice = "handsome";
                        type = "boy";
                    }
                    p.sendMessage(rvt.getText(trait.toLowerCase() + "-gift", "Like", p, clicked).replace("<nice>", nice).replace("<sex2>", type));
                    rvt.giveItem(clicked, null);
                    return;
                }
            }
            if (rvt.giftMap.get(p.getName()) != null && (rvt.giftMap.get(p.getName())).equals(clicked.getUniqueId())) {
                rvt.giveGift(p, clicked);
                rvt.giftMap.remove(p.getName());
                String id = p.getUniqueId().toString();
                rvt.interactList.add(id);
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(rvt, () -> rvt.interactList.remove(id),  3L);
                return;
            }
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item.getType() == Material.LEAD && clicked.setLeashHolder(p)) {
                if (item.getAmount() > 1)
                    item.setAmount(item.getAmount() - 1);
                else p.getInventory().setItemInMainHand(null);
            }
            new VillagerMenu(p, clicked).onOpen();
            return;
        }
        if (clicked instanceof Player pClicked && config.getBoolean("enablePlayerMarriage")) {
            e.setCancelled(true);
            clickedPlayer(p,pClicked);
            return;
        }
        if (clicked instanceof Wolf wolf) {
            clickedWolf(wolf, e.getRightClicked().getPassengers());
            return;
        }
        if (rvt.tradeList.contains(p.getName()))
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(rvt, () -> rvt.tradeList.remove(p.getName()),  20L);
    }

    private void clickedPlayer(Player player, Player clicked) {
        if (clicked.getUniqueId() == vm.getPlayer(player).getPartner()) {
            new ProcreateMenu(player,clicked).onOpen();
            return;
        }
        if (config.getList("playerButtons").contains("marry"))
            new MarryMenu(player,clicked).onOpen();
    }
    private void clickedWolf(Wolf wolf, List<Entity> passengers) {
        if (passengers.isEmpty() || !vm.isVillagerEntity(passengers.get(0))) return;
        wolf.remove();
        rvt.stopFollow((LivingEntity) passengers.get(0));
    }

}
