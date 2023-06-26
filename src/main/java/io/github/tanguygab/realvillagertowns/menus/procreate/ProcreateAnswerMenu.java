package io.github.tanguygab.realvillagertowns.menus.procreate;

import io.github.tanguygab.realvillagertowns.menus.AskMenu;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import org.bukkit.entity.Player;

public class ProcreateAnswerMenu extends AskMenu {

    public ProcreateAnswerMenu(RVTPlayer proposer, RVTPlayer target) {
        super(target,proposer,"Procreate?","procreate with "+proposer.getName());
    }

    @Override
    protected void onAccept() {
        rvt.playerProcreate(player,player2);
    }

    @Override
    protected void onDecline() {
        player2.sendMessage(player.getName() + " declined your request.");
    }
}
