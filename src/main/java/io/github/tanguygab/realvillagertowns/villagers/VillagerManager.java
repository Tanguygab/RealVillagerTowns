package io.github.tanguygab.realvillagertowns.villagers;

import io.github.tanguygab.realvillagertowns.ArrowHomingTask;
import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.Utils;
import io.github.tanguygab.realvillagertowns.villagers.enums.Interaction;
import io.github.tanguygab.realvillagertowns.villagers.enums.entity.Gender;
import io.github.tanguygab.realvillagertowns.villagers.enums.entity.Mood;
import io.github.tanguygab.realvillagertowns.villagers.enums.entity.RVTEntityType;
import io.github.tanguygab.realvillagertowns.villagers.enums.entity.Trait;
import io.github.tanguygab.realvillagertowns.villagers.enums.speeches.BooleanSpeech;
import io.github.tanguygab.realvillagertowns.villagers.enums.speeches.Speech;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class VillagerManager {

    private final RealVillagerTowns rvt;
    private final FileConfiguration data;
    @Getter private final Map<UUID,RVTVillager> villagers = new HashMap<>();
    @Getter private final Map<UUID,RVTPlayer> players = new HashMap<>();

    private final Map<Gender,List<String>> names = new HashMap<>();
    private final Map<String,Map<Gender,List<String>>> skins = new HashMap<>();

    public VillagerManager(RealVillagerTowns rvt) {
        this.rvt = rvt;
        data = rvt.data;

        names.put(Gender.MALE,rvt.getNames().getStringList("male"));
        names.put(Gender.FEMALE,rvt.getNames().getStringList("female"));
        rvt.getSkins().getKeys(false).forEach(profession->{
            ConfigurationSection cfg = rvt.getSkins().getConfigurationSection(profession);
            if (cfg == null) return;
            Map<Gender,List<String>> skinMap = new HashMap<>();
            skinMap.put(Gender.MALE,cfg.getStringList("male"));
            skinMap.put(Gender.FEMALE,cfg.getStringList("female"));
            skins.put(profession,skinMap);
        });

        rvt.getServer().getWorlds().forEach(world->world.getLivingEntities().forEach(entity->{
            if (isVillagerEntity(entity)) makeVillager(entity);
        }));
        rvt.getServer().getOnlinePlayers().forEach(this::loadPlayer);
    }

    public void unload() {
        villagers.values().forEach(this::saveVillager);
        players.values().forEach(this::savePlayer);
    }

    public boolean isVillagerEntity(Entity entity) {
        return entity instanceof LivingEntity && rvt.getConfiguration().isVillagerType(entity);
    }

    public boolean isVillager(Entity entity) {
        return isVillagerEntity(entity) && villagers.containsKey(entity.getUniqueId());
    }
    public void makeVillager(LivingEntity entity) {
        if (!data.contains("villagers."+entity.getUniqueId()) && !rvt.getConfiguration().AUTO_CHANGE_VILLAGERS) return;
        RVTVillager villager = data.contains("villagers."+entity.getUniqueId()) ? loadVillager(entity) : createVillager(entity);
        if (villager == null) return;
        villagers.put(villager.getUniqueId(),villager);
        disguise(villager);
    }

    private RVTVillager createVillager(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        Gender gender = Utils.random(2) + 1 == 2 ? Gender.MALE : Gender.FEMALE;
        String type;
        if (entity instanceof Villager villager) {
            if (villager.getProfession() == Villager.Profession.NONE) {
                List<Villager.Profession> list = new ArrayList<>();
                for (Villager.Profession profession : Villager.Profession.values())
                    if (profession != Villager.Profession.NONE)
                        list.add(profession);
                villager.setProfession(list.get(Utils.random(1, list.size()) - 1));
            }
            type = villager.getProfession().getKey().getKey();
        } else type = entity.getType().getKey().getKey().replace("_", " ");
        String name = Utils.randomFromList(names.get(gender));
        String skin = rvt.getConfiguration().USE_NAMES_AS_SKINS ? name : Utils.randomFromList(skins.getOrDefault(type,Map.of()).getOrDefault(gender,List.of("Steve")));
        String title = " the " + type;

        Trait trait = Trait.values()[Utils.random(Trait.values().length)];

        return new RVTVillager(entity,uuid,name,gender,skin,title,trait);
    }

    private RVTVillager loadVillager(LivingEntity entity) {
        UUID id = entity.getUniqueId();
        ConfigurationSection cfg = data.getConfigurationSection("villagers."+id);
        if (cfg == null) return null;

        String name = cfg.getString("name");
        Gender gender = Gender.valueOf(cfg.getString("gender"));
        String skin = cfg.getString("skin");
        String title = cfg.getString("title");
        Trait trait = Trait.valueOf(cfg.getString("trait"));
        if (name == null || skin == null || title == null) return null;

        RVTVillager villager = new RVTVillager(entity,id,name,gender,skin,title,trait);

        if (cfg.contains("parent")) {
            RVTEntityType parentType = RVTEntityType.valueOf(cfg.getString("parent.type"));
            UUID parent1 = UUID.fromString(Objects.requireNonNull(cfg.getString("parent.parent1")));
            UUID parent2 = cfg.contains("parent2") ? UUID.fromString(Objects.requireNonNull(cfg.getString("parent.parent2"))) : null;
            villager.setParent(parentType,parent1,parent2);
        }
        if (cfg.contains("partner")) {
            RVTEntityType type = RVTEntityType.valueOf(cfg.getString("partner.type"));
            UUID partner = UUID.fromString(Objects.requireNonNull(cfg.getString("partner.uuid")));
            villager.marry(partner,type);
        }
        if (cfg.contains("children")) cfg.getStringList("children").forEach(uuid->villager.getChildren().add(UUID.fromString(uuid)));

        ConfigurationSection home = cfg.getConfigurationSection("home");
        if (home != null) {
            String worldName = home.getString("world");
            if (worldName != null) {
                World world = rvt.getServer().getWorld(worldName);
                if (world != null) {
                    Location loc = new Location(world, home.getDouble("x"), home.getDouble("y"), home.getDouble("home.z"));
                    villager.setHome(loc);
                }
            }
        }
        if (cfg.contains("likes")) villager.setLikes(UUID.fromString(Objects.requireNonNull(cfg.getString("likes"))));
        if (cfg.contains("item")) villager.setInHand(Material.getMaterial(Objects.requireNonNull(cfg.getString("item"))));
        villager.setDrunk(cfg.getInt("drunk",0));
        villager.setMood(Mood.valueOf(cfg.getString("mood")),cfg.getInt("mood-level",1));
        return villager;
    }

    private void saveVillager(RVTVillager villager) {
        UUID id = villager.getUniqueId();
        ConfigurationSection cfg = data.getConfigurationSection("villagers."+id);
        if (cfg == null) {
            data.set("villagers."+id+".name",villager.getName());
            cfg = data.getConfigurationSection("villagers."+id);
        }
        assert cfg != null;
        cfg.set("name", villager.getName());
        cfg.set("gender", villager.getGender().toString());
        cfg.set("skin", villager.getSkin());
        cfg.set("title", villager.getTitle());
        cfg.set( "trait", villager.getTrait().toString());

        cfg.set("parent.type",villager.getParentType());
        cfg.set("parent.parent1",villager.getParent1() == null ? null : villager.getParent1());
        cfg.set("parent.parent2",villager.getParent2() == null ? null : villager.getParent2());

        cfg.set("partner.type",villager.getPartnerType() == null ? null : villager.getPartnerType());
        cfg.set("partner.uuid",villager.getPartner() == null ? null : villager.getPartner());
        cfg.set("children",villager.getChildren().isEmpty() ? null : villager.getChildren().stream().map(UUID::toString).toList());

        if (villager.getHome() != null) {
            cfg.set("home.world", villager.getEntity().getWorld().getName());
            Location loc = villager.getEntity().getLocation();
            cfg.set("home.x", loc.getX());
            cfg.set("home.y", loc.getY());
            cfg.set("home.z", loc.getZ());
        }
        cfg.set("likes",villager.getLikes() == null ? null : villager.getLikes().toString());
        cfg.set("item",villager.getInHand() == null ? null : villager.getInHand().toString());
        cfg.set("drunk",villager.getDrunk());
        cfg.set("mood",villager.getMood().toString());
        cfg.set("mood-level",villager.getMoodLevel());
    }

    public void loadPlayer(Player p) {
        UUID id = p.getUniqueId();
        ConfigurationSection cfg = data.getConfigurationSection("players."+id);
        if (cfg == null) {
            players.put(id,new RVTPlayer(p,p.getUniqueId(),p.getName(),Gender.MALE));
            return;
        }

        RVTPlayer player = new RVTPlayer(p,id,p.getName(),Gender.valueOf(cfg.getString("gender")));

        if (cfg.contains("partner")) {
            RVTEntityType type = RVTEntityType.valueOf(cfg.getString("partner.type"));
            UUID partner = UUID.fromString(Objects.requireNonNull(cfg.getString("partner.uuid")));
            player.marry(partner,type);
        }
        if (cfg.contains("children")) cfg.getStringList("children").forEach(uuid->player.getChildren().add(UUID.fromString(uuid)));
        if (cfg.getBoolean("has-baby",false)) player.setBaby(UUID.fromString(Objects.requireNonNull(cfg.getString("baby"))));
        if (cfg.contains("likes")) cfg.getStringList("likes").forEach(uuid->player.getLikes().add(UUID.fromString(uuid)));

        ConfigurationSection happiness = cfg.getConfigurationSection("happiness");
        if (happiness != null) {
            Map<String,Object> map = happiness.getValues(false);
            if (!map.isEmpty()) map.forEach((uuid,lvl)->player.getHappiness().put(UUID.fromString(uuid), (int) lvl));
        }
        players.put(id,player);
    }
    public void savePlayer(RVTPlayer player) {
        UUID id = player.getUniqueId();
        ConfigurationSection cfg = data.getConfigurationSection("players."+id);
        if (cfg == null) {
            data.set("players."+id+".name",player.getName());
            cfg = data.getConfigurationSection("players."+id);
        }
        assert cfg != null;
        cfg.set("name", player.getName());
        cfg.set("gender", player.getGender().toString());

        cfg.set("partner.type",player.getPartnerType() == null ? null : player.getPartnerType());
        cfg.set("partner.uuid",player.getPartner() == null ? null : player.getPartner());
        cfg.set("children",player.getChildren().isEmpty() ? null : player.getChildren().stream().map(UUID::toString).toList());
        cfg.set("has-baby",player.isHasBaby());
        cfg.set("baby",player.isHasBaby() ? player.getBaby().toString() : null);

        cfg.set("likes",player.getLikes() == null ? null : player.getLikes().stream().map(UUID::toString).toList());
        Map<String,Integer> happiness = new HashMap<>();
        player.getHappiness().forEach((uuid,lvl)->happiness.put(uuid.toString(),lvl));
        cfg.set("happiness",happiness.isEmpty() ? null : happiness);
    }

    public RVTVillager getVillager(Entity entity) {
        return villagers.get(entity.getUniqueId());
    }
    public String getVillagerName(RVTEntityType type, UUID uuid) {
        if (type == null || uuid == null) return "No one";
        return type == RVTEntityType.PLAYER ? players.get(uuid).getName() : villagers.get(uuid).getName();
    }
    public RVTPlayer getPlayer(Player p) {
        return p == null ? null : players.get(p.getUniqueId());
    }
    public RVTPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public void disguise(RVTVillager villager) {
        if (villager.isBaby()) return;

        PlayerDisguise disguise = new PlayerDisguise(villager.getName(),villager.getSkin());
        Material hand = villager.getInHand();
        if (hand != null) disguise.getWatcher().setItemInMainHand(new ItemStack(hand));
        DisguiseAPI.disguiseToAll(villager.getEntity(), disguise);
    }

    public void villagerDeath(LivingEntity v) {
        UUID id = v.getUniqueId();
        RVTVillager villager = villagers.get(id);

        if (villager.getPartnerType() == RVTEntityType.PLAYER) {
            UUID partnerUUID = villager.getPartner();
            if (players.containsKey(partnerUUID)) {
                RVTPlayer partner = players.get(partnerUUID);
                partner.divorce();

                partner.getPlayer().sendMessage("§cYour "+(villager.getGender() == Gender.FEMALE ? "wife" : "husband")+" " + villager.getName() + " has died!");
            } else {
                rvt.set("players." + partnerUUID + ".married", null);
                rvt.set("players." + partnerUUID + ".partner", null);
            }
        }
        if (villager.getParentType() == RVTEntityType.PLAYER) {
            UUID parentUUID = villager.getParent1();
            if (players.containsKey(parentUUID)) {
                RVTPlayer parent = players.get(parentUUID);
                if (parent.getBaby().equals(id)) {
                    parent.clearBaby();
                }
                parent.getChildren().remove(id);

                parent.getPlayer().sendMessage("§cYour "+(villager.getGender() == Gender.FEMALE ? "daughter" : "son")+" " + villager.getName() + " has died!");
            } else {
                rvt.set("players." + parentUUID + ".hasBaby", false);
                rvt.set("players." + parentUUID + ".baby", null);
                List<String> children = data.getStringList("players." + parentUUID + ".children");
                children.remove(id.toString());
                rvt.set("players." + parentUUID + ".children",children);
            }
        }
        rvt.set("villagers." + id, null);
    }

    public void procreate(RVTPlayer player, RVTVillager villager) {
        if (player.getGender() == villager.getGender() || player.isHasBaby()) return;
        boolean baby = rvt.getConfiguration().getBabyChance();

        int hearts = player.getHappiness(villager);

        if (hearts > 80 || (hearts > 0 && Utils.random(1, 5) == 1)) {
            BooleanSpeech.PROCREATE.sendGood(player,villager);
            loveJump(villager);
            player.setHappiness(villager, Utils.random(-2, 10));
            if (baby) rvt.getServer().getScheduler().scheduleSyncDelayedTask(rvt, () -> rvt.makeBaby(player, villager), 42L);
            return;
        }
        if (player.updateLastInteraction(Interaction.PROCREATE)) {
            Speech.DRUNK.send(player,villager);
            loveJump(villager);
            player.setHappiness(villager, Utils.random(-25, 0));
            return;
        }
        BooleanSpeech.PROCREATE.sendBad(player,villager);
        player.setHappiness(villager, Utils.random(-10, 0));
    }
    public void loveJump(RVTVillager villager) {
        for (int i = 0; i <= 3; i++)
            rvt.getServer().getScheduler().scheduleSyncDelayedTask(rvt, villager::jump, i * 16L);
    }

    public void shootArrows() {
        villagers.values().forEach(villager->{
            if (villager.getInHand() != Material.BOW && villager.getInHand() != Material.CROSSBOW) return;

            int SHOOT_RADIUS = rvt.getConfiguration().SHOOT_RADIUS;
            for (Entity e : villager.getEntity().getNearbyEntities(SHOOT_RADIUS, SHOOT_RADIUS, SHOOT_RADIUS)) {
                if (rvt.getConfiguration().isHostileMob(e)) {
                    shootArrow(villager.getEntity(), e);
                    break;
                }
            }
        });
    }
    private void shootArrow(LivingEntity villager, Entity victim) {
        Location loc1 = victim.getLocation();
        Location loc2 = villager.getLocation();
        loc2.setY(loc2.getY() + 1.0D);
        loc2.setX(loc2.getBlockX() + 0.5D);
        loc2.setZ(loc2.getBlockZ() + 0.5D);
        int arrowSpeed = 1;
        Arrow a = villager.getWorld().spawnArrow(loc2, new org.bukkit.util.Vector(loc1.getX() - loc2.getX(), loc1.getY() - loc2.getY(), loc1.getZ() - loc2.getZ()), arrowSpeed, 12.0F);
        a.setShooter(villager);
        double minAngle = 6.283185307179586D;
        LivingEntity minEntity = null;
        for (Entity entity : villager.getNearbyEntities(64.0D, 64.0D, 64.0D)) {
            if (villager.hasLineOfSight(entity) && entity instanceof LivingEntity v && !entity.isDead()) {
                Vector toTarget = entity.getLocation().toVector().clone().subtract(villager.getLocation().toVector());
                double angle = a.getVelocity().angle(toTarget);
                if (angle < minAngle) {
                    minAngle = angle;
                    minEntity = v;
                }
            }
        }
        if (minEntity != null) new ArrowHomingTask(a,minEntity,rvt);
    }


}
