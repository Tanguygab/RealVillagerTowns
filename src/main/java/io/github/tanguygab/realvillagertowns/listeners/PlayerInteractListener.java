package io.github.tanguygab.realvillagertowns.listeners;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.menus.VillagerMenu;
import io.github.tanguygab.realvillagertowns.menus.marry.MarryMenu;
import io.github.tanguygab.realvillagertowns.menus.procreate.ProcreateMenu;
import io.github.tanguygab.realvillagertowns.villagers.Gender;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;
import io.github.tanguygab.realvillagertowns.villagers.VillagerManager;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public record PlayerInteractListener(RealVillagerTowns rvt, VillagerManager vm) implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        RVTPlayer player = vm.getPlayer(p);
        if (!(e.getRightClicked() instanceof LivingEntity clicked)) return;
        if (!player.isTrading()
                && vm.isVillagerEntity(e.getRightClicked())
                && vm.USE_VILLAGER_INTERACTIONS
                && !player.isInteracting()
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
                    player.sendMessage(rvt.getText(trait.toLowerCase() + "-gift", "Like", player, villager)
                            .replace("<nice>", nice).replace("<sex2>", type));
                    rvt.giveItem(villager, null);
                    return;
                }
            }
            if (player.getGifting() == villager) {
                rvt.giveGift(player, villager);
                player.setInteracting(true);
                rvt.getServer().getScheduler().scheduleSyncDelayedTask(rvt, () -> player.setInteracting(false),  3L);
                return;
            }
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item.getType() == Material.LEAD && clicked.setLeashHolder(p)) {
                if (item.getAmount() > 1)
                    item.setAmount(item.getAmount() - 1);
                else p.getInventory().setItemInMainHand(null);
            }
            new VillagerMenu(player, villager).onOpen();
            return;
        }
        if (clicked instanceof Player pClicked && rvt.getConfig().getBoolean("enablePlayerMarriage")) {
            e.setCancelled(true);
            clickedPlayer(player,pClicked);
            return;
        }
        if (clicked instanceof Wolf wolf) {
            clickedWolf(wolf, e.getRightClicked().getPassengers());
            return;
        }
        if (player.isTrading()) rvt.getServer().getScheduler().scheduleSyncDelayedTask(rvt, () -> player.setTrading(false),  20L);
    }

    private void clickedPlayer(RVTPlayer player, Player clicked) {
        if (clicked.getUniqueId() == player.getPartner()) {
            new ProcreateMenu(player,vm.getPlayer(clicked)).onOpen();
            return;
        }
        if (rvt.getConfig().getList("playerButtons").contains("marry"))
            new MarryMenu(player,vm.getPlayer(clicked)).onOpen();
    }
    private void clickedWolf(Wolf wolf, List<Entity> passengers) {
        if (passengers.isEmpty() || !vm.isVillagerEntity(passengers.get(0))) return;
        wolf.remove();
        vm.getVillager(passengers.get(0)).stopFollow();
    }

}
