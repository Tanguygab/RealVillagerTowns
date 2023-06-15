package io.github.tanguygab.realvillagertowns;

import lombok.Getter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public final class RealVillagerTowns extends JavaPlugin implements Listener {

    @Getter private MenuUtils menuUtils;

    private final File saveYML = new File(getDataFolder(), "saves.yml");
    public YamlConfiguration saveFile = YamlConfiguration.loadConfiguration(saveYML);

    private final File langYML = new File(getDataFolder(), "lang.yml");
    private YamlConfiguration langFile = YamlConfiguration.loadConfiguration(langYML);

    Map<String, Integer> aidMap = new HashMap<>();
    Map<String, UUID> giftMap = new HashMap<>();
    Map<String, String> moodMap = new HashMap<>();
    Map<String, String> playerChildMap = new HashMap<>();
    Map<String, Integer> drunkMap = new HashMap<>();
    List<ItemStack> aidItemList = new ArrayList<>();
    List<String> tradeList = new ArrayList<>();
    Map<String, List<UUID>> likeMap = new HashMap<>();
    List<UUID> bowList = new ArrayList<>();
    Map<LivingEntity, Player> followMap = new HashMap<>();
    List<String> interactList = new ArrayList<>();
    Map<String, String> sameAction = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        if (!(new File(getDataFolder(), "config.yml")).exists())
            saveDefaultConfig();
        if (!saveYML.exists())
            try {
                saveYML.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (!langYML.exists()) {
            getLogger().log(Level.INFO, "No lang.yml found, generating...");
            isSave(getClass().getClassLoader().getResourceAsStream("lang.yml"), "lang.yml");
            getLogger().log(Level.INFO, "Lang successfully generated!");
        }
        reloadLang();
        menuUtils = new MenuUtils(this);
        CommandExecutor cmd = new RVTCommand(this);
        getCommand("realvillagertowns").setExecutor(cmd);
        getCommand("bringkids").setExecutor(cmd);

        makeVillagers();
        getAidItems();
        addRecipes();
        fastTimer();
        slowTimer();
        longTimer();
    }

    public void set(String path, Object value) {
        saveFile.set(path,value);
        try {saveFile.save(saveYML);}
        catch (IOException e1) {e1.printStackTrace();}
    }

    private ItemStack getRing() {
        return Utils.getItem(Material.GOLD_NUGGET, "§6Marriage Ring", 1, List.of("§8Gift to villager to propose"));
    }
    public void addRecipes() {
        ItemStack ring = getRing();
        NamespacedKey key = new NamespacedKey(this, "marriage_ring");
        ShapedRecipe sr = new ShapedRecipe(key, ring);
        sr.shape("AAA", "ABA", "AAA");
        sr.setIngredient('A', Material.GOLD_INGOT);
        Bukkit.addRecipe(sr);
    }
    public void reloadLang() {
        langFile = YamlConfiguration.loadConfiguration(langYML);
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(langYML);
        langFile.setDefaults(defConfig);
    }
    public void isSave(InputStream inputStream, String dFile) {
        OutputStream outputStream = null;
        try {
            File plugins = getDataFolder().getParentFile();
            outputStream = new FileOutputStream(plugins.getPath() + File.separator + getName() + File.separator + dFile);
            int read;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1)
                outputStream.write(bytes, 0, read);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public void fastTimer() {
        for (Map.Entry<String, Integer> hm : new HashSet<>(aidMap.entrySet())) {
            if (hm.getValue() <= 0) {
                aidMap.remove(hm.getKey());
                continue;
            }
            aidMap.put(hm.getKey(), hm.getValue() - 1);
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, this::fastTimer,  20L);
    }
    public void slowTimer() {
        for (Map.Entry<String, String> hm : new HashSet<>(moodMap.entrySet())) {
            if (Utils.random(1, 40) == 1) {
                moodMap.remove(hm.getKey());
                getMood(hm.getKey());
            }
        }
        HashMap<String, LivingEntity> vMap = new HashMap<>();
        for (World w : getServer().getWorlds()) {
            for (Entity e : w.getEntities())
                if (entEnabled(e) && playerChildMap.containsKey(e.getUniqueId().toString()))
                    vMap.put(e.getUniqueId().toString(), (LivingEntity)e);
        }
        for (Map.Entry<String, String> hm : new HashSet<>(playerChildMap.entrySet())) {
            String id = hm.getKey();
            LivingEntity v = vMap.get(id);
            if (v != null)
                if (!isBaby(v)) {
                    String pid = hm.getValue();
                    String n = saveFile.getString("villagers." + id + ".name");
                    Player p = getServer().getPlayer(UUID.fromString(pid));
                    if (p != null) p.sendMessage("Your child " + n + " has grown up!");
                    saveFile.set("players." + pid + ".hasBaby", false);
                    saveFile.set("players." + pid + ".baby", null);
                    try {saveFile.save(saveYML);}
                    catch (IOException e) {e.printStackTrace();}
                    playerChildMap.remove(id);
                    disguise(v);
                }
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, this::slowTimer,  120L);
    }
    public void longTimer() {
        for (Map.Entry<String, Integer> hm : new HashSet<>(drunkMap.entrySet())) {
            if (Utils.random(1, 25) == 1) {
                if (hm.getValue().intValue() <= 0) {
                    drunkMap.remove(hm.getKey());
                    continue;
                }
                drunkMap.put(hm.getKey(), Integer.valueOf(hm.getValue().intValue() - 1));
            }
        }
        for (Player p : getServer().getOnlinePlayers()) {
            int r = Utils.random(1, 10);
            if (r != 1) continue;
            for (Entity e : p.getNearbyEntities(30.0D, 30.0D, 30.0D)) {
                if (!entEnabled(e)) continue;
                LivingEntity v = (LivingEntity)e;
                if (saveFile.getString("villagers." + v.getUniqueId() + ".likes") != null
                        && saveFile.getString("villagers." + v.getUniqueId() + ".likes").equals(p.getUniqueId().toString())) {
                    giveItem(v, new ItemStack(Material.POPPY));
                    break;
                }
                String vSex = saveFile.getString("villagers." + v.getUniqueId() + ".sex");
                String pSex = saveFile.getString("players." + p.getUniqueId() + ".sex");
                assert pSex != null;
                if (!pSex.equals(vSex)) {
                    like(p, v);
                    break;
                }
            }
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, this::longTimer, getConfig().getInt("likeTimer") * 20L);
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
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> shootArrows(v),  100L);
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

    public void loveJump(final Entity e, int times) {
        for (int i = 0; i <= times; i++)
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> jump(e), i * 16L);
    }
    public void jump(Entity e) {
        Location l = e.getLocation();
        l.setY(l.getY() + 1.5D);
        Utils.displayParticle("HEARTS", l, 0.3D, 1, 3);
        Vector v = new Vector();
        v.setY(0.3D);
        e.setVelocity(v);
    }


    public boolean entEnabled(Entity e) {
        return getConfig().getList("villagerMobs").contains(e.getType().toString());
    }

    public void makeVillagers() {
        for (World w : getServer().getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (!entEnabled(e)) continue;
                if (!isVillager(e)) makeVillager((LivingEntity)e);
                else disguise((LivingEntity)e);
            }
        }
    }

    public void getAidItems() {
        for (String mat : getConfig().getConfigurationSection("aidItems").getKeys(false))
            aidItemList.add(Utils.getItem(Material.getMaterial(mat), null, getConfig().getInt("aidItems." + mat),null));
    }

    public void stopFollow(LivingEntity v) {
        disguise(v);
        v.removePotionEffect(PotionEffectType.INVISIBILITY);
        followMap.remove(v);
    }

    public String getLang(String str) {
        return langFile.getString("lang." + str).replace("&", "§");
    }

    public boolean likes(Player p, LivingEntity v) {
        return saveFile.getList("players." + p.getUniqueId() + ".likes").contains(v.getUniqueId());
    }

    public void setLikes(Player p, LivingEntity v) {
        List<String> likes = saveFile.getStringList("players." + p.getUniqueId() + ".likes");
        if (!likes.contains(v.getUniqueId().toString())) likes.add(v.getUniqueId().toString());
        saveFile.set("players." + p.getUniqueId() + ".likes", likes);
        try {
            saveFile.save(saveYML);
        } catch (IOException e) {e.printStackTrace();}
    }
    public boolean isStaying(LivingEntity v) {
        for (PotionEffect e : v.getActivePotionEffects())
            if (e.getType().equals(PotionEffectType.SLOW))
                return true;
        return false;
    }

    public void playerProcreate(Player p) {
        if (saveFile.getBoolean("players." + p.getUniqueId() + ".hasBaby")) {
            p.sendMessage(getConfig().getString("haveBabyMessage").replace("&", "§"));
            return;
        }
        Player p2 = getServer().getPlayer(getMenuUtils().getPlayerMenuMap().get(p.getName()));
        String msg = getConfig().getString("babyMessage").replace("<player2>", p2.getName()).replace("&", "§");
        p.sendMessage(msg);
        p2.sendMessage(msg);
        makePlayerBaby(p, p2);
        getMenuUtils().getPlayerMenuMap().remove(p.getName());
    }

    public void playerMarry(Player p) {
        if (saveFile.getString("players." + p.getUniqueId() + ".partner") != null) {
            p.sendMessage("You are already married!");
            return;
        }
        Player p2 = getServer().getPlayer(getMenuUtils().getPlayerMenuMap().get(p.getName()));
        p.sendMessage("You have married " + p2.getName());
        p2.sendMessage("You have married " + p.getName());
        saveFile.set("players." + p.getUniqueId() + ".married", "player");
        saveFile.set("players." + p.getUniqueId() + ".partner", p2.getUniqueId().toString());
        saveFile.set("players." + p2.getUniqueId() + ".married", "player");
        saveFile.set("players." + p2.getUniqueId() + ".partner", p.getUniqueId().toString());
        try {
            saveFile.save(saveYML);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        getMenuUtils().getPlayerMenuMap().remove(p.getName());
    }

    public ItemStack getInfo(Player p, LivingEntity v) {
        String name = saveFile.getString("villagers." + v.getUniqueId() + ".name");
        int hearts = 0;
        if (saveFile.getString("players." + p.getUniqueId() + ".happiness." + v.getUniqueId()) != null)
            hearts = saveFile.getInt("players." + p.getUniqueId() + ".happiness." + v.getUniqueId());
        String trait = saveFile.getString("villagers." + v.getUniqueId() + ".trait");
        String mood = getConfig().getString("moods." + getMood(v.getUniqueId().toString()));
        if (drunkMap.containsKey(v.getUniqueId().toString()))
            mood = "drunk";
        String parentId = saveFile.getString("villagers." + v.getUniqueId() + ".parent");
        String parentType = saveFile.getString("villagers." + v.getUniqueId() + ".parentType");
        String parent = saveFile.getString(parentType + "s." + parentId + ".name");

        String partnerId = saveFile.getString("villagers." + v.getUniqueId() + ".partner");
        String partnerType = saveFile.getString("villagers." + v.getUniqueId() + ".married");
        String partner = saveFile.getString(partnerType + "s." + partnerId + ".name");
        String sex = saveFile.getString("villagers." + v.getUniqueId() + ".sex");

        ArrayList<String> lore = new ArrayList<>(List.of("§7Name: §8" + name, "§7Hearts: §8" + hearts, "§7Sex: §8" + sex, "§7Trait: §8" + getLang(trait), "§7Mood: §8" + mood));
        if (parent != null) lore.add("§7Child of: §8" + parent);
        if (partner != null) lore.add("§7Married to: §8" + partner);
        return Utils.getItem(Material.LIME_DYE, "§2Info", 1, lore);
    }

    public String getMood(String id) {
        if (moodMap.get(id) == null) {
            int r = Utils.random(1, 5);
            String mood = switch (r) {
                case 1 -> "happy1";
                case 2 -> "neutral1";
                case 3 -> "sadness1";
                case 4 -> "anger1";
                default -> "fatigue1";
            };
            moodMap.put(id, mood);
        }
        return moodMap.get(id);
    }

    //if anyone knows what this method does, please do tell
    public void moodSwing(LivingEntity v, int amount) {
        final String mood = moodMap.get(v.getUniqueId().toString());
        if (mood.contains("sadness") || mood.contains("anger") || mood.contains("fatigue")) {
            amount *= -1;
        }
        if (mood.contains("1")) {
            mood.replace("1", "");
            ++amount;
        }
        else if (mood.contains("2")) {
            mood.replace("2", "");
            amount += 2;
        }
        else if (mood.contains("3")) {
            mood.replace("3", "");
            amount += 3;
        }
        else if (mood.contains("4")) {
            mood.replace("4", "");
            amount += 4;
        }
        else if (mood.contains("5")) {
            mood.replace("5", "5");
            ++amount;
        }
    }

    public boolean isChild(Player p, LivingEntity v) {
        return saveFile.getList("players."+p.getUniqueId()+".children") != null && saveFile.getList("players."+p.getUniqueId()+".children").contains(v.getUniqueId().toString());
    }

    public boolean isMarried(Player p, LivingEntity v) {
        return v.getUniqueId().toString().equals(saveFile.getString("players." + p.getUniqueId() + ".partner"));
    }

    public void divorce(Player p) {
        try {
            String sid = saveFile.getString("players." + p.getUniqueId() + ".partner");
            saveFile.set("players." + p.getUniqueId() + ".married", null);
            saveFile.set("players." + p.getUniqueId() + ".partner", null);
            saveFile.set("villagers." + sid + ".married", null);
            saveFile.set("villagers." + sid + ".partner", null);
            changeHearts(p, UUID.fromString(sid), -250);
            String vName = saveFile.getString("villagers." + sid + ".name");
            String dMsg = getConfig().getString("divorceMessage");
            dMsg = dMsg.replace("&", "§").replace("<villager>", vName);
            p.sendMessage(dMsg);
            p.sendMessage(getPathSpeech("speechSpouse.divorce", p, sid));
            moodMap.put(sid, "sadness3");
            saveFile.save(saveYML);
        } catch (Exception e) {
            p.sendMessage("§cYou are not married!");
        }
    }

    public void trade(Player p) {
        tradeList.add(p.getName());
        p.sendMessage("Click player again to trade.");
    }

    public void adoptBaby(Player p) {
        if (saveFile.getBoolean("players." + p.getUniqueId() + ".hasBaby")) {
            p.sendMessage(getConfig().getString("haveBabyMessage").replace("&", "§"));
            return;
        }
        p.sendMessage(getConfig().getString("adoptMessage").replace("&", "§"));
        makeBaby(p, null);
    }
    public void makeBaby(Player p, LivingEntity villager) {
        Villager baby = (Villager)p.getWorld().spawnEntity(p.getLocation(), EntityType.VILLAGER);
        baby.setBaby();
        List<String> children = saveFile.getStringList("players." + p.getUniqueId() + ".children");
        children.add(baby.getUniqueId().toString());
        set("players." + p.getUniqueId() + ".children", children);
        set("players." + p.getUniqueId() + ".hasBaby", true);
        set("players." + p.getUniqueId() + ".baby", baby.getUniqueId());
        playerChildMap.put(baby.getUniqueId().toString(), p.getUniqueId().toString());
        if (villager == null) return;
        List<String> villagerChildren = saveFile.getStringList("villagers." + villager.getUniqueId() + ".children");
        villagerChildren.add(baby.getUniqueId().toString());
        set("villagers." + villager.getUniqueId() + ".children", villagerChildren);
    }
    public void makePlayerBaby(Player p, Player p2) {
        Villager baby = (Villager)p.getWorld().spawnEntity(p.getLocation(), EntityType.VILLAGER);
        baby.setBaby();
        List<String> children = saveFile.getStringList("players." + p.getUniqueId() + ".children");
        children.add(baby.getUniqueId().toString());
        saveFile.set("players." + p.getUniqueId() + ".children", children);
        List<String> villagerChildren = saveFile.getStringList("players." + p2.getUniqueId() + ".children");
        villagerChildren.add(baby.getUniqueId().toString());
        saveFile.set("players." + p2.getUniqueId() + ".children", villagerChildren);
        saveFile.set("players." + p.getUniqueId() + ".hasBaby", true);
        saveFile.set("players." + p.getUniqueId() + ".baby", baby.getUniqueId());
        playerChildMap.put(baby.getUniqueId().toString(), p.getUniqueId().toString());
    }
    public void procreate(final Player p, final LivingEntity villager) {
        boolean baby = !saveFile.getString("players." + p.getUniqueId() + ".sex").equals(saveFile.getString("villagers." + villager.getUniqueId() + ".sex"));
        if (Utils.random(1, getConfig().getInt("babyChance")) != 1) baby = true;
        if (saveFile.getBoolean("players." + p.getUniqueId() + ".hasBaby")) baby = false;
        int hearts = 0;
        if (saveFile.getString("players." + p.getUniqueId() + ".happiness." + villager.getUniqueId()) != null)
            hearts = saveFile.getInt("players." + p.getUniqueId() + ".happiness." + villager.getUniqueId());
        if (hearts > 80 || (hearts > 0 && Utils.random(1, 5) == 1)) {
            p.sendMessage(getSpeech("procreate-yes", p, villager));
            loveJump(villager, 3);
            changeHearts(p, villager.getUniqueId(), Utils.random(-2, 10));
            if (baby) Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> makeBaby(p, villager), 42L);
            return;
        }
        if (sameCheck(p, "procreate")) {
            p.sendMessage(getSpeech("drunk", p, villager));
            loveJump(villager, 3);
            changeHearts(p, villager.getUniqueId(), Utils.random(-25, 0));
            return;
        }
        p.sendMessage(getSpeech("procreate-no", p, villager));
        changeHearts(p, villager.getUniqueId(), Utils.random(-10, 0));
    }

    public String getTrait() {
        return Utils.getListItem("traits",this);
    }
    public String getName(String sex) {
        return Utils.getListItem("names."+sex,this);
    }
    public String getSkin(String sex, String trait) {
        return Utils.getListItem("skins." + sex + "." + trait,this);
    }
    public boolean isBaby(LivingEntity entity) {
        return entity instanceof Villager villager && (!villager.isAdult() || !villager.canBreed() || villager.getEyeHeight() < 1.0D);
    }
    public boolean sameCheck(Player p, String type) {
        String uuid = p.getUniqueId().toString();
        if (!sameAction.containsKey(uuid)) {
            sameAction.put(uuid, type + ":1");
            return false;
        }
        if (!sameAction.get(p.getUniqueId().toString()).contains(type)) {
            sameAction.put(uuid, type + ":1");
            return false;
        }
        int times = Integer.parseInt(sameAction.get(uuid).split(":")[1]) + 1;
        sameAction.put(uuid, type + ":" + times);
        return times >= getConfig().getInt("maxSameInteraction");
    }
    public void giveItem(final LivingEntity villager, ItemStack item) {
        saveFile.set("villagers." + villager.getUniqueId() + ".has", item == null ?  null : item.getType());
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> disguise(villager), 20L);
    }
    public void disguise(LivingEntity villager) {
        String id = villager.getUniqueId().toString();
        if (isBaby(villager)) {
            if (!playerChildMap.containsKey(id)) {
                String pid = null;
                if (saveFile.getString("villagers." + id + ".parent") != null)
                    pid = saveFile.getString("villagers." + id + ".parent");
                playerChildMap.put(id, pid);
            }
            return;
        }
        String name = saveFile.getString("villagers." + id + ".name");
        String skin = saveFile.getString("villagers." + id + ".skin");
        PlayerDisguise disguise = new PlayerDisguise(name);
        if (saveFile.getString("villagers." + id + ".has") != null) {
            Material has = Material.getMaterial(saveFile.getString("villagers." + id + ".has"));
            PlayerWatcher watcher = disguise.getWatcher();
            watcher.setItemInMainHand(new ItemStack(has));
            if (has.equals(Material.BOW) && !bowList.contains(villager.getUniqueId())) {
                shootArrows(villager);
                bowList.add(villager.getUniqueId());
            }
        }
        if (saveFile.getString("villagers." + id + ".likes") != null) {
            String likes = saveFile.getString("villagers." + id + ".likes");
            Player p;
            if (likes != null && (p = getServer().getPlayer(UUID.fromString(likes))) != null) {
                List<UUID> ll = likeMap.containsKey(p.getName()) ? likeMap.get(p.getName()) : new ArrayList<>();
                ll.add(villager.getUniqueId());
                likeMap.put(p.getName(), ll);
            }
        }
        if (getConfig().getBoolean("useNames")) disguise.setSkin(skin);
        DisguiseAPI.disguiseToAll(villager, disguise);
    }
    public void getKids(Player p) {
        int count = 0;
        for (Entity e : p.getWorld().getEntities()) {
            if (entEnabled(e) && p.getUniqueId().toString().equals(saveFile.getString("villagers." + e.getUniqueId() + ".parent"))) {
                e.teleport(p);
                count++;
            }
        }
        p.sendMessage(count == 0 ? "§4RVT: §cNone of your children can be found!"
                : "§4RVT: §eFound " + count + " children and teleported them to you.");
    }
    public void like(Player p, LivingEntity villager) {
        String partner = saveFile.getString("villagers." + villager.getUniqueId() + ".partner");
        String parent = saveFile.getString("villagers." + villager.getUniqueId() + ".parent");
        int likes = 1;
        if (likeMap.get(p.getName()) != null) likes = likeMap.get(p.getName()).size();
        if (likes < getConfig().getInt("maxLikes") + 1
                && (partner == null || !partner.equals(p.getUniqueId().toString()))
                && (parent == null || !parent.equals(p.getUniqueId().toString()))) {
            giveItem(villager, new ItemStack(Material.POPPY));
            set("villagers." + villager.getUniqueId() + ".likes", p.getUniqueId());
            List<UUID> ids = likeMap.get(p.getName());
            ids.add(villager.getUniqueId());
            likeMap.put(p.getName(), ids);
        }
        changeHearts(p, villager.getUniqueId(), 15);
    }
    public void replaceSkin(String newSkin, String oldSkin) {
        ArrayList<String> cvidList = new ArrayList<>();
        for (String id : saveFile.getConfigurationSection("villagers").getKeys(false)) {
            if (oldSkin.equals(saveFile.getString("villagers." + id + ".skin"))) {
                cvidList.add(id);
                saveFile.set("villagers." + id + ".skin", newSkin);
            }
        }
        for (World w : getServer().getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (entEnabled(e) || cvidList.contains(e.getUniqueId().toString()))
                    disguise((LivingEntity)e);
            }
        }
    }

    boolean isVillager(Entity e) {
        return saveFile.getString("villagers." + e.getUniqueId() + ".name") != null;
    }

    public void makeVillager(final LivingEntity v) {
        if (getConfig().getBoolean("autoChangeVillagers") || isVillager(v))
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                String id = v.getUniqueId().toString();
                String sex = Utils.random(2) + 1 == 2 ? "male" : "female";
                String type;
                if (v instanceof Villager villager) {
                    if (villager.getProfession() == Villager.Profession.NONE) {
                        List<Villager.Profession> list = new ArrayList<>();
                        for (Villager.Profession profession : Villager.Profession.values())
                            if (profession != Villager.Profession.NONE)
                                list.add(profession);
                        villager.setProfession(list.get(Utils.random(1, list.size()) - 1));
                    }
                    type = villager.getProfession().getKey().getKey();
                } else type = v.getType().getKey().getKey().replace("_", " ");
                String skin = getSkin(sex, type);
                String name = skin;
                if (getConfig().getBoolean("useNames")) name = getName(sex);
                String title = " the " + type;
                String trait = getTrait();
                saveFile.set("villagers." + id + ".skin", skin);
                saveFile.set("villagers." + id + ".name", name);
                saveFile.set("villagers." + id + ".sex", sex);
                saveFile.set("villagers." + id + ".trait", trait);
                saveFile.set("villagers." + id + ".title", title);
                if (playerChildMap.get(id) != null) {
                    set("villagers." + id + ".parentType", "player");
                    set("villagers." + id + ".parent", playerChildMap.get(id));
                }
                disguise(v);
            }, 3L);
    }
    public void marry(Player p, LivingEntity v) {
        int hearts = 0;
        String pUuid = p.getUniqueId().toString();
        String vUuid = v.getUniqueId().toString();
        if (saveFile.getString("players." + pUuid + ".happiness." + v.getUniqueId()) != null)
            hearts = saveFile.getInt("players." + pUuid + ".happiness." + v.getUniqueId());
        if (saveFile.getString("players." + pUuid + ".partner") != null) {
            p.sendMessage(getSpeech("marry-cant", p, v));
            changeHearts(p, v.getUniqueId(), Utils.random(-50, -20));
            moodMap.put(vUuid, "sadness3");
            return;
        }
        if (hearts >= getConfig().getInt("minHeartsToMarry")) {
            p.sendMessage(getSpeech("marry-yes", p, v));
            changeHearts(p, v.getUniqueId(), Utils.random(10, 30));
            moodMap.put(vUuid, "happy5");
            saveFile.set("players." + pUuid + ".married", "villager");
            saveFile.set("players." + pUuid + ".partner", vUuid);
            saveFile.set("villagers." + vUuid + ".married", "player");
            saveFile.set("villagers." + vUuid + ".partner", pUuid);
            return;
        }
        p.sendMessage(getSpeech("marry-no", p, v));
        changeHearts(p, v.getUniqueId(), Utils.random(-50, -20));
        moodMap.put(vUuid, "anger3");
    }

    public String getSpeech(String type, Player p, LivingEntity v) {
        String uuid = p.getUniqueId().toString();
        String family = saveFile.getStringList("players."+uuid+".children").contains(v.getUniqueId().toString()) && isBaby(v) ? "Child"
                : v.getUniqueId().toString().equals(saveFile.getString("players."+uuid+".partner")) ? "Spouse"
                : "";
        return getText(type, family, p, v);
        //Message is never null as far as I can see?
        //if (message == null) message = getText(type, "", p, v);
        //if (message == null) message = "Message Error!";
        //return message;
    }
    public String getText(String type, String family, Player p, LivingEntity v) {
        String pUuid = p.getUniqueId().toString();
        String vUuid = v.getUniqueId().toString();
        if (!saveFile.contains("players." + pUuid + ".sex")) set("players." + pUuid + ".sex", "male");

        String parent1 = "female".equals(saveFile.getString("players." + pUuid + ".sex")) ? "mommy" : "daddy";
        String parent2 = "male".equals(saveFile.getString("villagers." + vUuid + ".sex")) ? "daddy" : "mommy";
        String gen = "male".equals(saveFile.getString("villagers." + vUuid + ".sex")) ? "boy" : "girl";

        List<String> talkList = getConfig().getStringList("speech" + family + "." + type);
        int index = Utils.random(talkList.size());
        String message = talkList.get(index).replace("<player>", p.getName())
                .replace("&", "§")
                .replace("<parent>", parent1)
                .replace("<parent2>", parent2)
                .replace("<sex>", gen);
        return saveFile.getString("villagers." + vUuid + ".name") + ": " + message;
    }

    public String getPathSpeech(String path, Player p, String vid) {
        List<String> talkList = getConfig().getStringList(path);
        int index = Utils.random(talkList.size());
        String message = talkList.get(index)
                .replace("<player>", p.getName())
                .replace("&", "§");
        return saveFile.getString("villagers." + vid + ".name") + ": " + message;
    }

    public void setHome(Player p, LivingEntity v) {
        String id = v.getUniqueId().toString();
        set("villagers." + id + ".home.world", v.getWorld().getName());
        set("villagers." + id + ".home.x", v.getLocation().getX());
        set("villagers." + id + ".home.y", v.getLocation().getY());
        set("villagers." + id + ".home.z", v.getLocation().getZ());
        String name = saveFile.getString("villagers." + id + ".name");
        p.sendMessage(name + "'s home set!");
    }

    public void changeHearts(Player p, UUID villagerUUID, int hearts) {
        if (saveFile.contains("players." + p.getUniqueId() + ".happiness." + villagerUUID))
            hearts += saveFile.getInt("players." + p.getUniqueId() + ".happiness." + villagerUUID);
        set("players." + p.getUniqueId() + ".happiness." + villagerUUID, hearts);
    }

    public void getAid(Player p, LivingEntity v) {
        if (aidMap.get(p.getName()) == null) {
            p.sendMessage(getSpeech("aid-yes", p, v));
            int index = Utils.random(aidItemList.size());
            p.getInventory().addItem(aidItemList.get(index));
            aidMap.put(p.getName(), getConfig().getInt("aidCooldown"));
        } else {
            p.sendMessage(getSpeech("aid-no", p, v));
            changeHearts(p, v.getUniqueId(), -1);
            int swing = Utils.random(-8, 1);
            moodSwing(v, Math.max(swing,0));
        }
    }

    public void giveGift(Player p, LivingEntity v) {
        ItemStack gift = p.getInventory().getItemInMainHand().clone();
        int amount = p.getInventory().getItemInMainHand().getAmount() - 1;
        if (amount <= 0) p.getInventory().setItemInMainHand(null);
        else p.getInventory().getItemInMainHand().setAmount(amount);

        String type = Utils.getGiftType(gift);
        if (type.equals("bow")) {
            giveItem(v, new ItemStack(Material.BOW));
            return;
        }
        if (type.equals("ring")) {
            marry(p, v);
            return;
        }
        String uuid = v.getUniqueId().toString();
        if (type.contains("-drunk")) {
            type = type.replace("-drunk", "");
            int i = type.contains("high") ? 2 : 1;
            if (Utils.random(1, 3) == 1) {
                drunkMap.put(uuid, drunkMap.containsKey(uuid) ? drunkMap.get(uuid) + i : i);
                p.sendMessage(saveFile.getString("villagers." + uuid + ".name") + ": " + getConfig().getString("drunkMessage").replace("&", "§"));
                return;
            }
        }
        if (type.equals("love") && !moodMap.get(uuid).endsWith("happy4") && !moodMap.get(uuid).endsWith("happy5"))
            moodMap.put(uuid, "happy3");
        p.sendMessage(getSpeech("gift-" + type, p, v));
        switch (type) {
            case "small" -> changeHearts(p, v.getUniqueId(), 1);
            case "regular" -> changeHearts(p, v.getUniqueId(), Utils.random(1, 5));
            case "great" -> changeHearts(p, v.getUniqueId(), Utils.random(5, 15));
            case "love" -> changeHearts(p, v.getUniqueId(), Utils.random(2, 6));
        }
        moodSwing(v, 1);
    }

}
