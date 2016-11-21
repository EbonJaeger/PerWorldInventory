package me.gnat008.perworldinventory.process;

import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.entity.Player;

import javax.inject.Inject;

/**
 * Handle a player leaving the server.
 */
public class PlayerQuitProcess {

    @Inject
    private GroupManager groupManager;

    @Inject
    private PWIPlayerManager playerManager;

    PlayerQuitProcess() {
    }

    /**
     * Save all of a player's data when they leave the server.
     *
     * @param player The player leaving.
     */
    public void processPlayerLeave(Player player) {
        String logoutWorld = player.getWorld().getName();
        Group group = groupManager.getGroupFromWorld(logoutWorld);

        PwiLogger.debug("Player '" + player.getName() + "' quit! Checking cache");

        PWIPlayer cached = playerManager.getPlayer(group, player);
        if (cached != null) {
            PwiLogger.debug("Cached data for player '" + player.getName() + "' found! Updating and setting them as saved");

            playerManager.updateCache(player, cached);
            cached.setSaved(true);
        }

        PwiLogger.debug("Saving logout data for player '" + player.getName() + "'...");
        playerManager.savePlayer(group, player);
    }
}
