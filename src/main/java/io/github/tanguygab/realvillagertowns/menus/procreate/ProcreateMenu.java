package io.github.tanguygab.realvillagertowns.menus.procreate;

import io.github.tanguygab.realvillagertowns.menus.AskMenu;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;

public class ProcreateMenu extends AskMenu {

    public ProcreateMenu(RVTPlayer proposer, RVTPlayer target) {
        super(target,proposer,"Ask to procreate?","ask "+proposer.getName()+" to procreate");
    }

    @Override
    protected void onAccept() {
        new ProcreateAnswerMenu(player,player2).onOpen();
    }

    @Override
    protected void onDecline() {}
}
