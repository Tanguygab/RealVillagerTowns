package io.github.tanguygab.realvillagertowns.menus;

import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class VillagerMenu extends RVTMenu {

    private final RVTVillager villager;
    private final List<String> buttons;

    public VillagerMenu(RVTPlayer player, RVTVillager villager) {
        super(player, "", 1);
        this.villager = villager;
        buttons = rvt.getConfig().getStringList("buttons");

        String name = villager.getName();
        String title = villager.getTitle();
        int length = title.length()+4;
        if (length > 32) title = title.substring(0,28);
        if (length+name.length() > 32) name = name.substring(0,32-length);

        inv = rvt.getServer().createInventory(null, 9, "§0§l" + name + title);
    }

    @Override
    public void onOpen() {
        addItem("interact");

        ItemStack i = rvt.getInfo(player, villager);
        inv.setItem(8, i);

        if (!(villager.getEntity() instanceof Villager v)) {
            open();
            return;
        }
        if (v.getProfession() != Villager.Profession.NITWIT) addItem("trade");
        addItem("setHome");
        if (!player.isMarried(villager) && !player.isChild(villager)) addItem("requestAid");
        if (!player.isBaby(villager) && v.getProfession() == Villager.Profession.CLERIC) {
            addItem("divorce");
            addItem("adopt");
        }
        open();
    }

    private void addItem(String action) {
        if (buttons.contains(action))
            inv.addItem(createMenuItem(Material.SLIME_BALL,getLang(action)));
    }

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return true;
        String currentItem = meta.getDisplayName();

        if (is(currentItem,"interact")) new InteractionMenu(player,villager).onOpen();
        else if (is(currentItem,"trade")) {
            player.setTrading(true);
            player.sendMessage("Click player again to trade.");
        }
        else if (is(currentItem,"requestAid")) rvt.getAid(player,villager);
        else if (is(currentItem,"setHome")) {
            villager.setHome();
            player.sendMessage(villager.getName() + "'s home set!");
        }
        else if (is(currentItem,"adopt")) rvt.adoptBaby(player);
        else if (is(currentItem,"divorce")) rvt.divorce(player);
        else return true;
        onClose();
        return true;
    }

    private boolean is(String currentItem, String action) {
        return currentItem.equals(getLang(action));
    }
}
