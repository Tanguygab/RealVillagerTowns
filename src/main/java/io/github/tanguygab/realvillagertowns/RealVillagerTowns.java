package io.github.tanguygab.realvillagertowns;

import io.github.tanguygab.realvillagertowns.listeners.PlayerInteractListener;
import io.github.tanguygab.realvillagertowns.listeners.PlayerListener;
import io.github.tanguygab.realvillagertowns.listeners.RVTListener;
import io.github.tanguygab.realvillagertowns.listeners.VillagerListener;
import io.github.tanguygab.realvillagertowns.villagers.*;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public final class RealVillagerTowns extends JavaPlugin {

    @Getter private static RealVillagerTowns instance;

    private final File dataFile = new File(getDataFolder(), "data.yml");
    public YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
    private final File langFile = new File(getDataFolder(), "lang.yml");
    private YamlConfiguration lang = YamlConfiguration.loadConfiguration(langFile);

    @Getter private VillagerManager villagerManager;
    @Getter private Interact interact;

    private final List<ItemStack> aidItemList = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;
        loadFiles();
        CommandExecutor cmd = new RVTCommand(this);
        getCommand("realvillagertowns").setExecutor(cmd);
        getCommand("bringkids").setExecutor(cmd);

        villagerManager = new VillagerManager(this);

        getAidItems();
        addRecipes();
        getServer().getScheduler().scheduleSyncRepeatingTask(this,this::slowTimer,120,120);
        long longTimerTime = getConfig().getInt("likeTimer") * 20L;
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::longTimer, longTimerTime,longTimerTime);
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
    }

    public void loadFiles() {
        saveDefaultConfig();
        reloadConfig();
        if (!dataFile.exists()) {
            try {dataFile.createNewFile();}
            catch (IOException e) {e.printStackTrace();}
        }
        if (!langFile.exists()) saveResource("lang.yml", false);
        lang = YamlConfiguration.loadConfiguration(langFile);
    }

    public void set(String path, Object value) {
        data.set(path,value);
        try {data.save(dataFile);}
        catch (IOException e1) {e1.printStackTrace();}
    }

    private ItemStack getRing() {
        return Utils.getItem(Material.GOLD_NUGGET, "§6Marriage Ring", 1, List.of("§8Gift to villager to propose"));
    }
    public void addRecipes() {
        NamespacedKey key = new NamespacedKey(this, "marriage_ring");
        if (getServer().getRecipe(key) != null) return;
        ItemStack ring = getRing();
        ShapedRecipe sr = new ShapedRecipe(key, ring);
        sr.shape("AAA", "ABA", "AAA");
        sr.setIngredient('A', Material.GOLD_INGOT);
        getServer().addRecipe(sr);
    }

    public void slowTimer() {
        villagerManager.getVillagers().values().forEach(villager->{
            villager.updateMood();
            if (villager.isBaby() || villager.getParentType() == RVTEntityType.VILLAGER) return;
            UUID uuid = villager.getParent1();
            RVTPlayer p = villagerManager.getPlayer(uuid);
            if (p != null) {
                p.sendMessage("Your child " + villager.getName() + " has grown up!");
                p.clearBaby();
            } else {
                set("players." + uuid + ".hasBaby", false);
                set("players." + uuid + ".baby", null);
            }
            villagerManager.disguise(villager);
        });
    }
    public void longTimer() {
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


    public void shootArrows(final LivingEntity v) {
        if (v.isDead()) return;
        int r = getConfig().getInt("shootRadius");
        for (Entity e : v.getNearbyEntities(r, r, r)) {
            if (getConfig().getList("hostileMobs").contains(e.getType().toString())) {
                shootArrow(v, e);
                break;
            }
        }
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> shootArrows(v),  100L);
    }
    public void shootArrow(LivingEntity atc, Entity vic) {
        Location loc1 = vic.getLocation();
        Location loc2 = atc.getLocation();
        loc2.setY(loc2.getY() + 1.0D);
        loc2.setX(loc2.getBlockX() + 0.5D);
        loc2.setZ(loc2.getBlockZ() + 0.5D);
        int arrowSpeed = 1;
        Arrow a = atc.getWorld().spawnArrow(loc2, new Vector(loc1.getX() - loc2.getX(), loc1.getY() - loc2.getY(), loc1.getZ() - loc2.getZ()), arrowSpeed, 12.0F);
        a.setShooter(atc);
        double minAngle = 6.283185307179586D;
        LivingEntity minEntity = null;
        for (Entity entity : atc.getNearbyEntities(64.0D, 64.0D, 64.0D)) {
            if (atc.hasLineOfSight(entity) && entity instanceof LivingEntity v && !entity.isDead()) {
                Vector toTarget = entity.getLocation().toVector().clone().subtract(atc.getLocation().toVector());
                double angle = a.getVelocity().angle(toTarget);
                if (angle < minAngle) {
                    minAngle = angle;
                    minEntity = v;
                }
            }
        }
        if (minEntity != null) new ArrowHomingTask(a,minEntity,this);
    }


    public void getAidItems() {
        for (String mat : getConfig().getConfigurationSection("aidItems").getKeys(false)) {
            Material material = Material.getMaterial(mat);
            if (material == null) {
                getLogger().severe("Invalid aidItem material type \""+mat+"\"! Skipping...");
                continue;
            }
            aidItemList.add(new ItemStack(material, getConfig().getInt("aidItems." + mat)));
        }
    }

    public String getLang(String str) {
        return lang.getString("lang." + str).replace("&", "§");
    }

    public void playerProcreate(RVTPlayer p, RVTPlayer proposer) {
        if (data.getBoolean("players." + p.getUniqueId() + ".hasBaby")) {
            p.sendMessage(getConfig().getString("haveBabyMessage"));
            return;
        }
        String msg = getConfig().getString("babyMessage").replace("<player2>", proposer.getName());
        p.sendMessage(msg);
        proposer.sendMessage(msg);
        makePlayerBaby(p, proposer);
    }

    public void playerMarry(RVTPlayer p, RVTPlayer proposer) {
        if (p.getPartner() != null) {
            p.sendMessage("You are already married!");
            return;
        }
        p.sendMessage("You have married " + proposer.getName());
        proposer.sendMessage("You have married " + p.getName());
        p.marry(proposer.getUniqueId(),RVTEntityType.PLAYER);
        proposer.marry(p.getUniqueId(),RVTEntityType.PLAYER);
    }

    public ItemStack getInfo(RVTPlayer p, RVTVillager v) {
        String name = v.getName();
        int hearts = p.getHappiness(v);
        String trait = v.getTrait();
        String mood = v.getDrunk() > 0 ? "drunk" : getConfig().getString("moods."+v.getMood()+v.getMoodLevel());
        UUID parentId = v.getParent1();
        RVTEntityType parentType = v.getParentType();
        String parent = parentType == RVTEntityType.PLAYER
                ? villagerManager.getPlayer(parentId).getName()
                : villagerManager.getVillager(parentId).getName();

        String partnerId = data.getString("villagers." + v.getUniqueId() + ".partner");
        String partnerType = data.getString("villagers." + v.getUniqueId() + ".married");
        String partner = data.getString(partnerType + "s." + partnerId + ".name");
        String sex = data.getString("villagers." + v.getUniqueId() + ".sex");

        List<String> lore = new ArrayList<>(List.of("§7Name: §8" + name, "§7Hearts: §8" + hearts, "§7Sex: §8" + sex, "§7Trait: §8" + getLang(trait), "§7Mood: §8" + mood));
        if (parent != null) lore.add("§7Child of: §8" + parent);
        if (partner != null) lore.add("§7Married to: §8" + partner);
        return Utils.getItem(Material.LIME_DYE, "§2Info", 1, lore);
    }

    public void divorce(RVTPlayer player) {
        UUID partnerUUID = player.getPartner();
        if (partnerUUID == null) {
            player.sendMessage("§cYou are not married!");
            return;
        }
        player.divorce();
        RVTVillager partner = villagerManager.getVillager(partnerUUID);
        partner.divorce();
        partner.setMood(Mood.SAD,3);
        player.setHappiness(partner,-250);

        player.sendMessage(getConfig().getString("divorceMessage").replace("<villager>", partner.getName()));
        player.sendMessage(partner.getName()+ ": "+Utils.getListItem("speechSpouse.divorce").replace("<player>", player.getName()));
    }

    public void adoptBaby(RVTPlayer p) {
        if (p.isHasBaby()) {
            p.sendMessage(getConfig().getString("haveBabyMessage"));
            return;
        }
        p.sendMessage(getConfig().getString("adoptMessage"));
        makeBaby(p,null);
    }
    public void makeBaby(RVTPlayer player, RVTVillager villager) {
        Villager baby = player.getPlayer().getWorld().spawn(player.getPlayer().getLocation(), Villager.class);
        baby.setBaby();
        player.setBaby(baby.getUniqueId());
        if (villager != null) villager.getChildren().add(baby.getUniqueId());
    }
    public void makePlayerBaby(RVTPlayer player, RVTPlayer player2) {
        Villager baby = player.getPlayer().getWorld().spawn(player.getPlayer().getLocation(), Villager.class);
        baby.setBaby();
        player.setBaby(baby.getUniqueId());
        player2.getChildren().add(baby.getUniqueId());
    }

    public void giveItem(RVTVillager villager, ItemStack item) {
        villager.setInHand(item == null ? null : item.getType());
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> villagerManager.disguise(villager), 20L);
    }
    public void like(RVTPlayer player, RVTVillager villager) {
        UUID uuid = player.getUniqueId();
        int likes = player.getLikes().size();
        if (likes < getConfig().getInt("maxLikes") + 1 && villager.getPartner() != uuid && villager.getParent1() != uuid) {
            giveItem(villager, new ItemStack(Material.POPPY));
            villager.setLikes(uuid);
            player.getLikes().add(villager.getUniqueId());
        }
        player.setHappiness(villager, 15);
    }

    public void marry(RVTPlayer player, RVTVillager villager) {
        int hearts = player.getHappiness(villager);

        if (player.getPartner() != null) {
            player.speech("marry-cant", villager);
            player.setHappiness(villager,Utils.random(-50, -20));
            villager.setMood(Mood.SAD,3);
            return;
        }

        if (hearts >= getConfig().getInt("minHeartsToMarry")) {
            player.speech("marry-yes", villager);
            player.setHappiness(villager, Utils.random(10, 30));
            villager.setMood(Mood.HAPPY,5);

            player.marry(villager.getUniqueId(), RVTEntityType.VILLAGER);
            villager.marry(player.getUniqueId(),RVTEntityType.PLAYER);
            return;
        }
        player.speech("marry-no", villager);
        player.setHappiness(villager,Utils.random(-50, -20));
        villager.setMood(Mood.ANGRY,3);
    }

    public String getText(String type, String family, RVTPlayer player, RVTVillager villager) {
        String parent1 = player.getGender().getParent();
        String parent2 = villager.getGender().getParent();
        String gen = villager.getGender().getChild();

        return villager.getName() + ": " + Utils.colors(Utils.getListItem("speech"+family+"."+type).replace("<player>", player.getName())
                .replace("<parent>", parent1)
                .replace("<parent2>", parent2)
                .replace("<sex>", gen));
    }
    public String getSpeech(String type, RVTPlayer player, RVTVillager villager) {
        String family = player.getChildren().contains(villager.getUniqueId()) && villager.isBaby() ? "Child"
                : player.getPartner() == villager.getUniqueId() ? "Spouse" : "";
        return getText(type, family, player, villager);
        //Message is never null as far as I can see?
        //if (message == null) message = getText(type, "", p, v);
        //if (message == null) message = "Message Error!";
        //return message;
    }


    public void getAid(RVTPlayer p, RVTVillager villager) {
        if (p.getAidCooldown() != null && ChronoUnit.SECONDS.between(p.getAidCooldown(),LocalDateTime.now()) < getConfig().getInt("aidCooldown")) {
            p.speech("aid-no", villager);
            p.setHappiness(villager, -1);
            villager.swingMood(Math.max(Utils.random(-8, 1), 0));
            return;
        }
        p.speech("aid-yes", villager);
        int index = Utils.random(aidItemList.size());
        p.getPlayer().getInventory().addItem(aidItemList.get(index));
        p.setAidCooldown(LocalDateTime.now());
    }

    public void giveGift(RVTPlayer player, RVTVillager villager) {
        PlayerInventory inv = player.getPlayer().getInventory();
        ItemStack gift = inv.getItemInMainHand();
        int amount = gift.getAmount() - 1;
        if (amount <= 0) inv.setItemInMainHand(null);
        else gift.setAmount(amount);

        GiftType type = Utils.getGiftType(gift);
        switch (type) {
            case BOW -> giveItem(villager, new ItemStack(Material.BOW));
            case RING -> marry(player, villager);
            case REGULAR_DRUNK,HIGH_DRUNK -> {
                int i = type == GiftType.HIGH_DRUNK ? 2 : 1;
                if (Utils.random(1, 3) == 1) {
                    villager.setDrunk(villager.getDrunk()+i);
                    player.getPlayer().sendMessage(villager.getName()+": "+Utils.colors(getConfig().getString("drunkMessage")));
                }
            }
            default -> {
                player.speech("gift-" + type, villager);
                switch (type) {
                    case SMALL -> player.setHappiness(villager, 1);
                    case REGULAR -> player.setHappiness(villager, Utils.random(1, 5));
                    case GREAT -> player.setHappiness(villager, Utils.random(5, 15));
                    case LOVE -> {
                        if (villager.getMood() != Mood.HAPPY || villager.getMoodLevel() < 4)
                            villager.setMood(Mood.HAPPY,3);
                        player.setHappiness(villager, Utils.random(2, 6));
                    }
                }
            }
        }
        villager.swingMood(1);
    }

}
