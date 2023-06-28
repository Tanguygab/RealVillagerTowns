package io.github.tanguygab.realvillagertowns.villagers.enums;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import lombok.Getter;

public enum Trait {

    SHY,
    FUN,
    SERIOUS,
    FRIENDLY,
    IRRITABLE,
    EMOTIONAL,
    OUTGOING;

    @Getter private final String lang;

    Trait() {
        lang = RealVillagerTowns.getInstance().getLang().getString("traits."+this);
    }

}
