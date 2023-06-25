package io.github.tanguygab.realvillagertowns.menus.procreate;

import io.github.tanguygab.realvillagertowns.menus.AskMenu;
import org.bukkit.entity.Player;

public class ProcreateMenu extends AskMenu {

    public ProcreateMenu(Player proposer, Player target) {
        super(target,proposer,"Ask to procreate?","ask "+proposer.getName()+" to procreate");
    }

    @Override
    protected void onAccept() {
        new ProcreateAnswerMenu(player,player2).onOpen();
    }

    @Override
    protected void onDecline() {}
}
