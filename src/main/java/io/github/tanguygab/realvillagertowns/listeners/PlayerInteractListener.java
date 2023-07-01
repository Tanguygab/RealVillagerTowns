package io.github.tanguygab.realvillagertowns.listeners;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.menus.VillagerMenu;
import io.github.tanguygab.realvillagertowns.menus.marry.MarryMenu;
import io.github.tanguygab.realvillagertowns.menus.procreate.ProcreateMenu;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public record PlayerInteractListener(RealVillagerTowns rvt, VillagerManager vm) implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        Player p = e.getPlayer();
        RVTPlayer player = vm.getPlayer(p);
        if (!(e.getRightClicked() instanceof LivingEntity clicked)) return;
        if (vm.isVillagerEntity(e.getRightClicked())
                && rvt.getConfiguration().USE_VILLAGER_INTERACTIONS
                && !clicked.hasMetadata("NPC")
                && !clicked.hasMetadata("shopkeeper")) {
            if (player.isTrading()) {
                player.setTrading(false);
                return;
            }
            e.setCancelled(true);
            if (player.isInteracting()) return;
            RVTVillager villager = vm.getVillager(clicked);
            if (villager.getInHand() == Material.POPPY && p.getUniqueId() == villager.getLikes()) {
                p.getInventory().addItem(new ItemStack(Material.BLUE_ORCHID));
                villager.getTrait().send(player);
                rvt.giveItem(villager, null);
                return;
            }
            if (player.getGifting() == villager) {
                rvt.giveGift(player, villager);
                player.setInteracting(true);
                player.setGifting(null);
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
        if (clicked instanceof Player pClicked && rvt.getConfiguration().ENABLE_PLAYER_MARRIAGE) {
            e.setCancelled(true);
            clickedPlayer(player,pClicked);
            return;
        }
        if (clicked instanceof Wolf wolf) clickedWolf(wolf, e.getRightClicked().getPassengers());
    }

    private void clickedPlayer(RVTPlayer player, Player clicked) {
        if (clicked.getUniqueId() == player.getPartner()) {
            new ProcreateMenu(player,vm.getPlayer(clicked)).onOpen();
            return;
        }
        new MarryMenu(player,vm.getPlayer(clicked)).onOpen();
    }
    private void clickedWolf(Wolf wolf, List<Entity> passengers) {
        if (passengers.isEmpty() || !vm.isVillagerEntity(passengers.get(0))) return;
        wolf.remove();
        vm.getVillager(passengers.get(0)).stopFollow();
    }

}
