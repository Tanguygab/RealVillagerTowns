package io.github.tanguygab.realvillagertowns;

import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {

    private static final Random random = new Random();

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
        List<Location> ll = getArea(l, radius, 0.2D);
        for (int i = 0; i < amount; i++) {
            int index = random(ll.size());
            w.spawnParticle(particles, ll.get(index), 0, 0.0D, 0.0D, speed, 1.0D);
            ll.remove(index);
        }
    }

    public static List<Location> getArea(Location l, double r, double t) {
        List<Location> ll = new ArrayList<>();
        for (double x = l.getX() - r; x < l.getX() + r; x += t) {
            for (double y = l.getY() - r; y < l.getY() + r; y += t) {
                for (double z = l.getZ() - r; z < l.getZ() + r; z += t)
                    ll.add(new Location(l.getWorld(), x, y, z));
            }
        }
        return ll;
    }

    // This won't work anymore since I split things across 3 different files
    public static String getListItem(String listName) {
        List<String> list = RealVillagerTowns.getInstance().getConfig().getStringList(listName);
        int index = random.nextInt(list.size());
        return list.get(index);
    }

    public static String colors(String str) {
        return ChatColor.translateAlternateColorCodes('&',str);
    }
}
