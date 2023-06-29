package io.github.tanguygab.realvillagertowns.villagers.enums.speeches;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.Utils;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;
import io.github.tanguygab.realvillagertowns.villagers.enums.GiftType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Speech {

    GIFT_BAD,
    GIFT_SMALL,
    GIFT_REGULAR,
    GIFT_GREAT,
    GIFT_LOVE,

    MARRY_CANT(SpeechType.NORMAL),
    DIVORCE(SpeechType.SPOUSE),

    DRUNK(SpeechType.NORMAL),
    PUNCH,
    INSULT,
    FOLLOW;

    private final Map<SpeechType,List<String>> messages = new HashMap<>();

    Speech(SpeechType... types) {
        if (types.length == 0) types = new SpeechType[]{SpeechType.NORMAL, SpeechType.SPOUSE, SpeechType.CHILD};
        String speech = this.toString().toLowerCase();
        for (SpeechType type : types)
            messages.put(type,RealVillagerTowns.getInstance().getSpeeches().getStringList(speech+"."+type.toString().toLowerCase()));
    }

    public static Speech fromGift(GiftType giftType) {
        return switch (giftType) {
            case BAD -> GIFT_BAD;
            case SMALL -> GIFT_SMALL;
            case REGULAR -> GIFT_REGULAR;
            case GREAT -> GIFT_GREAT;
            case LOVE -> GIFT_LOVE;
            default -> null;
        };
    }

    public void send(RVTPlayer player, RVTVillager villager) {
        player.sendMessage(Utils.randomFromList(messages.get(SpeechType.get(player,villager)))
                .replace("<player>", player.getName())
                .replace("<parent>", player.getGender().getParent())
                .replace("<parent2>", villager.getGender().getParent())
                .replace("<gender>", villager.getGender().getChild())
        );

    }

}
