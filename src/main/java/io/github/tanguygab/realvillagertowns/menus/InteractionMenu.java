package io.github.tanguygab.realvillagertowns.menus;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class InteractionMenu extends RVTMenu {

    private final List<String> interactions;
    private static final List<String> interactionsNames = List.of("gift","chat","joke","greet","insult","story");
    private static final List<String> interactionsClick = List.of("chat", "joke", "greet", "kiss", "story", "flirt", "insult", "play", "follow", "stopFollow", "stay", "move");
    private final RVTVillager villager;

    public InteractionMenu(RVTPlayer player, RVTVillager villager) {
        super(player, "§0§lInteract with " + RealVillagerTowns.getInstance().data.getString("villagers." + villager.getUniqueId() + ".name"), 2);
        interactions = rvt.getConfig().getStringList("interactions");
        this.villager = villager;
    }

    @Override
    public void onOpen() {
        for (String interaction : interactionsNames) addItem(interaction);
        if (!player.isChild(villager)) {
            addItem("flirt");
            if (player.likes(villager) && (player.getHappiness(villager) > 50 || villager.getDrunk() >= 1))
                addItem("kiss");
        }
        if (villager.isBaby()) addItem("play");
        if (player.likes(villager) && (player.getHappiness(villager) > 200 || player.isMarried(villager) || villager.getDrunk() >= 3))
            addItem("procreate");
        if (player.getHappiness(villager) > 100 || player.isChild(villager) || player.isBaby(villager) || player.isMarried(villager)) {
            if (interactions.contains("follow")) {
                String lang = villager.getFollowed() == player ? "stopFollow" : "follow";
                inv.addItem(createMenuItem(Material.SLIME_BALL, getLang(lang)));
            }
            if (interactions.contains("stay"))
                inv.addItem(createMenuItem(Material.SLIME_BALL, getLang(villager.isStaying() ? "move" : "stay")));
        }
        open();
    }

    private void addItem(String name) {
        if (interactions.contains(name))
            inv.addItem(createMenuItem(Material.SLIME_BALL, getLang(name)));
    }

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return true;
        String currentItem = meta.getDisplayName();

        if (currentItem.equals(getLang("procreate"))) {
            vm.procreate(player, villager);
            onClose();
            return true;
        }
        if (currentItem.equals(getLang("gift"))) {
            player.sendMessage("Right-click on a villager to give them the item in your hand.");
            player.setGifting(villager);
            onClose();
            return true;
        }
        for (String interaction : interactionsClick)
            if (currentItem.equals(getLang(interaction))) {
                rvt.getInteract().interact(player, villager, interaction);
                onClose();
                break;
            }
        return true;
    }
}
