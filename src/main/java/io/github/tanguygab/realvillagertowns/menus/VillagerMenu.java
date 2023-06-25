package io.github.tanguygab.realvillagertowns.menus;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class VillagerMenu extends RVTMenu {

    private final LivingEntity entity;
    private final List<String> buttons;

    public VillagerMenu(Player player, LivingEntity entity) {
        super(player, "", 1);
        this.entity = entity;
        buttons = rvt.getConfig().getStringList("buttons");

        String name = rvt.data.getString("villagers." + entity.getUniqueId() + ".name");
        String title = rvt.data.getString("villagers." + entity.getUniqueId() + ".title");
        int length = title.length()+4;
        if (length > 32) title = title.substring(0,28);
        if (length+name.length() > 32) name = name.substring(0,32-length);

        inv = rvt.getServer().createInventory(player, 9, "§0§l" + name + title);
    }

    @Override
    public void onOpen() {
        addItem("interact");

        ItemStack i = rvt.getInfo(player, entity);
        inv.setItem(8, i);

        if (!(entity instanceof Villager villager)) {
            player.openInventory(inv);
            return;
        }
        if (villager.getProfession() != Villager.Profession.NITWIT) addItem("trade");
        addItem("setHome");
        if (!rvt.isMarried(player, villager) && !rvt.isChild(player, villager)) addItem("requestAid");
        if (!rvt.isBaby(villager) && villager.getProfession() == Villager.Profession.CLERIC) {
            addItem("divorce");
            addItem("adopt");
        }
        player.openInventory(inv);
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

        if (is(currentItem,"interact")) new InteractionMenu(player,entity).onOpen();
        else if (is(currentItem,"trade")) rvt.trade(player);
        else if (is(currentItem,"requestAid")) rvt.getAid(player, entity);
        else if (is(currentItem,"setHome")) rvt.setHome(player, entity);
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
