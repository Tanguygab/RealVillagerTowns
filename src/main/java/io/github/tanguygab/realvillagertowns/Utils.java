package io.github.tanguygab.realvillagertowns;

import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {

    private static final Random random = new Random();

    public static ItemStack getItem(Material material, String name, int amount, List<String> lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta m = item.getItemMeta();
        assert m != null;
        if (name != null) m.setDisplayName(name);
        if (lore != null) m.setLore(lore);
        item.setItemMeta(m);
        return item;
    }

    public static int random(int max) {
        return random.nextInt(max);
    }
    public static int random(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static void displayParticle(Particle particles, Location l, double radius, int speed, int amount) {
        World w = l.getWorld();
        assert w != null;
        if (radius == 0.0D) {
            w.spawnParticle(particles, l, 0, 0.0D, 0.0D, speed, amount);
            return;
        }
        ArrayList<Location> ll = getArea(l, radius, 0.2D);
        for (int i = 0; i < amount; i++) {
            int index = random(ll.size());
            w.spawnParticle(particles, ll.get(index), 0, 0.0D, 0.0D, speed, 1.0D);
            ll.remove(index);
        }
    }

    public static ArrayList<Location> getArea(Location l, double r, double t) {
        ArrayList<Location> ll = new ArrayList<>();
        for (double x = l.getX() - r; x < l.getX() + r; x += t) {
            for (double y = l.getY() - r; y < l.getY() + r; y += t) {
                for (double z = l.getZ() - r; z < l.getZ() + r; z += t)
                    ll.add(new Location(l.getWorld(), x, y, z));
            }
        }
        return ll;
    }

    public static String getListItem(String listName) {
        List<String> list = RealVillagerTowns.getInstance().getConfig().getStringList(listName);
        int index = random.nextInt(list.size());
        return list.get(index);
    }

    public static String colors(String str) {
        return ChatColor.translateAlternateColorCodes('&',str);
    }
}
