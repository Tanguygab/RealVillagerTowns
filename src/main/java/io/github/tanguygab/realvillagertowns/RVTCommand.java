package io.github.tanguygab.realvillagertowns;

import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;
import io.github.tanguygab.realvillagertowns.villagers.VillagerManager;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.util.BlockIterator;

import java.util.List;
import java.util.Objects;

public class RVTCommand implements CommandExecutor {

    private final RealVillagerTowns rvt;
    private final VillagerManager vm;

    public RVTCommand(RealVillagerTowns rvt) {
        this.rvt = rvt;
        vm = rvt.getVillagerManager();
    }

    private void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&4RVT: "+msg));
    }

    private boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("rvt.op.commands");
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, Command command, @NonNull String label, String@NonNull[] args) {
        if (command.getName().equals("bringkids")) {
            if (sender instanceof Player p) {
                List<Entity> children = vm.getPlayer(p).getChildren().stream().map(uuid->rvt.getServer().getEntity(uuid)).filter(Objects::nonNull).toList();
                children.forEach(e->e.teleport(p));
                p.sendMessage(children.size() == 0 ? "§4RVT: §cNone of your children can be found!"
                        : "§4RVT: §eFound " + children.size() + " children and teleported them to you.");
            }
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
                if (getTarget(p) instanceof LivingEntity v && vm.isVillagerEntity(v)) {
                    String skin = vm.getVillager(v).getSkin();
                    sendMessage(sender, "&eThat villagers skin is: " + skin);
                } else sendMessage(sender, "&cYou're not looking at a villager!");
            }
            case "setvillager" -> {
                if (rvt.getConfig().getBoolean("autoChangeLivingEntitys")) {
                    sendMessage(sender, "&cLivingEntities automatically change!");
                    return;
                }
                if (getTarget(p) instanceof LivingEntity v && vm.isVillagerEntity(v)) {
                    vm.makeVillager(v);
                    sendMessage(sender, "&eLivingEntity changed to RVT version!");
                } else sendMessage(sender, "&cYou're not looking at a villager!");
            }
            case "likes" -> sendMessage(sender,"&e" + vm.getPlayer(p).getLikes().size() + " villagers like you!");
            default -> {
                if (args.length < 2) {
                    helpMsg(sender);
                    return;
                }
                switch (arg) {
                    case "fixskin" -> {
                        if (getTarget(p) instanceof LivingEntity v && vm.isVillagerEntity(v)) {
                            String newSkin = args[1];
                            RVTVillager villager = vm.getVillager(v);
                            String oldSkin = villager.getSkin();
                            villager.setSkin(newSkin);
                            vm.disguise(villager);
                            sendMessage(sender, "&cSkin " + oldSkin + " replaced with " + newSkin);
                        } else sendMessage(sender, "&cYou're not looking at a villager!");
                    }
                    case "like" -> {
                        Player player;
                        if ((player = rvt.getServer().getPlayer(args[1])) == null) {
                            sendMessage(sender,"&cCan't find that player!");
                            return;
                        }

                        if (getTarget(p) instanceof LivingEntity v && vm.isVillagerEntity(v)) {
                            rvt.like(vm.getPlayer(player), vm.getVillager(v));
                            sendMessage(sender,"&eThat villager now likes " + player.getName() + "!");
                        } else sendMessage(sender,"&cYou're not looking at a villager!");
                    }
                    case "clearbaby" -> {
                        RVTPlayer player;
                        if ((player = vm.getPlayer(rvt.getServer().getPlayer(args[1]))) == null) {
                            sendMessage(sender,"&cCan't find that player!");
                            return;
                        }
                        player.clearBaby();
                        sendMessage(sender,"&eCleared " + player.getName() + "'s baby.");
                    }
                    case "setprofession" -> {
                        if (!(getTarget(p) instanceof Villager v)) {
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

    private Entity getTarget(Player player) {
        BlockIterator iterator = new BlockIterator(player.getWorld(), player.getLocation().toVector(), player.getEyeLocation().getDirection(), 0.0D, 100);
        while (iterator.hasNext()) {
            Block item = iterator.next();
            for (Entity entity : player.getNearbyEntities(100.0D, 100.0D, 100.0D)) {
                if (entity instanceof LivingEntity && !entity.getType().equals(EntityType.BAT)) {
                    int acc = 2;
                    for (int x = -acc; x < acc; x++) {
                        for (int z = -acc; z < acc; z++) {
                            for (int y = -acc; y < acc; y++) {
                                if (entity.getLocation().getBlock()
                                        .getRelative(x, y, z).equals(item))
                                    return entity;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

}
