package io.github.tanguygab.realvillagertowns.villagers.enums.speeches;

import io.github.tanguygab.realvillagertowns.RealVillagerTowns;
import io.github.tanguygab.realvillagertowns.Utils;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.RVTVillager;
import io.github.tanguygab.realvillagertowns.villagers.enums.Interaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum BooleanSpeech {

    JOKE,
    CHAT,
    GREET,
    KISS(SpeechType.NORMAL,SpeechType.SPOUSE),
    STORY,
    FLIRT(SpeechType.NORMAL,SpeechType.SPOUSE),
    PROCREATE(true,SpeechType.NORMAL,SpeechType.SPOUSE),
    AID(true,SpeechType.NORMAL),
    MARRY(true,SpeechType.NORMAL),
    PLAY(SpeechType.CHILD);

    private final Map<SpeechType, List<String>> good = new HashMap<>();
    private final Map<SpeechType,List<String>> bad = new HashMap<>();

    BooleanSpeech(SpeechType... types) {
        this(false,types);
    }
    BooleanSpeech(boolean yesNo, SpeechType... types) {
        if (types.length == 0) types = new SpeechType[]{SpeechType.NORMAL, SpeechType.SPOUSE, SpeechType.CHILD};
        String speech = this.toString().toLowerCase();
        String suffixGood = yesNo ? "yes" : "good";
        String suffixBad = yesNo ? "no" : "bad";
        for (SpeechType type : types) {
            String t = type.toString().toLowerCase();
            good.put(type, RealVillagerTowns.getInstance().getSpeeches().getStringList(speech+"-"+suffixGood+"."+t));
            bad.put(type, RealVillagerTowns.getInstance().getSpeeches().getStringList(speech+"-"+suffixBad+"."+t));
        }
    }

    public static BooleanSpeech fromInteraction(Interaction interaction) {
        return switch (interaction) {
            case CHAT -> CHAT;
            case JOKE -> JOKE;
            case PLAY -> PLAY;
            case STORY -> STORY;
            default -> null;
        };
    }

    public void sendGood(RVTPlayer player, RVTVillager villager) {
        send(player,villager,good);
    }
    public void sendBad(RVTPlayer player, RVTVillager villager) {
        send(player,villager,bad);
    }

    private void send(RVTPlayer player, RVTVillager villager, Map<SpeechType,List<String>> map) {
        player.sendMessage(Utils.randomFromList(map.get(SpeechType.get(player,villager)))
                .replace("<player>", player.getName())
                .replace("<parent>", player.getGender().getParent())
                .replace("<parent2>", villager.getGender().getParent())
                .replace("<gender>", villager.getGender().getChild())
        );
    }

}
