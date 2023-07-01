package io.github.tanguygab.realvillagertowns.villagers.enums;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;
import lombok.Getter;

import java.util.function.BiFunction;


public enum Interaction {
    GIFT,
    CHAT,
    JOKE,
    GREET,
    INSULT,
    STORY,

    FLIRT((p,v)->!p.isChild(v)),
    KISS((p,v)->!p.isChild(v) && p.likes(v) && (p.getHappiness(v) > 50 || v.getDrunk() >= 1)),
    PLAY((p,v)->v.isBaby()),
    PROCREATE((p,v)->p.likes(v) && (p.getHappiness(v) > 200 || p.isMarried(v) || v.getDrunk() >= 3)),
    FOLLOW((p,v)->(p.getHappiness(v) > 100 || p.isChild(v) || p.isBaby(v) || p.isMarried(v)) && v.getFollowed() == p),
    STOP_FOLLOW((p,v)->(p.getHappiness(v) > 100 || p.isChild(v) || p.isBaby(v) || p.isMarried(v)) && v.getFollowed() != p),
    MOVE((p,v)->(p.getHappiness(v) > 100 || p.isChild(v) || p.isBaby(v) || p.isMarried(v)) && v.isStaying()),
    STAY((p,v)->(p.getHappiness(v) > 100 || p.isChild(v) || p.isBaby(v) || p.isMarried(v)) && !v.isStaying());

    @Getter private final String lang;
    private final BiFunction<RVTPlayer,RVTVillager,Boolean> condition;

    Interaction() {
        this(null);
    }
    Interaction(BiFunction<RVTPlayer,RVTVillager,Boolean> condition) {
        lang = RealVillagerTowns.getInstance().getLang().getString("interactions."+this.toString().toLowerCase().replace("_","-"));
        this.condition = condition;
    }

    public boolean meetsCondition(RVTPlayer player, RVTVillager villager) {
        return condition == null || condition.apply(player,villager);
    }

}
