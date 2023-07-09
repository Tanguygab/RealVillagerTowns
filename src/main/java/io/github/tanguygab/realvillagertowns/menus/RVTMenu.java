package io.github.tanguygab.realvillagertowns.menus;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.Utils;
import io.github.tanguygab.realvillagertowns.configs.RVTMessages;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.VillagerManager;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RVTMenu {

    public final static Map<RVTPlayer,RVTMenu> openedMenus = new HashMap<>();

    protected final RVTPlayer player;

    protected final RealVillagerTowns rvt = RealVillagerTowns.getInstance();
    protected final VillagerManager vm = rvt.getVillagerManager();
    protected final RVTMessages msgs = rvt.getMessages();
    public Inventory inv;

    public RVTMenu(RVTPlayer player, String title, int rows) {
        this.player = player;
        inv = rvt.getServer().createInventory(null,rows*9,colors(title));
        openedMenus.put(player,this);
    }
    public RVTMenu(RVTPlayer player, String title, InventoryType invType) {
        this.player = player;
        inv = rvt.getServer().createInventory(null,invType,title);
        openedMenus.put(player,this);
    }

    public abstract void onOpen();
    public abstract void onClick(ItemStack item, int slot);
    public void onClose() {
        openedMenus.remove(player);
        player.getPlayer().closeInventory();
    }

    protected void open() {
        player.getPlayer().openInventory(inv);
    }

    public static ItemStack createMenuItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(colors("&f"+name));
        if (lore.length != 0) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) loreList.add(colors("&7"+line));
            meta.setLore(loreList);
        }
        item.setItemMeta(meta);
        return item;
    }

    protected static String colors(String str) {
        return Utils.colors(str);
    }

}