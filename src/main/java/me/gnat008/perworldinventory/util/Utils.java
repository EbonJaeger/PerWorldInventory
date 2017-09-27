package me.gnat008.perworldinventory.util;

import me.gnat008.perworldinventory.PerWorldInventory;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/**
 * Class that holds utility methods.
 */
public final class Utils {

    /**
     * Check if a server's version is the same as a given version
     * or higher.
     *
     * @param version The server's version.
     * @param major The major version number.
     * @param minor The minor version number.
     * @param patch The patch version number.
     *
     * @return True if the server is running the same version or newer.
     */
    public static boolean checkServerVersion(String version, int major, int minor, int patch) {
        String versionNum = version.substring(version.indexOf(".") - 1, version.length() - 1).trim();
        String[] parts = versionNum.split("\\.");

        try {
            if ((Integer.parseInt(parts[0]) >= major)) {
                if (Integer.parseInt(parts[1]) == minor) {
                    if (parts.length == 2) {
                        return true;
                    } else {
                        return Integer.parseInt(parts[2]) >= patch;
                    }
                } else {
                    return Integer.parseInt(parts[1]) > minor;
                }
            }
        } catch (NumberFormatException ex) {
            return false;
        }

        return false;
    }

    /**
     * Clear a player's inventory and set all of their stats to default.
     *
     * @param plugin {@link PerWorldInventory} for econ.
     * @param player The player to zero.
     */
    public static void zeroPlayer(PerWorldInventory plugin, Player player) {
        zeroPlayer(plugin, player, true);
    }

    /**
     * Set a player's stats to defaults, and optionally clear their inventory.
     *
     * @param plugin {@link PerWorldInventory} for econ.
     * @param player The player to zero.
     * @param clearInventory Clear the player's inventory.
     */
    public static void zeroPlayer(PerWorldInventory plugin, Player player, boolean clearInventory) {
        if (clearInventory) {
            player.getInventory().clear();
            player.getEnderChest().clear();
        }

        player.setExp(0f);
        player.setFoodLevel(20);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setLevel(0);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setSaturation(5f);
        player.setFallDistance(0f);
        player.setFireTicks(0);

        if (plugin.isEconEnabled()) {
            Economy econ = plugin.getEconomy();
            econ.bankWithdraw(player.getName(), econ.bankBalance(player.getName()).amount);
            econ.withdrawPlayer(player, econ.getBalance(player));
        }
    }
}
