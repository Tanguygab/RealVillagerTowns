package io.github.tanguygab.realvillagertowns.configs;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class RVTConfig {

    public final boolean USE_NAMES;
    public final boolean USE_VILLAGER_INTERACTIONS;
    public final boolean AUTO_CHANGE_VILLAGERS;

    private final List<EntityType> VILLAGERS_TYPES = new ArrayList<>();

    private final boolean SPAWN_RANDOM_VILLAGERS;
    private final int RANDOM_VILLAGER_CHANCE;

    public final int VILLAGER_HAPPINESS_LEVEL;

    public final int LIKE_TIMER;
    private final int BABY_CHANCE;
    private final int MAX_LIKES;
    public final int MIN_HEARTS_TO_MARRY;
    public final boolean ENABLE_PLAYER_MARRIAGE;
    private final int MAX_SAME_INTERACTION;

    private final int AID_COOLDOWN;
    private final List<ItemStack> AID_ITEMS = new ArrayList<>();

    public final int SHOOT_RADIUS;
    private final List<EntityType> HOSTILE_MOBS = new ArrayList<>();

    public RVTConfig(RealVillagerTowns rvt) {
        rvt.saveDefaultConfig();
        rvt.reloadConfig();
        FileConfiguration cfg = rvt.getConfig();

        USE_NAMES = cfg.getBoolean("use-names",true);
        USE_VILLAGER_INTERACTIONS = cfg.getBoolean("use-villager-interactions",true);
        AUTO_CHANGE_VILLAGERS = cfg.getBoolean("auto-change-villagers",true);

        List<String> entityTypes = cfg.getStringList("villagers-types");
        entityTypes.forEach(str->{
            try {
                VILLAGERS_TYPES.add(EntityType.valueOf(str));
            } catch (Exception e) {
                rvt.getLogger().severe("Invalid Villager entity type \""+str+"\"! Skipping...");
            }
        });

        SPAWN_RANDOM_VILLAGERS = cfg.getBoolean("spawn-random-villagers",false);
        RANDOM_VILLAGER_CHANCE = cfg.getInt("random-villager-chance",256);

        VILLAGER_HAPPINESS_LEVEL = cfg.getInt("villager-happiness-level",6);
        LIKE_TIMER = cfg.getInt("like-timer",30);
        BABY_CHANCE = cfg.getInt("baby-chance",6);
        MAX_LIKES = cfg.getInt("max-likes",3);
        MIN_HEARTS_TO_MARRY = cfg.getInt("min-hearts-to-marry",100);
        ENABLE_PLAYER_MARRIAGE = cfg.getBoolean("enable-player-marriage",true);
        MAX_SAME_INTERACTION = cfg.getInt("max-same-interaction",5);

        AID_COOLDOWN = cfg.getInt("like-timer",30);
        ConfigurationSection items = cfg.getConfigurationSection("aidItems");
        if (items != null)
            items.getKeys(false).forEach(mat->{
                Material material = Material.getMaterial(mat);
                if (material == null) {
                    rvt.getLogger().severe("Invalid aidItem material type \""+mat+"\"! Skipping...");
                    return;
                }
                AID_ITEMS.add(new ItemStack(material, items.getInt(mat)));
            });

        SHOOT_RADIUS = cfg.getInt("shoot-radius",15);
        List<String> hostileMobs = cfg.getStringList("hostile-mobs");
        hostileMobs.forEach(str->{
            try {
                HOSTILE_MOBS.add(EntityType.valueOf(str));
            } catch (Exception e) {
                rvt.getLogger().severe("Invalid Hostile Mob entity type \""+str+"\"! Skipping...");
            }
        });

    }

    public boolean isVillagerType(Entity entity) {
        return VILLAGERS_TYPES.contains(entity.getType());
    }

    public boolean isHostileMob(Entity entity) {
        return HOSTILE_MOBS.contains(entity.getType());
    }

    public ItemStack getAidItem() {
        return AID_ITEMS.get(Utils.random(AID_ITEMS.size()));
    }

    public boolean belowMaxLike(int likes) {
        return likes < MAX_LIKES+1;
    }

    public boolean enoughHeartsToMarry(int hearts) {
        return hearts >= MIN_HEARTS_TO_MARRY;
    }

    public boolean isOnCooldown(LocalDateTime cooldown) {
        return cooldown != null && ChronoUnit.SECONDS.between(cooldown,LocalDateTime.now()) < AID_COOLDOWN;
    }

    public boolean canSpawnRandomVillager() {
        return SPAWN_RANDOM_VILLAGERS && Utils.random(RANDOM_VILLAGER_CHANCE) + 1 == 1;
    }

    public boolean canUpdateLastInteraction(int times) {
        return times >= MAX_SAME_INTERACTION;
    }

    public boolean getBabyChance() {
        return Utils.random(1, BABY_CHANCE ) != 1;
    }
}
