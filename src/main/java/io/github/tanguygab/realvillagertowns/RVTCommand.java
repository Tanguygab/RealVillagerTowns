package io.github.tanguygab.realvillagertowns;

import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public class RVTCommand implements CommandExecutor {

    private final RealVillagerTowns rvt;

    public RVTCommand(RealVillagerTowns rvt) {
        this.rvt = rvt;
    }

    private void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&4RVT: "+msg));
    }

    private boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("rvt.op.commands");
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, Command command, @NonNull String label, @NonNull String[] args) {
        if (command.getName().equals("bringkids")) {
            if (sender instanceof Player p) rvt.getKids(p);
            else sendMessage(sender,"&cThis command can only be run by a player!");
            return true;
        }

        if (command.getName().equals("realvillagertowns")) process(sender,args);
        return true;
    }

    public void process(CommandSender sender, String[] args) {
        if (args.length == 0) {
            helpMsg(sender);
            return;
        }
        String arg = args[0].toLowerCase();
        if (arg.equals("reload") && hasPermission(sender)) {
            rvt.loadFiles();
            sendMessage(sender,"&eReloaded config!");
            return;
        }
        if (!(sender instanceof Player p)) {
            sendMessage(sender,"&cThis command can only be run by a player!");
            return;
        }
        if (arg.equals("sex")) {
            if (args.length < 2) {
                helpMsg(sender);
                return;
            }
            if (!args[1].equalsIgnoreCase("male") &&  !args[1].equalsIgnoreCase("female")) {
                sendMessage(sender,"&cUnrecognized sex " + args[1]);
                return;
            }
            sendMessage(sender,"&eYour sex has been set to " + args[1]);
            rvt.set("players." + p.getUniqueId() + ".sex", args[1]);
            return;
        }
        if (!hasPermission(sender)) {
            helpMsg(sender);
            return;
        }
        switch (arg) {
            case "skin" -> {
                if (Utils.getTarget(p) instanceof LivingEntity v && rvt.entEnabled(v)) {
                    String skin = rvt.data.getString("villagers." + v.getUniqueId() + ".skin");
                    sendMessage(sender, "&eThat villagers skin is: " + skin);
                } else sendMessage(sender, "&cYou're not looking at a villager!");
            }
            case "setvillager" -> {
                if (rvt.getConfig().getBoolean("autoChangeLivingEntitys")) {
                    sendMessage(sender, "&cLivingEntities automatically change!");
                    return;
                }
                if (Utils.getTarget(p) instanceof LivingEntity v && rvt.entEnabled(v)) {
                    rvt.set("villagers." + v.getUniqueId() + ".name", "villager");
                    rvt.getVillagerManager().makeVillager(v);
                    sendMessage(sender, "&eLivingEntity changed to RVT version!");
                } else sendMessage(sender, "&cYou're not looking at a villager!");
            }
            case "likes" -> sendMessage(sender,"&e" + rvt.likeMap.get(p.getName()).size() + " villagers like you!");
            default -> {
                if (args.length < 2) {
                    helpMsg(sender);
                    return;
                }
                switch (arg) {
                    case "fixskin" -> {
                        if (Utils.getTarget(p) instanceof LivingEntity v && rvt.entEnabled(v)) {
                            String newSkin = args[1];
                            String oldSkin = rvt.data.getString("villagers." + v.getUniqueId() + ".skin");
                            rvt.replaceSkin(newSkin, oldSkin);
                            sendMessage(sender, "&cSkin " + oldSkin + " replaced with " + newSkin);
                        } else sendMessage(sender, "&cYou're not looking at a villager!");
                    }
                    case "like" -> {
                        Player player;
                        if ((player = rvt.getServer().getPlayer(args[1])) == null) {
                            sendMessage(sender,"&cCan't find that player!");
                            return;
                        }

                        if (Utils.getTarget(p) instanceof LivingEntity v && rvt.entEnabled(v)) {
                            rvt.like(player, v);
                            sendMessage(sender,"&eThat villager now likes " + player.getName() + "!");
                        } else sendMessage(sender,"&cYou're not looking at a villager!");
                    }
                    case "clearbaby" -> {
                        Player player;
                        if ((player = rvt.getServer().getPlayer(args[1])) == null) {
                            sendMessage(sender,"&cCan't find that player!");
                            return;
                        }
                        rvt.set("players." + player.getUniqueId() + ".baby", null);
                        rvt.set("players." + player.getUniqueId() + ".hasBaby", false);
                        sendMessage(sender,"&eCleared " + player.getName() + "'s baby.");
                    }
                    case "setprofession" -> {
                        if (!(Utils.getTarget(p) instanceof Villager v)) {
                            sendMessage(sender,"&cYou're not looking at a villager!");
                            return;
                        }
                        try {
                            v.setProfession(Villager.Profession.valueOf(args[1].toUpperCase()));
                            sendMessage(sender,"&eThat villagers profession has been set to: " + args[1]);
                        } catch (Exception e) {
                            sendMessage(sender,"&cThat is not a valid profession!");
                            sendMessage(sender,"&6Professions:");
                            for (Villager.Profession profession : Villager.Profession.values())
                                sendMessage(sender,"&e - " + profession.getKey().getKey());
                        }
                    }
                    default -> helpMsg(sender);
                }
            }
        }
    }

    private void helpMsg(CommandSender sender) {
        StringBuilder message = new StringBuilder("&6--- Real Villager Towns v" + rvt.getDescription().getVersion() + " ---&e");
        if (hasPermission(sender)) {
            message.append("\n/rvt fixskin <newPlayerName>");
            message.append("\n/rvt skin");
            message.append("\n/rvt reload");
            message.append("\n/rvt setvillager");
            message.append("\n/rvt like <playerName>");
            message.append("\n/rvt likes");
            message.append("\n/rvt clearbaby <playerName>");
            message.append("\n/rvt setprofession <profession>");
        }
        message.append("\n/rvt sex <male/female>");
        sendMessage(sender,message.toString());
    }

}
