package io.github.tanguygab.realvillagertowns.villagers.enums;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;

import java.util.List;

public enum Mood {

    HAPPY,
    NEUTRAL,
    SAD,
    ANGRY,
    FATIGUED;

    private final List<String> lang;

    Mood() {
        lang = RealVillagerTowns.getInstance().getLang().getStringList("mood."+this);
    }

    public String getLang(int moodLevel) {
        return lang.size() > moodLevel ? lang.get(moodLevel) : lang.get(0);
    }

}
