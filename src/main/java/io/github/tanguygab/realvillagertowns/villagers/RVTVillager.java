package io.github.tanguygab.realvillagertowns.villagers;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.Utils;
import lombok.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class RVTVillager {

    private final LivingEntity entity;
    private final UUID uniqueId;

    @NonNull private String name;
    @NonNull private Gender gender;
    @NonNull @Setter private String skin;
    @NonNull private String title;
    @NonNull private String trait;

    private RVTEntityType parentType;
    private UUID parent1;
    private UUID parent2;

    private RVTEntityType partnerType;
    private UUID partner;
    private final List<UUID> children = new ArrayList<>();

    @Setter private Location home;
    @Setter private UUID likes;
    @Setter private Material inHand;
    @Setter private int drunk = 0;
    private Mood mood;
    private int moodLevel = 0;
    @Setter private RVTPlayer followed;

    public void setParent(RVTEntityType parentType, UUID parent1, UUID parent2) {
        this.parentType = parentType;
        this.parent1 = parent1;
        this.parent2 = parent2;
    }
    public void marry(UUID partner, RVTEntityType partnerType) {
        this.partnerType = partnerType;
        this.partner = partner;
    }
    public void divorce() {
        marry(null,null);
    }

    public void setHome() {
        home = entity.getLocation();
    }
    public void setMood(Mood mood, int level) {
        this.mood = mood;
        moodLevel = level;
    }

    public void swingMood(int amount) {
        if (mood == Mood.SAD || mood == Mood.ANGRY || mood == Mood.FATIGUED) amount *= -1;
        switch (moodLevel) {
            case 1,5 -> amount++;
            case 2 -> amount+=2;
            case 3 -> amount+=3;
            case 4 -> amount+=4;
        }
        setMood(mood, amount);
    }

    public void setStaying(boolean stay) {
        if (stay) entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 998001, 257));
        else entity.removePotionEffect(PotionEffectType.SLOW);
    }
    public boolean isStaying() {
        return entity.getActivePotionEffects().stream().anyMatch(e->e.getType() == PotionEffectType.SLOW);
    }

    public void jump() {
        Location l = entity.getLocation();
        l.setY(l.getY() + 1.5D);
        Utils.displayParticle(Particle.HEART, l, 0.3D, 1, 3);
        Vector v = new Vector();
        v.setY(0.3D);
        entity.setVelocity(v);
    }

    public void updateMood() {
        if (Utils.random(1, 40) != 1) return;
        int r = Utils.random(1, 5);
        setMood(switch (r) {
            case 1 -> Mood.HAPPY;
            case 2 -> Mood.NEUTRAL;
            case 3 -> Mood.SAD;
            case 4 -> Mood.ANGRY;
            default -> Mood.FATIGUED;
        },1);
    }

    public void stopFollow() {
        RealVillagerTowns.getInstance().getVillagerManager().disguise(this);
        entity.removePotionEffect(PotionEffectType.INVISIBILITY);
        followed = null;
    }

    public boolean isBaby() {
        return entity instanceof Ageable ageable && !ageable.isAdult();
    }
}
