package io.github.tanguygab.realvillagertowns;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {

    private static final Random random = new Random();
    private static final boolean is1_8 = Bukkit.getVersion().contains("1.8");

    public static ItemStack getItem(Material material, String name) {
        return getItem(material,name,1,null);
    }

    public static ItemStack getItem(Material material, String name, int amount, List<String> lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta m = item.getItemMeta();
        if (name != null) m.setDisplayName(name);
        if (lore != null) m.setLore(lore);
        item.setItemMeta(m);
        return item;
    }

    public static Entity getTarget(Player player) {
        BlockIterator iterator = new BlockIterator(player.getWorld(), player
                .getLocation().toVector(), player.getEyeLocation()
                .getDirection(), 0.0D, 100);
        while (iterator.hasNext()) {
            Block item = iterator.next();
            for (Entity entity : player.getNearbyEntities(100.0D, 100.0D, 100.0D)) {
                if (entity instanceof LivingEntity && !entity.getType().equals(EntityType.BAT)) {
                    int acc = 2;
                    for (int x = -acc; x < acc; x++) {
                        for (int z = -acc; z < acc; z++) {
                            for (int y = -acc; y < acc; y++) {
                                if (entity.getLocation().getBlock()
                                        .getRelative(x, y, z).equals(item))
                                    return entity;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static int random(int max) {
        return random.nextInt(max);
    }
    public static int random(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static void displayParticle(String effect, Location l, double radius, int speed, int amount) {
        World w = l.getWorld();
        assert w != null;
        if (radius == 0.0D) {
            w.spawnParticle(Particle.valueOf(effect), l, 0, 0.0D, 0.0D, speed, amount);
            return;
        }
        ArrayList<Location> ll = getArea(l, radius, 0.2D);
        for (int i = 0; i < amount; i++) {
            int index = random(ll.size());
            w.spawnParticle(Particle.valueOf(effect), ll.get(index), 0, 0.0D, 0.0D, speed, 1.0D);
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

    public static String getGiftType(ItemStack s) {
        if (s.getItemMeta().getDisplayName().equals("§6Marriage Ring")) return "ring";
        if (s.getType() == Material.BOW || s.getType() == Material.CROSSBOW) return "bow";
        if (s.getType() == Material.POTION || (!is1_8 && (s.getType() == Material.SPLASH_POTION || s.getType() == Material.LINGERING_POTION))) {
            PotionMeta pMeta = (PotionMeta)s.getItemMeta();
            PotionData pd = pMeta.getBasePotionData();
            if (pd.getType().equals(PotionType.WEAKNESS) || pd.getType().equals(PotionType.SLOWNESS) || pd.getType().equals(PotionType.POISON))
                return "high-drunk";
            return "regular-drunk";
        }
        if (s.getType().equals(Material.POTION) && s.getDurability() > 0) return "regular-drunk";
        if (s.getType() == Material.DANDELION || s.getType() == Material.POPPY) return "love";
        return switch (Utils.random(1, 4)) {
            case 1 -> "bad";
            case 2 -> "small";
            case 3 -> "regular";
            default -> "great";
        };
    }

    public static String getListItem(String listName) {
        List<String> list = RealVillagerTowns.getInstance().getConfig().getStringList(listName);
        int index = random.nextInt(list.size());
        return list.get(index).toLowerCase();
    }

    public static String colors(String str) {
        return ChatColor.translateAlternateColorCodes('&',str);
    }
}
