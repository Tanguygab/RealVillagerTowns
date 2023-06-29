package io.github.tanguygab.realvillagertowns.villagers.enums.entity;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.Utils;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import lombok.Getter;

import java.util.List;

public enum Trait {

    SHY,
    FUN,
    SERIOUS,
    FRIENDLY,
    IRRITABLE,
    EMOTIONAL,
    OUTGOING;

    @Getter private final String lang;
    private final List<String> speech;

    Trait() {
        lang = RealVillagerTowns.getInstance().getLang().getString("traits."+this);
        speech = RealVillagerTowns.getInstance().getSpeeches().getStringList("like."+this.toString().toLowerCase());
    }

    public void send(RVTPlayer player) {
        Gender gender = player.getGender();
        player.sendMessage(Utils.randomFromList(speech).replace("<gender>", gender.getChild())
                .replace("<nice>", gender == Gender.MALE ? "handsome" : "beautiful"));
    }
}
