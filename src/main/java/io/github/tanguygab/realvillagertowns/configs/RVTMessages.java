package io.github.tanguygab.realvillagertowns.configs;

import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;
import org.bukkit.configuration.ConfigurationSection;

public class RVTMessages {

    public final String ADOPT;
    public final String ALREADY_HAVE_BABY;
    private final String DIVORCE;
    public final String DRUNK;
    private final String BABY;

    public RVTMessages(ConfigurationSection messages) {
        ADOPT = messages.getString("adopt","You have adopted a baby!");
        ALREADY_HAVE_BABY = messages.getString("already-have-baby","You already have a baby!");
        DIVORCE = messages.getString("divorce","You have divorced your spouse <villager>");
        DRUNK = messages.getString("drunk","I'm feeling a bit tipsy...");
        BABY = messages.getString("baby","You had a baby with <player2>!");
    }

    public String getDivorce(RVTVillager villager) {
        return DIVORCE.replace("<villager>",villager.getName());
    }

    public String getBaby(RVTPlayer player) {
        return BABY.replace("<player2>",player.getName());
    }

}
