package io.github.tanguygab.realvillagertowns.menus.marry;

import io.github.tanguygab.realvillagertowns.menus.AskMenu;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import io.github.tanguygab.realvillagertowns.villagers.enums.entity.RVTEntityType;

public class MarryAnswerMenu extends AskMenu {

    public MarryAnswerMenu(RVTPlayer proposer, RVTPlayer target) {
        super(target,proposer,"Accept proposal?","marry "+proposer.getName());
    }

    @Override
    protected void onAccept() {
        if (player.getPartner() != null) {
            player.sendMessage("You are already married!");
            return;
        }
        marry(player,player2);
        marry(player2,player);
    }

    private void marry(RVTPlayer p, RVTPlayer p2) {
        p.sendMessage("You have married " + p2.getName());
        p.marry(p2.getUniqueId(), RVTEntityType.PLAYER);
    }

    @Override
    protected void onDecline() {
        player2.sendMessage(player.getName() + " declined your request.");
    }
}
