package io.github.tanguygab.realvillagertowns.listeners;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.Utils;
import io.github.tanguygab.realvillagertowns.menus.VillagerMenu;
import io.github.tanguygab.realvillagertowns.menus.marry.MarryMenu;
import io.github.tanguygab.realvillagertowns.menus.procreate.ProcreateMenu;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;
import io.github.tanguygab.realvillagertowns.villagers.VillagerManager;
import io.github.tanguygab.realvillagertowns.villagers.enums.GiftType;
import io.github.tanguygab.realvillagertowns.villagers.enums.entity.Mood;
import io.github.tanguygab.realvillagertowns.villagers.enums.entity.RVTEntityType;
import io.github.tanguygab.realvillagertowns.villagers.enums.speeches.BooleanSpeech;
import io.github.tanguygab.realvillagertowns.villagers.enums.speeches.Speech;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

public record PlayerInteractListener(RealVillagerTowns rvt, VillagerManager vm) implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        Player p = e.getPlayer();
        RVTPlayer player = vm.getPlayer(p);
        if (!(e.getRightClicked() instanceof LivingEntity clicked)) return;
        if (vm.isVillagerEntity(e.getRightClicked()) && rvt.getConfiguration().USE_VILLAGER_INTERACTIONS
                && !clicked.hasMetadata("NPC") && !clicked.hasMetadata("shopkeeper")) {
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
                giveGift(player, villager);
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
                return;
            }
            rvt.getServer().getScheduler().runTask(rvt,()->new VillagerMenu(player, villager).onOpen());
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

    public void giveGift(RVTPlayer player, RVTVillager villager) {
        PlayerInventory inv = player.getPlayer().getInventory();
        ItemStack gift = inv.getItemInMainHand();
        int amount = gift.getAmount() - 1;
        if (amount <= 0) inv.setItemInMainHand(null);
        else gift.setAmount(amount);

        GiftType type = GiftType.fromItem(gift);
        switch (type) {
            case BOW -> rvt.giveItem(villager, new ItemStack(Material.BOW));
            case RING -> marry(player, villager);
            case REGULAR_DRUNK,HIGH_DRUNK -> {
                int i = type == GiftType.HIGH_DRUNK ? 2 : 1;
                if (Utils.random(1, 3) == 1) {
                    villager.setDrunk(villager.getDrunk()+i);
                    player.villagerMessage(villager,rvt.getMessages().DRUNK);
                }
            }
            default -> {
                Speech.fromGift(type).send(player,villager);
                switch (type) {
                    case SMALL -> player.setHappiness(villager, 1);
                    case REGULAR -> player.setHappiness(villager, Utils.random(1, 5));
                    case GREAT -> player.setHappiness(villager, Utils.random(5, 15));
                    case LOVE -> {
                        if (villager.getMood() != Mood.HAPPY || villager.getMoodLevel() < 4)
                            villager.setMood(Mood.HAPPY,3);
                        player.setHappiness(villager, Utils.random(2, 6));
                    }
                }
            }
        }
        villager.swingMood(1);
    }

    public void marry(RVTPlayer player, RVTVillager villager) {
        int hearts = player.getHappiness(villager);

        if (player.getPartner() != null) {
            Speech.MARRY_CANT.send(player,villager);
            player.setHappiness(villager,Utils.random(-50, -20));
            villager.setMood(Mood.SAD,3);
            return;
        }

        if (rvt.getConfiguration().enoughHeartsToMarry(hearts)) {
            BooleanSpeech.MARRY.sendGood(player,villager);
            player.setHappiness(villager, Utils.random(10, 30));
            villager.setMood(Mood.HAPPY,5);

            player.marry(villager.getUniqueId(), RVTEntityType.VILLAGER);
            villager.marry(player.getUniqueId(),RVTEntityType.PLAYER);
            return;
        }

        BooleanSpeech.MARRY.sendBad(player,villager);
        player.setHappiness(villager,Utils.random(-50, -20));
        villager.setMood(Mood.ANGRY,3);
    }

}
