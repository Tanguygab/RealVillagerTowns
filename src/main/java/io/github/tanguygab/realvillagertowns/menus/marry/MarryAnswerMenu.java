package io.github.tanguygab.realvillagertowns.menus.marry;

import io.github.tanguygab.realvillagertowns.menus.AskMenu;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;

public class MarryAnswerMenu extends AskMenu {

    public MarryAnswerMenu(RVTPlayer proposer, RVTPlayer target) {
        super(target,proposer,"Accept proposal?","marry "+proposer.getName());
    }

    @Override
    protected void onAccept() {
        rvt.playerMarry(player,player2);
    }

    @Override
    protected void onDecline() {
        player2.sendMessage(player.getName() + " declined your request.");
    }
}
