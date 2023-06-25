package io.github.tanguygab.realvillagertowns.villagers;

import lombok.Data;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.*;

@Data
public class RVTPlayer {

    private final Player player;
    private final UUID uniqueId;
    private final String name;

    @NonNull private Gender gender;

    private RVTEntityType partnerType;
    private UUID partner;
    private List<UUID> children = new ArrayList<>();
    private boolean hasBaby;
    private UUID baby;

    private List<UUID> likes = new ArrayList<>();
    private Map<UUID,Integer> happiness = new HashMap<>();

}
