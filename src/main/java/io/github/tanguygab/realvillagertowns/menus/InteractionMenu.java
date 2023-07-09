package io.github.tanguygab.realvillagertowns.menus;

import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;
import io.github.tanguygab.realvillagertowns.villagers.enums.Interaction;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InteractionMenu extends RVTMenu {
    private final RVTVillager villager;

    public InteractionMenu(RVTPlayer player, RVTVillager villager) {
        super(player, "§0§lInteract with " + villager.getName(), 2);
        this.villager = villager;
    }

    @Override
    public void onOpen() {
        for (Interaction interaction : Interaction.values()) {
            if (interaction.meetsCondition(player,villager))
                inv.addItem(createMenuItem(Material.SLIME_BALL, interaction.getLang()));
        }
        open();
    }

    @Override
    public void onClick(ItemStack item, int slot) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String currentItem = meta.getDisplayName();

        if (Interaction.PROCREATE.getLang().equals(currentItem)) {
            vm.procreate(player, villager);
            onClose();
            return;
        }
        if (Interaction.GIFT.getLang().equals(currentItem)) {
            player.sendMessage("Right-click on a villager to give them the item in your hand.");
            player.setGifting(villager);
            onClose();
            return;
        }
        for (Interaction interaction : Interaction.values()) {
            if (interaction.getLang().equals(currentItem)) {
                rvt.getInteract().interact(player, villager, interaction);
                onClose();
                break;
            }
        }
    }
}
