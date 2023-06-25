package io.github.tanguygab.realvillagertowns.menus;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class InteractionMenu extends RVTMenu {

    private final List<String> interactions;
    private final List<String> interactionsNames = List.of("gift","chat","joke","greet","insult","story");
    private final LivingEntity entity;

    public InteractionMenu(Player player, LivingEntity entity) {
        super(player, "§0§lInteract with " + RealVillagerTowns.getInstance().data.getString("villagers." + entity.getUniqueId() + ".name"), 2);
        interactions = rvt.getConfig().getStringList("interactions");
        this.entity = entity;
    }

    @Override
    public void onOpen() {
        for (String interaction : interactionsNames) addItem(interaction);

        if (!rvt.isChild(player, entity)) {
            addItem("flirt");
            if (rvt.likes(player, entity) && (rvt.data.getInt("players." + player.getUniqueId() + ".happiness." + entity.getUniqueId()) > 50
                    || (rvt.drunkMap.get(entity.getUniqueId()) != null
                    && rvt.drunkMap.get(entity.getUniqueId()) >= 1)))
                addItem("kiss");
        }
        if (rvt.isBaby(entity)) addItem("play");
        if (rvt.likes(player, entity) && (
                rvt.data.getInt("players." + player.getUniqueId() + ".happiness." + entity.getUniqueId()) > 200
                        || rvt.isMarried(player, entity)
                        || (rvt.drunkMap.get(entity.getUniqueId()) != null
                        && rvt.drunkMap.get(entity.getUniqueId()) >= 3)))
            addItem("procreate");

        if (rvt.data.getInt("players." + player.getUniqueId() + ".happiness." + entity.getUniqueId()) > 100
                || rvt.isChild(player, entity) || rvt.isBaby(entity) || rvt.isMarried(player, entity)) {

            if (interactions.contains("follow")) {
                String lang = rvt.followMap.containsKey(entity) && rvt.followMap.get(entity).equals(player) ? "stopFollow" : "follow";
                inv.addItem(createMenuItem(Material.SLIME_BALL, getLang(lang)));
            }
            if (interactions.contains("stay"))
                inv.addItem(createMenuItem(Material.SLIME_BALL, getLang(rvt.isStaying(entity) ? "move" : "stay")));
        }
        player.openInventory(inv);
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
            rvt.procreate(player, entity);
            onClose();
            return true;
        }
        if (currentItem.equals(getLang("gift"))) {
            player.sendMessage("Right-click on a villager to give them the item in your hand.");
            rvt.giftMap.put(player.getName(), entity.getUniqueId());
            onClose();
            return true;
        }
        for (String interaction : List.of("chat", "joke", "greet", "kiss", "story", "flirt", "insult", "play", "follow", "stopFollow", "stay", "move"))
            if (currentItem.equals(getLang(interaction))) {
                rvt.getInteract().interact(player, entity, interaction);
                onClose();
                break;
            }
        return true;
    }
}
