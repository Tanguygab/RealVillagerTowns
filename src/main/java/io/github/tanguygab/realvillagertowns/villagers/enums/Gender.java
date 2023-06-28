package io.github.tanguygab.realvillagertowns.villagers.enums;

import lombok.Getter;

public enum Gender {

    MALE("daddy","boy"),
    FEMALE("mommy","girl");

    @Getter private final String parent;
    @Getter private final String child;

    Gender(String parent, String child) {
        this.parent = parent;
        this.child = child;
    }
}
