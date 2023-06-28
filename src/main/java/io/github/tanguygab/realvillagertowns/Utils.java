package io.github.tanguygab.realvillagertowns;

import org.bukkit.*;

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

    public static String colors(String str) {
        return ChatColor.translateAlternateColorCodes('&',str);
    }

    // This won't work anymore since I split things across 3 different files
    public static String getListItem(String listName) {
        List<String> list = RealVillagerTowns.getInstance().getConfig().getStringList(listName);
        int index = random.nextInt(list.size());
        return list.get(index);
    }

    public static void displayParticle(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        double r = 0.3D, t = 0.2D;
        List<Location> locations = new ArrayList<>();
        for (double x = loc.getX() - r; x < loc.getX() + r; x += t)
            for (double y = loc.getY() - r; y < loc.getY() + r; y += t)
                for (double z = loc.getZ() - r; z < loc.getZ() + r; z += t)
                    locations.add(new Location(loc.getWorld(), x, y, z));

        for (int i = 0; i < 3; i++) {
            int index = random(locations.size());
            world.spawnParticle(Particle.HEART, locations.get(index), 0, 0.0D, 0.0D, 1, 1.0D);
            locations.remove(index);
        }
    }

}
