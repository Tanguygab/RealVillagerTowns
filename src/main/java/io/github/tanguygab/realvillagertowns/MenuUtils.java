package io.github.tanguygab.realvillagertowns;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuUtils {
    
    private final RealVillagerTowns rvt;
    private final FileConfiguration config;
    @Getter private final Map<String, String> playerMenuMap = new HashMap<>();
    @Getter private final Map<String, LivingEntity> menuMap = new HashMap<>();
    
    public MenuUtils(RealVillagerTowns rvt) {
        this.rvt = rvt;
        config = rvt.getConfig();
    }

    private void openMenu(Player p, Player p2, String action) {
        Inventory inv = rvt.getServer().createInventory(p, 9, "§0§lAsk"+action+"?");
        ItemStack y = Utils.getItem(Material.SLIME_BALL, "§2Yes", 1, List.of("§8I want to ask","§8"+p2.getName()+action));
        inv.setItem(2, y);
        ItemStack n = Utils.getItem(Material.MAGMA_CREAM, "§cNo", 1, List.of("§8I don't want to ask","§8"+p2.getName()+action));
        inv.setItem(6, n);
        p.openInventory(inv);
        playerMenuMap.put(p.getName(), p2.getName());
    }
    public void openPlayerProcreateMenu(Player p, Player p2) {
        openMenu(p,p2," to procreate");
    }
    public void openPlayerMarryMenu(Player p, Player p2) {
        openMenu(p,p2," to marry");
    }

    private void ask(Player p, String action) {
        Player p2 = rvt.getServer().getPlayer(playerMenuMap.get(p.getName()));
        Inventory inv = rvt.getServer().createInventory(p, 9, "§0§lAccept proposal?");
        ItemStack y = Utils.getItem(Material.SLIME_BALL, "§2Yes", 1, List.of("§8I want to", "§8"+action+p.getName()));
        inv.setItem(2, y);
        ItemStack n = Utils.getItem(Material.MAGMA_CREAM, "§cNo", 1, List.of("§8I don't want to", "§8"+action+p.getName()));
        inv.setItem(6, n);
        p2.openInventory(inv);
        playerMenuMap.put(p2.getName(), p.getName());
    }
    public void marryAsk(Player p) {
        ask(p,"marry ");
    }
    public void procreateAsk(Player p) {
        ask(p,"procreate with ");
    }

    public void openVillagerMenu(Player p, LivingEntity v) {
        String name = rvt.saveFile.getString("villagers." + v.getUniqueId() + ".name");
        String title = rvt.saveFile.getString("villagers." + v.getUniqueId() + ".title");
        int length = title.length()+4;
        if (length > 32) title = title.substring(0,28);
        if (length+name.length() > 32) name = name.substring(0,32-length);

        Inventory inv = rvt.getServer().createInventory(p, 9, "§0§l" + name + title);
        int index = 0;
        if (config.getList("buttons").contains("interact")) {
            ItemStack s = Utils.getItem(Material.SLIME_BALL, getLang("interact"));
            inv.setItem(index, s);
            index++;
        }
        if (v instanceof Villager villager) {
            if (!villager.getProfession().getKey().getKey().equals("NITWIT") && config.getStringList("buttons").contains("trade")) {
                ItemStack s = Utils.getItem(Material.SLIME_BALL, getLang("trade"));
                inv.setItem(index, s);
                index++;
            }
            if (config.getStringList("buttons").contains("setHome")) {
                ItemStack s = Utils.getItem(Material.SLIME_BALL, getLang("setHome"));
                inv.setItem(index, s);
                index++;
            }
            if (!rvt.isMarried(p, v) && !rvt.isChild(p, v))
                if (config.getStringList("buttons").contains("requestAid")) {
                    ItemStack s = Utils.getItem(Material.SLIME_BALL, getLang("requestAid"));
                    inv.setItem(index, s);
                    index++;
                }
            if (!rvt.isBaby(v) && ((Villager) v).getProfession().getKey().getKey().equals("priest")) {
                if (config.getStringList("buttons").contains("divorce")) {
                    ItemStack s = Utils.getItem(Material.SLIME_BALL, getLang("divorce"));
                    inv.setItem(index, s);
                    index++;
                }
                if (config.getStringList("buttons").contains("adopt")) {
                    ItemStack s = Utils.getItem(Material.SLIME_BALL, getLang("adopt"));
                    inv.setItem(index, s);
                    index++;
                }
            }
        }
        ItemStack i = rvt.getInfo(p, v);
        inv.setItem(8, i);
        p.openInventory(inv);
        menuMap.put(p.getName(), v);
    }


    public void openInteractMenu(Player p, LivingEntity v) {
        String name = rvt.saveFile.getString("villagers." + v.getUniqueId() + ".name");
        Inventory inv = rvt.getServer().createInventory(p, 18, "§0§lInteract with " + name);
        int index = 0;
        List<String> interactions = config.getStringList("interactions");
        List<String> interactionsNames = List.of("gift","chat","joke","greet","insult","story");
        for (String iName : interactionsNames) index = checkInteraction(iName,index,inv,interactions);

        if (!rvt.isChild(p, v)) {
            index = checkInteraction("flirt",index,inv,interactions);
            if (rvt.likes(p, v) && (rvt.saveFile.getInt("players." + p.getUniqueId() + ".happiness." + v.getUniqueId()) > 50
                            || (rvt.drunkMap.get(v.getUniqueId().toString()) != null
                            && rvt.drunkMap.get(v.getUniqueId().toString()) >= 1)))
                index = checkInteraction("kiss",index,inv,interactions);
        }
        if (rvt.isBaby(v)) index = checkInteraction("play",index,inv,interactions);
        if (rvt.likes(p, v) && (
                rvt.saveFile.getInt("players." + p.getUniqueId() + ".happiness." + v.getUniqueId()) > 200
                        || rvt.isMarried(p, v)
                        || (rvt.drunkMap.get(v.getUniqueId().toString()) != null
                        && rvt.drunkMap.get(v.getUniqueId().toString()) >= 3)))
            index = checkInteraction("procreate",index,inv,interactions);

        if (rvt.saveFile.getInt("players." + p.getUniqueId() + ".happiness." + v.getUniqueId()) > 100
                || rvt.isChild(p, v) || rvt.isBaby(v) || rvt.isMarried(p, v)) {

            if (interactions.contains("follow")) {
                String lang = rvt.followMap.containsKey(v) && rvt.followMap.get(v).equals(p) ? "stopFollow" : "follow";
                ItemStack s = Utils.getItem(Material.SLIME_BALL, getLang(lang));
                inv.setItem(index, s);
                index++;
            }
            if (interactions.contains("stay")) {
                ItemStack s = Utils.getItem(Material.SLIME_BALL, getLang(rvt.isStaying(v) ? "move" : "stay"));
                inv.setItem(index, s);
            }
        }
        p.openInventory(inv);
        menuMap.put(p.getName(), v);
    }

    private int checkInteraction(String name, int index, Inventory inv, List<String> interactions) {
        if (!interactions.contains(name)) return index;
        ItemStack s = Utils.getItem(Material.SLIME_BALL, getLang(name));
        inv.setItem(index, s);
        return index+1;
    }


    private String getLang(String str) {
        return rvt.langFile.getString("lang."+str).replace("&", "§");
    }
}
