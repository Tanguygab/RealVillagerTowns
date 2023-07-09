package io.github.tanguygab.realvillagertowns.menus;

import io.github.tanguygab.realvillagertowns.villagers.RVTPlayer;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public abstract class AskMenu extends RVTMenu {

    protected final RVTPlayer player2;
    private final String action;

    public AskMenu(RVTPlayer player, RVTPlayer player2, String title, String action) {
        super(player, "&0&l"+title, InventoryType.HOPPER);
        this.player2 = player2;
        this.action = action;
    }

    @Override
    public void onOpen() {
        inv.setItem(1, createMenuItem(Material.SLIME_BALL, "&2Yes", "&8I want to", "&8"+action));
        inv.setItem(3, createMenuItem(Material.MAGMA_CREAM, "&cNo", "&8I don't want to", "&8"+action));
        open();
    }

    protected abstract void onAccept();
    protected abstract void onDecline();

    @Override
    public void onClick(ItemStack item, int slot) {
        switch (slot) {
            case 1 -> {
                onClose();
                onAccept();
            }
            case 3 -> {
                onClose();
                onDecline();
            }
        }
    }

    @Override
    public void onClose() {
        onDecline();
        super.onClose();
    }
}
