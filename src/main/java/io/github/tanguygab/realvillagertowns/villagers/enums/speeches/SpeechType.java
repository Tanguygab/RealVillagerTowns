package io.github.tanguygab.realvillagertowns.villagers.enums.speeches;

import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;

public enum SpeechType {

    NORMAL, SPOUSE, CHILD;

    public static SpeechType get(RVTPlayer player, RVTVillager villager) {
        if (player.getChildren().contains(villager.getUniqueId()) && villager.isBaby()) return CHILD;
        if (player.getPartner() == villager.getUniqueId()) return SPOUSE;
        return NORMAL;
    }
}