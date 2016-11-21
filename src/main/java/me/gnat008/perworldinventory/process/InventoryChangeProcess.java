package me.gnat008.perworldinventory.process;

import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PlayerPermission;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import javax.inject.Inject;

/**
 * Process to follow when a player's inventory is changing.
 */
public class InventoryChangeProcess {

    @Inject
    private GroupManager groupManager;

    @Inject
    private PermissionManager permissionManager;

    @Inject
    private PWIPlayerManager playerManager;

    @Inject
    private Settings settings;

    InventoryChangeProcess() {
    }

    /**
     * Begins the process of a player changing worlds.
     *
     * @param event The called {@link PlayerChangedWorldEvent}.
     */
    public void preProcessWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String worldFrom = event.getFrom().getName();
        String worldTo = player.getWorld().getName();
        Group groupFrom = groupManager.getGroupFromWorld(worldFrom);
        Group groupTo = groupManager.getGroupFromWorld(worldTo);

        PwiLogger.debug("Player '" + player.getName() + "' going from world '" + worldFrom + "' to world '" + worldTo + "'");

        playerManager.addPlayer(player, groupFrom);

        processWorldChange(player, groupFrom, groupTo);
    }

    /**
     * Process the situation where a player logs in to a different world than the one they
     * logged out of.
     *
     * @param player The player to process.
     * @param from The world they logged out from.
     * @param to The world they logged in to.
     */
    public void processWorldChangeOnSpawn(Player player, Group from, Group to) {
        if (!from.equals(to)) {
            PwiLogger.debug("Logout world groups are different! Saving data for player '" + player.getName() + "' for group '" + from.getName() + "'");

            playerManager.addPlayer(player, from);

            processWorldChange(player, from, to);
        }
    }

    /**
     * Process a player changing worlds. This is where the inventory and stats
     * for a player are changed, if necessary.
     *
     * @param player The player to process.
     * @param from The group they're coming from.
     * @param to The group they're going to.
     */
    protected void processWorldChange(Player player, Group from, Group to) {
        // Check of the FROM group is configured
        if (!from.isConfigured() && settings.getProperty(PwiProperties.SHARE_IF_UNCONFIGURED)) {
            PwiLogger.debug("FROM group (" + from.getName() + ") is not defined, and plugin configured to share inventory");
            postProcessWorldChange(player, to);
            return;
        }

        // Check if the groups are actually the same group
        if (from.equals(to)) {
            PwiLogger.debug("Both groups are the same: '" + to.getName() + "'");
            postProcessWorldChange(player, to);
            return;
        }

        // Check of the TO group is configured
        if (!to.isConfigured() && settings.getProperty(PwiProperties.SHARE_IF_UNCONFIGURED)) {
            PwiLogger.debug("TO group (" + to.getName() + ") is not defined, and plugin configured to share inventory");
            postProcessWorldChange(player, to);
            return;
        }

        // Check if the player bypasses the changes
        if (!settings.getProperty(PwiProperties.DISABLE_BYPASS) && permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)) {
            PwiLogger.debug("Player '" + player.getName() + "' has '" + PlayerPermission.BYPASS_WORLDS.getNode() + "' permission! Returning");
            postProcessWorldChange(player, to);
            return;
        }

        // Check if gamemodes have separate inventories
        if (settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)) {
            PwiLogger.debug("Gamemodes are separated! Loading data for player '" + player.getName() + "' for group '" + to.getName() + "' in gamemode '" + player.getGameMode().name() + "'");
            playerManager.getPlayerData(to, player.getGameMode(), player);
        } else {
            PwiLogger.debug("Loading data for player '" + player.getName() + "' for group '" + to.getName() + "'");
            playerManager.getPlayerData(to, GameMode.SURVIVAL, player);
        }

        postProcessWorldChange(player, to);
    }

    /**
     * Performs any final checks or actions after the inventory and stats have been
     * changed.
     *
     * @param player The player to process.
     * @param to The group the player changed to.
     */
    protected void postProcessWorldChange(Player player, Group to) {
        // Check if we should manage the player's gamemode when changing worlds
        if (settings.getProperty(PwiProperties.MANAGE_GAMEMODES)) {
            if (permissionManager.hasPermission(player, PlayerPermission.BYPASS_ENFORCEGAMEMODE)) {
                PwiLogger.debug("Player '" + player.getName() + "' has '" + PlayerPermission.BYPASS_ENFORCEGAMEMODE.getNode() + "' permission! Not enforcing gamemode.");
            } else {
                PwiLogger.debug("PWI manages gamemodes! Setting player '" + player.getName() + "' to gamemode " + to.getGameMode().name());
                player.setGameMode(to.getGameMode());
            }
        }
    }
}
