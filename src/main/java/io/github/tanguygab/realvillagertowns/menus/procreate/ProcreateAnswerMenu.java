package io.github.tanguygab.realvillagertowns.menus.procreate;

import io.github.tanguygab.realvillagertowns.menus.AskMenu;
import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import org.bukkit.entity.Villager;

public class ProcreateAnswerMenu extends AskMenu {

    public ProcreateAnswerMenu(RVTPlayer proposer, RVTPlayer target) {
        super(target,proposer,"Procreate?","procreate with "+proposer.getName());
    }

    @Override
    protected void onAccept() {
        if (player.isHasBaby()) {
            player.sendMessage(msgs.ALREADY_HAVE_BABY);
            return;
        }
        String msg = msgs.getBaby(player2);
        player.sendMessage(msg);
        player2.sendMessage(msg);

        Villager baby = player.getPlayer().getWorld().spawn(player.getPlayer().getLocation(), Villager.class);
        baby.setBaby();
        player.setBaby(baby.getUniqueId());
        player2.getChildren().add(baby.getUniqueId());
    }

    @Override
    protected void onDecline() {
        player2.sendMessage(player.getName() + " declined your request.");
    }
}
