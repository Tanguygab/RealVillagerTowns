package io.github.tanguygab.realvillagertowns;

import io.github.tanguygab.realvillagertowns.configs.RVTConfig;
import io.github.tanguygab.realvillagertowns.configs.RVTMessages;
import io.github.tanguygab.realvillagertowns.listeners.PlayerInteractListener;
import io.github.tanguygab.realvillagertowns.listeners.PlayerListener;
import io.github.tanguygab.realvillagertowns.listeners.RVTListener;
import io.github.tanguygab.realvillagertowns.listeners.VillagerListener;
import io.github.tanguygab.realvillagertowns.villagers.*;
import io.github.tanguygab.realvillagertowns.villagers.enums.entity.RVTEntityType;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

public final class RealVillagerTowns extends JavaPlugin {

    @Getter private static RealVillagerTowns instance;

    private final File langFile = new File(getDataFolder(),"lang.yml");
    @Getter private FileConfiguration lang;
    @Getter private RVTMessages messages;

    private final File speechesFile = new File(getDataFolder(),"villagers/speeches.yml");
    @Getter private FileConfiguration speeches;
    private final File namesFile = new File(getDataFolder(),"villagers/names.yml");
    @Getter private FileConfiguration names;
    private final File skinsFile = new File(getDataFolder(),"villagers/skins.yml");
    @Getter private FileConfiguration skins;

    private final File dataFile = new File(getDataFolder(), "data.yml");
    public FileConfiguration data;

    @Getter private RVTConfig configuration;

    @Getter private VillagerManager villagerManager;
    @Getter private Interact interact;

    @Override
    public void onEnable() {
        instance = this;
        loadFiles();
        CommandExecutor cmd = new RVTCommand(this);
        Objects.requireNonNull(getCommand("realvillagertowns")).setExecutor(cmd);
        Objects.requireNonNull(getCommand("bringkids")).setExecutor(cmd);

        villagerManager = new VillagerManager(this);

        addRecipes();
        getServer().getScheduler().scheduleSyncRepeatingTask(this,this::villagerAgedLoop,0,120);
        getServer().getScheduler().scheduleSyncRepeatingTask(this,this::villagersFeelingsLoop, 0, configuration.LIKE_TIMER * 20L);
        getServer().getScheduler().scheduleSyncRepeatingTask(this,villagerManager::shootArrows,0,100);
        interact = new Interact(this);

        registerEvents(new RVTListener(this,villagerManager),
                new PlayerListener(villagerManager),
                new VillagerListener(villagerManager),
                new PlayerInteractListener(this,villagerManager));
    }

    private void registerEvents(Listener... listeners) {
        for (Listener listener : listeners)
            getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        villagerManager.unload();
        try {data.save(dataFile);}
        catch (IOException e1) {e1.printStackTrace();}
    }

    public void loadFiles() {
        saveDefaultConfig();
        reloadConfig();
        configuration = new RVTConfig(this);

        if (!langFile.exists()) saveResource("lang.yml", false);
        lang = YamlConfiguration.loadConfiguration(langFile);
        messages = new RVTMessages(Objects.requireNonNull(lang.getConfigurationSection("messages")));

        if (!speechesFile.exists()) saveResource("villagers/speeches.yml", false);
        speeches = YamlConfiguration.loadConfiguration(speechesFile);
        if (!namesFile.exists()) saveResource("villagers/names.yml", false);
        names = YamlConfiguration.loadConfiguration(namesFile);
        if (!skinsFile.exists()) saveResource("villagers/skins.yml", false);
        skins = YamlConfiguration.loadConfiguration(skinsFile);

        if (!dataFile.exists()) {
            try {dataFile.createNewFile();}
            catch (IOException e) {e.printStackTrace();}
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void save() {
        try {data.save(dataFile);}
        catch (IOException e1) {e1.printStackTrace();}
    }

    public void addRecipes() {
        NamespacedKey key = new NamespacedKey(this, "marriage_ring");
        if (getServer().getRecipe(key) != null) return;

        ItemStack ring = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = ring.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§6Marriage Ring");
        meta.setLore(List.of("§8Gift to villager to propose"));
        ring.setItemMeta(meta);

        ShapedRecipe sr = new ShapedRecipe(key, ring);
        sr.shape("AAA", "ABA", "AAA");
        sr.setIngredient('A', Material.GOLD_INGOT);
        getServer().addRecipe(sr);
    }

    public void villagerAgedLoop() {
        villagerManager.getVillagers().values().forEach(villager->{
            villager.updateMood();
            if (villager.isBaby() || villager.getParentType() == RVTEntityType.VILLAGER) return;
            UUID uuid = villager.getParent1();
            RVTPlayer p = villagerManager.getPlayer(uuid);
            if (p != null) {
                p.sendMessage("Your child " + villager.getName() + " has grown up!");
                p.clearBaby();
            } else if (uuid != null) {
                data.set("players." + uuid + ".hasBaby", false);
                data.set("players." + uuid + ".baby", null);
                save();
            }
            villagerManager.disguise(villager);
        });
    }

    public void villagersFeelingsLoop() {
        villagerManager.getVillagers().values().forEach(villager -> {
            if (Utils.random(1, 25) != 1) return;
            villager.setDrunk(villager.getDrunk() <= 0 ? 0 : villager.getDrunk()-1);
        });
        for (Player p : getServer().getOnlinePlayers()) {
            if (Utils.random(1, 10) != 1) continue;
            for (Entity e : p.getNearbyEntities(30, 30, 30)) {
                if (!villagerManager.isVillagerEntity(e)) continue;
                RVTVillager villager = villagerManager.getVillager(e);
                if (villager.getLikes() == p.getUniqueId()) {
                    giveItem(villager, new ItemStack(Material.POPPY));
                    break;
                }
                RVTPlayer player = villagerManager.getPlayer(p);
                if (villager.getGender() != player.getGender()) {
                    like(player, villager);
                    break;
                }
            }
        }
    }

    public void makeBaby(RVTPlayer player, RVTVillager villager) {
        Villager baby = player.getPlayer().getWorld().spawn(player.getPlayer().getLocation(), Villager.class);
        baby.setBaby();
        player.setBaby(baby.getUniqueId());
        if (villager != null) villager.getChildren().add(baby.getUniqueId());
    }

    public void giveItem(RVTVillager villager, ItemStack item) {
        villager.setInHand(item == null ? null : item.getType());
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> villagerManager.disguise(villager), 20L);
    }
    public void like(RVTPlayer player, RVTVillager villager) {
        UUID uuid = player.getUniqueId();
        int likes = player.getLikes().size();
        if (configuration.belowMaxLike(likes) && villager.getPartner() != uuid && villager.getParent1() != uuid) {
            giveItem(villager, new ItemStack(Material.POPPY));
            villager.setLikes(uuid);
            player.getLikes().add(villager.getUniqueId());
        }
        player.setHappiness(villager, 15);
    }

}
