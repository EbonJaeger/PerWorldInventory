package me.gnat008.perworldinventory.listeners.player;

import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import javax.inject.Inject;

/**
 * Listens for {@link PlayerTeleportEvent} and adds players to the cache.
 */
public class PlayerTeleportListener implements Listener {

    private GroupManager groupManager;
    private PWIPlayerManager playerManager;

    @Inject
    PlayerTeleportListener(GroupManager groupManager, PWIPlayerManager playerManager) {
        this.groupManager = groupManager;
        this.playerManager = playerManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getWorld().equals(to.getWorld())) {
            return;
        }

        PwiLogger.debug("[EVENTS] Player '" + event.getPlayer().getName() + "' going from world '" + from + "' to world '" + to + "'");

        Group groupFrom = groupManager.getGroupFromWorld(from.getWorld().getName());
        Group groupTo = groupManager.getGroupFromWorld(to.getWorld().getName());

        if (groupFrom.equals(groupTo)) {
            return;
        }

        playerManager.addPlayer(event.getPlayer(), groupFrom);
    }
}
