package io.github.tanguygab.realvillagertowns.villagers;

import lombok.Data;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class RVTVillager {

    private final LivingEntity entity;
    private final UUID uniqueId;

    @NonNull private String name;
    @NonNull private Gender gender;
    @NonNull private String skin;
    @NonNull private String title;
    @NonNull private String trait;

    private RVTEntityType parentType;
    private UUID parent1;
    private UUID parent2;

    private RVTEntityType partnerType;
    private UUID partner;
    private List<UUID> children = new ArrayList<>();

    private Location home;
    private UUID likes;
    private Material inHand;
    private int drunk;
    private Mood mood;
    private int moodLevel;
    private Player followed;

    public void setMood(Mood mood, int level) {
        this.mood = mood;
        moodLevel = level;
    }

}
