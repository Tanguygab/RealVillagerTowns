package io.github.tanguygab.realvillagertowns.villagers.enums;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import lombok.Getter;

public enum Button {

    INTERACT,
    TRADE,
    SET_HOME,
    REQUEST_AID,
    ADOPT,
    DIVORCE,
    MARRY;

    @Getter private final String lang;

    Button() {
        lang = RealVillagerTowns.getInstance().getLang().getString("buttons."+this.toString().toLowerCase().replace("_","-"));
    }

    public static Button fromLang(String lang) {
        for (Button button : values())
            if (button.getLang().equals(lang))
                return button;
        return null;
    }
}
