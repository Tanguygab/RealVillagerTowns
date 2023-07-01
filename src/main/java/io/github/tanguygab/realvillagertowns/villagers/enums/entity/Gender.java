package io.github.tanguygab.realvillagertowns.villagers.enums.entity;

import lombok.Getter;

public enum Gender {

    MALE("Male","daddy","boy"),
    FEMALE("Female","mommy","girl");

    @Getter private final String lang;
    @Getter private final String parent;
    @Getter private final String child;

    Gender(String lang, String parent, String child) {
        this.lang = lang;
        this.parent = parent;
        this.child = child;
    }
}
