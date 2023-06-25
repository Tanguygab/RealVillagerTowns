package io.github.tanguygab.realvillagertowns;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Interact {

    private final RealVillagerTowns rvt;

    public Interact(RealVillagerTowns rvt) {
        this.rvt = rvt;
    }

    public void interact(Player p, LivingEntity v, String type) {
        int hearts = rvt.data.getInt("players." + p.getUniqueId() + ".happiness." + v.getUniqueId(),0);
        int min = 1;
        int max = rvt.getConfig().getInt("villagerHappinessLevel");
        if (v instanceof Villager villager && villager.getProfession() == Villager.Profession.NITWIT) max -= 5;
        if (rvt.drunkMap.get(v.getUniqueId()) != null) max -= Utils.random(1, 3);
        boolean likes = p.getUniqueId().toString().equals(rvt.data.getString("villagers." + v.getUniqueId() + ".likes"));

        String mood = rvt.getMood(v.getUniqueId());
        int tmp = Integer.parseInt(mood.substring(mood.length()-1));
        mood = mood.substring(0,mood.length()-1);
        switch (mood) {
            case "happy" -> {
                tmp += 4;
                max -= tmp;
            }
            case "neutral" -> {
                tmp+=3;
                max -= tmp;
            }
            case "sadness" -> {
                tmp /= 2;
                max += tmp;
            }
            case "anger" -> max += tmp;
            case "fatigue" -> {
                tmp++;
                max -= tmp;
            }
        }
        String trait = rvt.data.getString("villagers." + v.getUniqueId() + ".trait");
        if (rvt.sameCheck(p, type)) max += 6;
        switch (type) {
            case "story","chat","joke","play" -> play(p,v,type,trait,max,min,hearts);
            case "greet" -> greet(p,v,trait,max,min,hearts);
            case "flirt" -> flirt(p,v,likes,trait,max,min,hearts);
            case "kiss" -> kiss(p,v,likes,trait,max,min,hearts);
            case "insult" -> insult(p,v);
            case "follow" -> follow(p,v);
            case "stopFollow" -> stopFollow(v);
            case "stay" -> v.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 998001, 257));
            case "move" -> v.removePotionEffect(PotionEffectType.SLOW);
        }
    }

    private void play(Player p, LivingEntity v, String type, String trait, int max, int min, int hearts) {
        if (trait.equals("Shy")) max++;
        if (trait.equals("Fun") && (type.equals("joke") || type.equals("play"))) max -= 2;
        else if (trait.equals("Fun")) max--;

        if (trait.equals("Outgoing")) max++;
        if (hearts > 20) max--;
        if (hearts > 50) max--;
        if (hearts > 80) max--;
        if (hearts > 150) max--;
        if (max < 1) max = 1;
        int r = Utils.random(max - min + 1) + min;
        if (r == 1) {
            p.sendMessage(rvt.getSpeech(type + "-good", p, v));
            rvt.changeHearts(p, v.getUniqueId(), Utils.random(1, 10));
            return;
        }
        p.sendMessage(rvt.getSpeech(type + "-bad", p, v));
        rvt.changeHearts(p, v.getUniqueId(), Utils.random(-10, -1));
    }
    private void greet(Player p, LivingEntity v, String trait, int max, int min, int hearts) {
        if (trait.equals("Shy")) max++;
        if (trait.equals("Friendly")) max -= 2;
        if (trait.equals("Fun")) max++;
        if (trait.equals("Outgoing")) max++;
        if (trait.equals("Serious")) max++;
        if (hearts > 10) max--;
        if (hearts > 30) max--;
        if (hearts > 50) max--;
        if (hearts > 100) max--;
        if (hearts > 300) max--;
        if (max < 1) max = 1;
        int r = Utils.random(max - min + 1) + min;
        if (r == 1) {
            p.sendMessage(rvt.getSpeech("greet-good", p, v));
            rvt.changeHearts(p, v.getUniqueId(), Utils.random(1, 6));
            return;
        }
        p.sendMessage(rvt.getSpeech("greet-bad", p, v));
        rvt.changeHearts(p, v.getUniqueId(), Utils.random(-6, -1));
    }
    private void flirt(Player p, LivingEntity v, boolean likes, String trait, int max, int min, int hearts) {
        if (likes) max -= 4;
        if (trait.equals("Shy") && !rvt.isMarried(p, v)) max += 2;
        if (trait.equals("Irritable")) max++;
        if (trait.equals("Emotional")) max += Utils.random(-1, 1);
        if (trait.equals("Outgoing")) max--;
        if (trait.equals("Serious")) max++;
        if (hearts < -10) max++;
        if (hearts < -30) max++;
        if (hearts < -50) max++;
        if (hearts > 30) max--;
        if (hearts > 50) max--;
        if (hearts > 80) max--;
        if (hearts > 100) max--;
        if (hearts > 300) max--;
        if (max < 1) max = 1;
        int r = Utils.random(max - min + 1) + min;
        if (r == 1) {
            p.sendMessage(rvt.getSpeech("flirt-good", p, v));
            rvt.changeHearts(p, v.getUniqueId(), Utils.random(2, 10));
            int swing = Utils.random(-8, 1);
            if (swing < 0)
                swing = 0;
            rvt.moodSwing(v, swing);
            rvt.setLikes(p, v);
            return;
        }
        p.sendMessage(rvt.getSpeech("flirt-bad", p, v));
        rvt.changeHearts(p, v.getUniqueId(), Utils.random(-10, -2));
        int swing = Utils.random(-8, 1);
        swing *= -1;
        rvt.moodSwing(v, Math.max(swing,0));
    }
    private void kiss(Player p, LivingEntity v, boolean likes, String trait, int max, int min, int hearts) {
        if (likes) max -= 2;
        if (trait.equals("Shy") && !rvt.isMarried(p, v)) max += 3;
        if (trait.equals("Irritable")) max += 2;
        if (trait.equals("Emotional")) max += Utils.random(-3, 3);
        if (trait.equals("Outgoing")) max += Utils.random(-1, 3);
        if (trait.equals("Serious") && hearts < 50) max++;
        if (hearts < 30) max++;
        if (hearts < 20) max += 2;
        if (hearts < 10) max += 3;
        if (hearts < -5) max -= 2;
        if (hearts < -20) max += 6;
        if (hearts > 50) max--;
        if (hearts > 80) max--;
        if (hearts > 100) max--;
        if (hearts > 250) max--;
        if (hearts > 400)max--;
        if (rvt.isMarried(p, v)) max -= 3;
        if (max < 1) max = 1;
        int r = Utils.random(max - min + 1) + min;
        if (r == 1) {
            p.sendMessage(rvt.getSpeech("kiss-good", p, v));
            rvt.changeHearts(p, v.getUniqueId(), Utils.random(5, 15));
            int swing = Utils.random(-5, 1);
            rvt.moodSwing(v, Math.max(swing, 0));
            return;
        }
        p.sendMessage(rvt.getSpeech("kiss-bad", p, v));
        rvt.changeHearts(p, v.getUniqueId(), Utils.random(-15, -5));
        int swing = Math.max(Utils.random(-5, 1),0);
        swing *= -1;
        rvt.moodSwing(v, swing);
    }
    private void insult(Player p, LivingEntity v) {
        p.sendMessage(rvt.getSpeech("insult", p, v));
        rvt.changeHearts(p, v.getUniqueId(), Utils.random(-15, -5));
    }
    private void follow(Player p, LivingEntity v) {
        p.sendMessage(rvt.getSpeech("follow", p, v));
        rvt.followMap.put(v, p);
        Wolf w = (Wolf)v.getWorld().spawnEntity(v.getLocation(), EntityType.WOLF);
        w.setOwner(p);
        w.addPassenger(v);
        w.setHealth(v.getHealth());
        MobDisguise mobDisguise = new MobDisguise(DisguiseType.HORSE, !rvt.isBaby(v));
        DisguiseAPI.disguiseToAll(w, mobDisguise);
        w.setVelocity(new Vector(0.0D, 0.1D, 0.0D));
    }
    private void stopFollow(LivingEntity v) {
        for (Entity near : v.getNearbyEntities(5.0D, 5.0D, 5.0D))
            if (!near.getPassengers().isEmpty() && near.getPassengers().get(0).equals(v)) {
                near.remove();
                rvt.stopFollow(v);
            }
    }
}
