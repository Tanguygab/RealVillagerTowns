package io.github.tanguygab.realvillagertowns.menus;

import io.github.tanguygab.realvillagertowns.Utils;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;
import io.github.tanguygab.realvillagertowns.villagers.enums.Button;
import io.github.tanguygab.realvillagertowns.villagers.enums.entity.Mood;
import io.github.tanguygab.realvillagertowns.villagers.enums.speeches.BooleanSpeech;
import io.github.tanguygab.realvillagertowns.villagers.enums.speeches.Speech;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.util.UUID;

public class VillagerMenu extends RVTMenu {

    private final RVTVillager villager;

    public VillagerMenu(RVTPlayer player, RVTVillager villager) {
        super(player, "", 1);
        this.villager = villager;

        String name = villager.getName();
        String title = villager.getTitle();
        int length = title.length()+4;
        if (length > 32) title = title.substring(0,28);
        if (length+name.length() > 32) name = name.substring(0,32-length);

        inv = rvt.getServer().createInventory(null, 9, "§0§l" + name + title);
    }

    @Override
    public void onOpen() {
        addItem(Button.INTERACT);

        inv.setItem(8, getInfo(player, villager));

        if (!(villager.getEntity() instanceof Villager v)) {
            open();
            return;
        }
        if (v.getProfession() != Villager.Profession.NITWIT && v.getProfession() != Villager.Profession.NONE) addItem(Button.TRADE);
        addItem(Button.SET_HOME);
        if (!player.isMarried(villager) && !player.isChild(villager)) addItem(Button.REQUEST_AID);
        if (!player.isBaby(villager) && v.getProfession() == Villager.Profession.CLERIC) {
            addItem(Button.DIVORCE);
            addItem(Button.ADOPT);
        }
        open();
    }

    private void addItem(Button button) {
        inv.addItem(createMenuItem(Material.SLIME_BALL,button.getLang()));
    }

    @Override
    public void onClick(ItemStack item, int slot) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        Button button = Button.fromLang(meta.getDisplayName());
        if (button == null) return;

        switch (button) {
            case INTERACT -> {
                new InteractionMenu(player,villager).onOpen();
                return;
            }
            case TRADE -> {
                player.getPlayer().openMerchant((Villager) villager.getEntity(),true);
                return;
            }
            case REQUEST_AID -> {
                if (rvt.getConfiguration().isOnCooldown(player.getAidCooldown())) {
                    BooleanSpeech.AID.sendBad(player,villager);
                    player.setHappiness(villager, -1);
                    villager.swingMood(Math.max(Utils.random(-8, 1), 0));
                    return;
                }
                BooleanSpeech.AID.sendGood(player,villager);
                player.getPlayer().getInventory().addItem(rvt.getConfiguration().getAidItem());
                player.setAidCooldown(LocalDateTime.now());
            }
            case SET_HOME -> {
                villager.setHome();
                player.sendMessage(villager.getName() + "'s home set!");
            }
            case ADOPT -> {
                if (player.isHasBaby()) {
                    player.sendMessage(msgs.ALREADY_HAVE_BABY);
                    return;
                }
                player.sendMessage(msgs.ADOPT);
                rvt.makeBaby(player,null);
            }
            case DIVORCE -> {
                UUID partnerUUID = player.getPartner();
                if (partnerUUID == null) {
                    player.sendMessage("§cYou are not married!");
                    return;
                }
                player.divorce();
                RVTVillager partner = vm.getVillagers().get(partnerUUID);
                partner.divorce();
                partner.setMood(Mood.SAD,3);
                player.setHappiness(partner,-250);

                player.sendMessage(msgs.getDivorce(partner));
                Speech.DIVORCE.send(player,partner);
            }
            default -> {return;}
        }
        onClose();
    }

    public ItemStack getInfo(RVTPlayer p, RVTVillager villager) {
        String mood = villager.getDrunk() > 0 ? "Drunk" : villager.getMood().getLang(villager.getMoodLevel());
        String parent = vm.getVillagerName(villager.getParentType(),villager.getParent1());
        String partner = vm.getVillagerName(villager.getPartnerType(),villager.getPartner());

        String lore = "§7Name: §8"+villager.getName()
                +"\n§7Hearts: §8"+p.getHappiness(villager)
                +"\n§7Sex: §8"+villager.getGender().getLang()
                +"\n§7Trait: §8"+villager.getTrait().getLang()
                +"\n§7Mood: §8"+mood;
        if (parent != null) lore+="\n§7Child of: §8" + parent;
        if (partner != null) lore+="\n§7Married to: §8" + partner;
        return createMenuItem(Material.LIME_DYE, "§2Info", lore.split("\n"));
    }

}
