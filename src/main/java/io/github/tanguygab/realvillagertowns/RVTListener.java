package io.github.tanguygab.realvillagertowns;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RVTListener implements Listener {

    private final RealVillagerTowns rvt;
    private final FileConfiguration config;
    private final Interact interact;
    
    public RVTListener(RealVillagerTowns rvt) {
        this.rvt = rvt;
        config = rvt.getConfig();
        interact = new Interact(rvt);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (!(e.getRightClicked() instanceof LivingEntity clicked)) return;
        if (!rvt.tradeList.contains(p.getName())
                && rvt.entEnabled(e.getRightClicked())
                && config.getBoolean("useVillagerInteractions")
                && !rvt.interactList.contains(p.getUniqueId().toString())
                && !clicked.hasMetadata("NPC")
                && !clicked.hasMetadata("shopkeeper")) {
            e.setCancelled(true);
            if (rvt.saveFile.getString("villagers." + clicked.getUniqueId() + ".has") != null && rvt.saveFile.getString("villagers." + clicked.getUniqueId() + ".has").equalsIgnoreCase(Material.POPPY.toString())) {
                String likes = rvt.saveFile.getString("villagers." + clicked.getUniqueId() + ".likes");
                if (p.getUniqueId().toString().equals(likes)) {
                    p.getInventory().addItem(new ItemStack(Material.BLUE_ORCHID));
                    String trait = rvt.saveFile.getString("villagers." + clicked.getUniqueId() + ".trait");
                    String pSex = rvt.saveFile.getString("players." + p.getUniqueId() + ".sex");
                    String nice = "beautiful";
                    String type = "girl";
                    if (pSex.equals("male")) {
                        nice = "handsome";
                        type = "boy";
                    }
                    p.sendMessage(rvt.getText(trait.toLowerCase() + "-gift", "Like", p, clicked).replace("<nice>", nice).replace("<sex2>", type));
                    rvt.giveItem(clicked, null);
                    return;
                }
            }
            if (rvt.giftMap.get(p.getName()) != null && (rvt.giftMap.get(p.getName())).equals(clicked.getUniqueId())) {
                rvt.giveGift(p, clicked);
                rvt.giftMap.remove(p.getName());
                String id = p.getUniqueId().toString();
                rvt.interactList.add(id);
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(rvt, () -> rvt.interactList.remove(id),  3L);
                return;
            }
            if (p.getInventory().getItemInMainHand().getType().equals(Material.LEAD) && clicked.setLeashHolder(p)) {
                    if (p.getInventory().getItemInMainHand().getAmount() > 1)
                        p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
                    else p.getInventory().setItemInMainHand(null);
            }
            rvt.getMenuUtils().openVillagerMenu(p, clicked);
            return;
        }
        if (clicked instanceof Player pClicked && config.getBoolean("enablePlayerMarriage")) {
            e.setCancelled(true);
            if (pClicked.getUniqueId().toString().equals(rvt.saveFile.getString("players." + p.getUniqueId() + ".partner"))) {
                rvt.getMenuUtils().openPlayerProcreateMenu(p, pClicked);
                return;
            }
            if (config.getList("playerButtons").contains("marry"))
                rvt.getMenuUtils().openPlayerMarryMenu(p, pClicked);
            return;
        }
        if (clicked instanceof Wolf
                && !e.getRightClicked().getPassengers().isEmpty()
                && e.getRightClicked().getPassengers().get(0) instanceof LivingEntity passenger
                && rvt.entEnabled(passenger)) {
            e.getRightClicked().remove();
            rvt.stopFollow(passenger);
            return;
        }
        if (rvt.tradeList.contains(p.getName()))
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(rvt, () -> rvt.tradeList.remove(p.getName()),  20L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerClick(EntityTargetEvent e) {
        if (!rvt.isVillager(e.getEntity()) || !(e.getTarget() instanceof Player p)) return;
        LivingEntity v = (LivingEntity)e.getEntity();
        if (rvt.likes(p, v) && rvt.saveFile.getInt("players." + p.getUniqueId() + ".happiness." + v.getUniqueId()) > 25
                || (rvt.drunkMap.get(v.getUniqueId().toString()) != null && rvt.drunkMap.get(v.getUniqueId().toString()) >= 1))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        Player player = (Player)e.getWhoClicked();
        LivingEntity villager = rvt.getMenuUtils().getMenuMap().get(player.getName());
        String currentItem = e.getCurrentItem().getItemMeta().getDisplayName();
        switch (title) {
            case "§lInteract" -> {
                e.setCancelled(true);
                player.closeInventory();
                rvt.getMenuUtils().getMenuMap().remove(player.getName());
                if (currentItem.equals(getLang("gift"))) {
                    player.sendMessage("Right-click on a villager to give them the item in your hand.");
                    rvt.giftMap.put(player.getName(), villager.getUniqueId());
                    return;
                }
                if (List.of("chat", "joke", "greet", "kiss", "story", "flirt", "insult", "play", "follow", "stop", "stay", "move").contains(currentItem))
                    interact.interact(player, villager, currentItem);
                else if (currentItem.equals(getLang("procreate"))) rvt.procreate(player, villager);

            }
            case "§lAsk to marry?" -> {
                e.setCancelled(true);
                if (currentItem.equals("§2Yes")) {
                    player.closeInventory();
                    rvt.getMenuUtils().marryAsk(player);
                } else if (currentItem.equals("§cNo")) player.closeInventory();
            }
            case "§lAsk to procreate?" -> {
                e.setCancelled(true);
                if (currentItem.equals("§2Yes")) {
                    player.closeInventory();
                    rvt.getMenuUtils().procreateAsk(player);
                } else if (currentItem.equals("§cNo")) player.closeInventory();
            }
            case "§lAccept proposal?" -> {
                e.setCancelled(true);
                if (currentItem.equals("§2Yes")) {
                    player.closeInventory();
                    rvt.playerMarry(player);
                } else if (currentItem.equals("§cNo")) {
                    player.closeInventory();
                    Player p2 = rvt.getServer().getPlayer(rvt.getMenuUtils().getPlayerMenuMap().get(player.getName()));
                    p2.sendMessage(player.getName() + " declined your request.");
                    rvt.getMenuUtils().getPlayerMenuMap().remove(player.getName());
                }
            }
            case "§lProcreate?" -> {
                e.setCancelled(true);
                if (currentItem.equals("§2Yes")) {
                    player.closeInventory();
                    rvt.playerProcreate(player);
                } else if (currentItem.equals("§cNo")) {
                    player.closeInventory();
                    Player p2 = rvt.getServer().getPlayer(rvt.getMenuUtils().getPlayerMenuMap().get(player.getName()));
                    p2.sendMessage(player.getName() + " declined your request.");
                    rvt.getMenuUtils().getPlayerMenuMap().remove(player.getName());
                }
            }
            default -> {
                if (!title.contains("§l") || e.getInventory().getItem(8).getType() != Material.LIME_DYE) return;
                e.setCancelled(true);
                player.closeInventory();
                if (currentItem.equals("§7Interact")) {
                    rvt.getMenuUtils().openInteractMenu(player, villager);
                    return;
                }
                rvt.getMenuUtils().getMenuMap().remove(player.getName());

                if (currentItem.equals(getLang("trade"))) rvt.trade(player);
                else if (currentItem.equals(getLang("requestAid"))) rvt.getAid(player, villager);
                else if (currentItem.equals(getLang("setHome"))) rvt.setHome(player, villager);
                else if (currentItem.equals(getLang("adopt"))) rvt.adoptBaby(player);
                else if (currentItem.equals(getLang("divorce"))) rvt.divorce(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (rvt.saveFile.getString("players." + p.getUniqueId() + ".name") != null) return;
        rvt.set("players." + p.getUniqueId() + ".name", p.getName());
        rvt.set("players." + p.getUniqueId() + ".sex", "male");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Projectile pr && pr.getShooter() instanceof Entity shooter
                && rvt.entEnabled(e.getEntity()) && rvt.entEnabled(shooter)) {
            e.setCancelled(true);
            return;
        }
        if (!(e.getDamager() instanceof Player p) || !rvt.entEnabled(e.getEntity()) || !rvt.isVillager(e.getEntity())) return;
        LivingEntity v = (LivingEntity)e.getEntity();
        if (v.getHealth() - e.getDamage() <= 0.0D) return;
        if (rvt.moodMap.get(v.getUniqueId().toString()) == null || !(rvt.moodMap.get(v.getUniqueId().toString())).contains("anger")) {
            rvt.moodMap.put(v.getUniqueId().toString(), "anger1");
        } else {
            int swing = Utils.random(-1, 1);
            if (swing < 0)
                swing = 0;
            swing *= -1;
            rvt.moodSwing(v, swing);
        }
        p.sendMessage(rvt.getSpeech("punch", p, v));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(ChunkLoadEvent e) {
        for (Entity entity : e.getChunk().getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (rvt.entEnabled(entity)) {
                if (!rvt.isVillager(entity)) {
                    rvt.makeVillager(living);
                    continue;
                }
                rvt.disguise(living);
                continue;
            }
            if (entity instanceof Wolf && !entity.getPassengers().isEmpty()
                    && entity.getPassengers().get(0) instanceof LivingEntity liv
                    && rvt.entEnabled(liv) && rvt.followMap.get(liv) == null) {
                entity.remove();
                rvt.stopFollow(liv);
            }
        }
        if (!config.getBoolean("spawnRandomVillagers")) return;
        int r = Utils.random(config.getInt("randomVillagerChance") - 1 + 1) + 1;
        if (r != 1) return;
        World w = e.getWorld();
        Chunk c = e.getChunk();
        Location tmp = new Location(w, (c.getX() * 16), 0.0D, (c.getZ() * 16));
        Location loc = new Location(w, (c.getX() * 16), w.getHighestBlockYAt(tmp), (c.getZ() * 16));
        loc.setY(loc.getY() + 1.0D);
        if (!List.of(Material.WATER, Material.LAVA).contains(loc.getWorld().getHighestBlockAt(loc).getType()))
            e.getChunk().getWorld().spawnEntity(loc, EntityType.VILLAGER);

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInvClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof Player && rvt.tradeList.contains(((Player)e.getInventory().getHolder()).getName()))
            rvt.tradeList.remove(((Player)e.getInventory().getHolder()).getName());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLivingEntityDie(EntityDeathEvent e) {
        if (rvt.entEnabled(e.getEntity()) && rvt.isVillager(e.getEntity()))
            villagerDeath(e.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetEvent e) {
        if (!e.getEntity().getPassengers().isEmpty() && e.getEntity().getPassengers().get(0) instanceof LivingEntity entity && rvt.followMap.get(entity) != null)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLivingEntitySpawn(CreatureSpawnEvent e) {
        if (rvt.entEnabled(e.getEntity()) && !rvt.isVillager(e.getEntity())) {
            LivingEntity v = e.getEntity();
            rvt.makeVillager(v);
        }
    }

    public void villagerDeath(LivingEntity v) {
        String id = v.getUniqueId().toString();
        FileConfiguration saveFile = rvt.saveFile;
        if ("player".equals(saveFile.getString("villagers." + id + ".married"))) {
            deathMessage(id);
            String pid = saveFile.getString("villagers." + id + ".partner");
            saveFile.set("players." + pid + ".married", null);
            saveFile.set("players." + pid + ".partner", null);
        }
        if ("player".equals(saveFile.getString("villagers." + id + ".parentType"))) {
            String pid = saveFile.getString("villagers." + id + ".parent");
            if (id.equals(saveFile.getString("players." + pid + ".baby"))) {
                saveFile.set("players." + pid + ".hasBaby", false);
                saveFile.set("players." + pid + ".baby", null);
            }
            List<String> children = saveFile.getStringList("players." + pid + ".children");
            children.remove(id);
            saveFile.set("players." + pid + ".children", children);
            deathMessage(id);
        }
        saveFile.set("villagers." + id, null);
    }
    public void deathMessage(String id) {
        if (rvt.saveFile.getString("villagers." + id + ".married").equals("player")) {
            Player p = rvt.getServer().getPlayer(rvt.saveFile.getString("villagers." + id + ".partner"));
            if (p != null) {
                String sex = rvt.saveFile.getString("villagers." + p.getUniqueId() + ".sex");
                String name = rvt.saveFile.getString("villagers." + p.getUniqueId() + ".name");
                p.sendMessage("§cYour "+("female".equals(sex) ? "wife" : "husband")+" " + name + " has died!");
            } else rvt.getLogger().warning("Player not found!");
        }
        if (!"player".equals(rvt.saveFile.getString("villagers." + id + ".parentType"))) return;
        Player p = rvt.getServer().getPlayer(rvt.saveFile.getString("villagers." + id + ".parent"));
        if (p == null) {
            rvt.getLogger().warning("Player not found!");
            return;
        }
        String sex = rvt.saveFile.getString("villagers." + p.getUniqueId() + ".sex");
        String name = rvt.saveFile.getString("villagers." + p.getUniqueId() + ".name");
        p.sendMessage("§cYour "+("female".equals(sex) ? "daughter" : "son")+" " + name + " has died!");
    }

    private String getLang(String str) {
        return rvt.getLang(str);
    }
}
