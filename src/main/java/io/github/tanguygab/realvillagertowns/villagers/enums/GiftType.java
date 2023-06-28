package io.github.tanguygab.realvillagertowns.villagers.enums;

import io.github.tanguygab.realvillagertowns.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public enum GiftType {

    RING,
    BOW,
    HIGH_DRUNK,
    REGULAR_DRUNK,
    LOVE,
    BAD,
    SMALL,
    REGULAR,
    GREAT;

    private static final boolean is1_8 = Bukkit.getVersion().contains("1.8");

    public static GiftType fromItem(ItemStack item) {
        Material type = item.getType();
        if (item.getItemMeta() != null && item.getItemMeta().getDisplayName().equals("§6Marriage Ring")) return RING;
        if (type == Material.BOW || type == Material.CROSSBOW) return BOW;
        if (type == Material.POTION || type == Material.SPLASH_POTION || (!is1_8 && type == Material.LINGERING_POTION)) {
            PotionType potion = ((PotionMeta) item.getItemMeta()).getBasePotionData().getType();
            return potion == PotionType.WEAKNESS || potion == PotionType.SLOWNESS || potion == PotionType.POISON ? HIGH_DRUNK : REGULAR_DRUNK;
        }
        if (type == Material.DANDELION || type == Material.POPPY) return LOVE;
        return switch (Utils.random(1, 4)) {
            case 1 -> BAD;
            case 2 -> SMALL;
            case 3 -> REGULAR;
            default -> GREAT;
        };
    }

}
