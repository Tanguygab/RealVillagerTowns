package io.github.tanguygab.realvillagertowns.menus.marry;

import io.github.tanguygab.realvillagertowns.menus.AskMenu;
import org.bukkit.entity.Player;

public class MarryMenu extends AskMenu {

    public MarryMenu(Player proposer, Player target) {
        super(proposer,target,"Ask to marry?","ask "+target.getName()+" to marry me");
    }

    @Override
    protected void onAccept() {
        new MarryAnswerMenu(player,player2).onOpen();
    }

    @Override
    protected void onDecline() {}
}
