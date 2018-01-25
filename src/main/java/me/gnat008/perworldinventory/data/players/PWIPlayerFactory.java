package me.gnat008.perworldinventory.data.players;

import me.gnat008.perworldinventory.BukkitService;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.groups.Group;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import javax.inject.Inject;

/**
 * Factory for creating {@link PWIPlayer} objects.
 */
public class PWIPlayerFactory {

    @Inject
    private PerWorldInventory plugin;

    @Inject
    BukkitService bukkitService;

    PWIPlayerFactory() {
    }

    /**
     * Creates a PWI player.
     *
     * @param player the Bukkit player to base the player on
     * @param group the group the player is in
     * @return the created PWI player
     */
    public PWIPlayer create(Player player, Group group) {
        double balance = 0;
        double bankBalance = 0;
        if (plugin.isEconEnabled()) {
            Economy economy = plugin.getEconomy();
            bankBalance = economy.bankBalance(player.getName()).balance;
            balance = economy.getBalance(player);
        }

        return new PWIPlayer(player, group, bankBalance, balance, bukkitService.shouldUseAttributes());
    }
}
