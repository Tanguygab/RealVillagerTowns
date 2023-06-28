package io.github.tanguygab.realvillagertowns;

import io.github.tanguygab.realvillagertowns.villagers.enums.Interaction;
import io.github.tanguygab.realvillagertowns.villagers.enums.Mood;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;
import io.github.tanguygab.realvillagertowns.villagers.enums.Trait;
import lombok.NonNull;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

public class Interact {

    private final RealVillagerTowns rvt;

    public Interact(RealVillagerTowns rvt) {
        this.rvt = rvt;
    }

    public void interact(RVTPlayer player, RVTVillager villager, Interaction interaction) {
        int hearts = player.getHappiness(villager);
        int min = 1;
        int max = rvt.getConfiguration().VILLAGER_HAPPINESS_LEVEL;
        if (villager.getEntity() instanceof Villager v && v.getProfession() == Villager.Profession.NITWIT) max -= 5;
        if (villager.getDrunk() > 0) max -= Utils.random(1, 3);

        Mood mood = villager.getMood();
        int moodLevel = villager.getMoodLevel();
        switch (mood) {
            case HAPPY -> {
                moodLevel += 4;
                max -= moodLevel;
            }
            case NEUTRAL -> {
                moodLevel +=3;
                max -= moodLevel;
            }
            case SAD -> {
                moodLevel /= 2;
                max += moodLevel;
            }
            case ANGRY -> max += moodLevel;
            case FATIGUED -> {
                moodLevel++;
                max -= moodLevel;
            }
        }
        Trait trait = villager.getTrait();
        if (player.updateLastInteraction(interaction)) max += 6;
        switch (interaction) {
            case STORY,CHAT,JOKE,PLAY -> play(player,villager,interaction,trait,max,min,hearts);
            case GREET -> greet(player,villager,trait,max,min,hearts);
            case FLIRT -> flirt(player,villager,trait,max,min,hearts);
            case KISS -> kiss(player,villager,trait,max,min,hearts);
            case INSULT -> insult(player,villager);
            case FOLLOW -> follow(player,villager);
            case STOP_FOLLOW -> stopFollow(villager);
            case STAY -> villager.setStaying(true);
            case MOVE -> villager.setStaying(false);
        }
    }

    private void play(RVTPlayer player, RVTVillager villager, Interaction interaction, Trait trait, int max, int min, int hearts) {
        if (trait == Trait.SHY) max++;
        if (trait == Trait.FUN) {
            if (interaction == Interaction.JOKE || interaction == Interaction.PLAY) max -= 2;
            else max--;
        }

        if (trait == Trait.OUTGOING) max++;
        if (hearts > 20) max--;
        if (hearts > 50) max--;
        if (hearts > 80) max--;
        if (hearts > 150) max--;
        if (max < 1) max = 1;
        int r = Utils.random(max - min + 1) + min;
        if (r == 1) {
            player.speech(interaction + "-good", villager);
            player.setHappiness(villager, Utils.random(1, 10));
            return;
        }
        player.speech(interaction + "-bad", villager);
        player.setHappiness(villager, Utils.random(-10, -1));
    }
    private void greet(RVTPlayer player, RVTVillager villager, Trait trait, int max, int min, int hearts) {
        if (trait == Trait.SHY) max++;
        if (trait == Trait.FRIENDLY) max -= 2;
        if (trait == Trait.FUN) max++;
        if (trait == Trait.OUTGOING) max++;
        if (trait == Trait.SERIOUS) max++;
        if (hearts > 10) max--;
        if (hearts > 30) max--;
        if (hearts > 50) max--;
        if (hearts > 100) max--;
        if (hearts > 300) max--;
        if (max < 1) max = 1;
        int r = Utils.random(max - min + 1) + min;
        if (r == 1) {
            player.speech("greet-good", villager);
            player.setHappiness(villager, Utils.random(1, 6));
            return;
        }
        player.speech("greet-bad", villager);
        player.setHappiness(villager, Utils.random(-6, -1));
    }
    private void flirt(RVTPlayer player, RVTVillager villager, Trait trait, int max, int min, int hearts) {
        if (villager.getLikes() == player.getUniqueId()) max -= 4;
        if (trait == Trait.SHY && !player.isMarried(villager)) max += 2;
        if (trait == Trait.IRRITABLE) max++;
        if (trait == Trait.EMOTIONAL) max += Utils.random(-1, 1);
        if (trait == Trait.OUTGOING) max--;
        if (trait == Trait.SERIOUS) max++;
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
            player.speech("flirt-good", villager);
            player.setHappiness(villager, Utils.random(2, 10));
            int swing = Utils.random(-8, 1);
            if (swing < 0)
                swing = 0;
            villager.swingMood(swing);
            player.getLikes().add(villager.getUniqueId());
            return;
        }
        player.speech("flirt-bad", villager);
        player.setHappiness(villager, Utils.random(-10, -2));
        int swing = Utils.random(-8, 1);
        swing *= -1;
        villager.swingMood(Math.max(swing,0));
    }
    private void kiss(RVTPlayer player, RVTVillager villager, Trait trait, int max, int min, int hearts) {
        if (villager.getLikes() == player.getUniqueId()) max -= 2;
        if (trait == Trait.SHY && !player.isMarried(villager)) max += 3;
        if (trait == Trait.IRRITABLE) max += 2;
        if (trait == Trait.EMOTIONAL) max += Utils.random(-3, 3);
        if (trait == Trait.OUTGOING) max += Utils.random(-1, 3);
        if (trait == Trait.SERIOUS && hearts < 50) max++;
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
        if (player.isMarried(villager)) max -= 3;
        if (max < 1) max = 1;
        int r = Utils.random(max - min + 1) + min;
        if (r == 1) {
            player.speech("kiss-good", villager);
            player.setHappiness(villager, Utils.random(5, 15));
            int swing = Utils.random(-5, 1);
            villager.swingMood(Math.max(swing, 0));
            return;
        }
        player.speech("kiss-bad", villager);
        player.setHappiness(villager, Utils.random(-15, -5));
        int swing = Math.max(Utils.random(-5, 1),0);
        swing *= -1;
        villager.swingMood(swing);
    }
    private void insult(RVTPlayer player, RVTVillager villager) {
        player.speech("insult", villager);
        player.setHappiness(villager, Utils.random(-15, -5));
    }

    @SuppressWarnings("deprecation")
    private void follow(@NonNull RVTPlayer player, RVTVillager villager) {
        player.speech("follow", villager);
        villager.setFollowed(player);
        LivingEntity v = villager.getEntity();
        Wolf wolf = v.getWorld().spawn(v.getLocation(), Wolf.class);
        wolf.setOwner(player.getPlayer());
        wolf.addPassenger(v);
        wolf.setHealth(v.getHealth());
        MobDisguise mobDisguise = new MobDisguise(DisguiseType.HORSE, !villager.isBaby());
        DisguiseAPI.disguiseToAll(wolf, mobDisguise);
        wolf.setVelocity(new Vector(0.0D, 0.1D, 0.0D));
    }
    private void stopFollow(RVTVillager villager) {
        for (Entity near : villager.getEntity().getNearbyEntities(5.0D, 5.0D, 5.0D))
            if (!near.getPassengers().isEmpty() && near.getPassengers().get(0) == villager.getEntity()) {
                near.remove();
                villager.stopFollow();
                return;
            }
    }
}
