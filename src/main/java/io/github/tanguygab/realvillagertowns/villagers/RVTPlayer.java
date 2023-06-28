package io.github.tanguygab.realvillagertowns.villagers;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.Utils;
import io.github.tanguygab.realvillagertowns.villagers.enums.Gender;
import io.github.tanguygab.realvillagertowns.villagers.enums.Interaction;
import io.github.tanguygab.realvillagertowns.villagers.enums.RVTEntityType;
import lombok.*;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Getter
public class RVTPlayer {

    private final Player player;
    private final UUID uniqueId;
    private final String name;

    @NonNull private Gender gender;

    private RVTEntityType partnerType;
    private UUID partner;
    private final List<UUID> children = new ArrayList<>();
    private boolean hasBaby = false;
    private UUID baby;

    private final List<UUID> likes = new ArrayList<>();
    private final Map<UUID,Integer> happiness = new HashMap<>();
    @Setter private LocalDateTime aidCooldown;

    @Setter private RVTVillager gifting;
    @Setter private boolean trading = false;
    @Setter private boolean interacting = false;

    private Interaction lastInteraction;
    private int lastInteractionTimes = 0;

    public boolean isMarried(RVTVillager entity) {
        return partner == entity.getUniqueId();
    }
    public void marry(UUID partner, RVTEntityType partnerType) {
        this.partnerType = partnerType;
        this.partner = partner;
    }
    public void divorce() {
        marry(null,null);
    }

    public boolean isChild(RVTVillager entity) {
        return children.contains(entity.getUniqueId());
    }
    public boolean isBaby(RVTVillager entity) {
        return baby == entity.getUniqueId();
    }
    public void setBaby(UUID uuid) {
        baby = uuid;
        hasBaby = true;
        if (!children.contains(uuid)) children.add(uuid);
    }
    public void clearBaby() {
        baby = null;
        hasBaby = false;
    }

    public boolean likes(RVTVillager villager) {
        return likes.contains(villager.getUniqueId());
    }
    public void setHappiness(RVTVillager villager, int hearts) {
        hearts += happiness.getOrDefault(villager.getUniqueId(),0);
        happiness.put(villager.getUniqueId(),hearts);
    }
    public int getHappiness(RVTVillager villager) {
        return happiness.getOrDefault(villager.getUniqueId(),0);
    }

    public void setLastInteraction(Interaction interaction, int times) {
        lastInteraction = interaction;
        lastInteractionTimes = times;
    }
    public boolean updateLastInteraction(Interaction interaction) {
        if (lastInteraction != interaction) {
            setLastInteraction(interaction,1);
            return false;
        }
        int times = lastInteractionTimes + 1;
        setLastInteraction(interaction,times);
        return RealVillagerTowns.getInstance().getConfiguration().canUpdateLastInteraction(times);
    }

    public void sendMessage(String message) {
        player.sendMessage(Utils.colors(message));
    }
    public void villagerMessage(RVTVillager villager, String message) {
        sendMessage(villager.getName()+": "+message);
    }
    public void speech(String msg, RVTVillager villager) {
        sendMessage(RealVillagerTowns.getInstance().getSpeech(msg,this,villager));
    }

}
