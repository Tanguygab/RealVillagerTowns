package io.github.tanguygab.realvillagertowns.villagers;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.Utils;
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

    private String lastAction;
    private int lastActionTimes = 0;

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

    public void setLastAction(String action, int times) {
        lastAction = action;
        lastActionTimes = times;
    }
    public boolean updateLastAction(String type) {
        if (lastAction == null || !type.startsWith(lastAction)) {
            setLastAction(type,1);
            return false;
        }
        int times = lastActionTimes+1;
        setLastAction(type,times);
        return times >= RealVillagerTowns.getInstance().getConfig().getInt("maxSameInteraction");
    }

    public void sendMessage(String message) {
        player.sendMessage(Utils.colors(message));
    }
    public void speech(String msg, RVTVillager villager) {
        sendMessage(RealVillagerTowns.getInstance().getSpeech(msg,this,villager));
    }

}
