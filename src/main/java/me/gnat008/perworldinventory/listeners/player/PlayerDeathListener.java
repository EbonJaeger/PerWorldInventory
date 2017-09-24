package me.gnat008.perworldinventory.listeners.player;

import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import javax.inject.Inject;

/**
 * Listens for any {@link PlayerDeathEvent} in order to prevent
 * inventory duplication.
 */
public class PlayerDeathListener implements Listener {

    private GroupManager groupManager;
    private PWIPlayerManager playerManager;

    @Inject
    PlayerDeathListener(GroupManager groupManager, PWIPlayerManager playerManager) {
        this.groupManager = groupManager;
        this.playerManager = playerManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Group group = groupManager.getGroupFromWorld(player.getLocation().getWorld().getName());

        playerManager.addPlayer(player, group);
    }
}
