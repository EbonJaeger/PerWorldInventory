package me.gnat008.perworldinventory.process;

import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.metadata.PWIMetaDataManager;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PlayerPermission;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import javax.inject.Inject;
import java.util.Map;

/**
 * Process to follow when a player's inventory is changing.
 */
public class TeleportProcess {

    @Inject
    private GroupManager groupManager;

    @Inject
    private PermissionManager permissionManager;

    @Inject
    private PWIPlayerManager playerManager;

    @Inject
    private Settings settings;

    @Inject
    private PWIMetaDataManager metaDataManager;

    @Inject
    private Server server;

    TeleportProcess() {
    }

    /**
     * Begins the process of a player teleporting.
     *
     * @param event The called {@link PlayerTeleportEvent}.
     */
    public void preProcessTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (event.isCancelled()) {
            PwiLogger.debug(String.format("Player '%s' tried to teleport from '%s' to '%s', but the event was previously cancelled, dismissing handler",
              player.getName(),
              from,
              to));
            return;
        }

        if (from == null || from.getWorld() == null) {
            PwiLogger.warning(String.format("Player '%s' tried to teleport from '%s' to '%s', but got null on origin. Cancelling teleport.",
              player.getName(),
              from,
              to));
            event.setCancelled(true);
            return;
        }

        if (to == null || to.getWorld() == null) {
            PwiLogger.warning(String.format("Player '%s' tried to teleport from '%s' to '%s', but got null on origin. Cancelling teleport.",
              player.getName(),
              from,
              to));
            event.setCancelled(true);
            return;
        }

        if (from.getWorld().equals(to.getWorld())) {
            PwiLogger.debug(String.format("Player '%s' is teleporting from world '%s' to world '%s', in-world teleport, dismissing handler.",
              player.getName(),
              from.getWorld().getName(),
              to.getWorld().getName()));
            return;
        }

        processTeleport(player, event, from, to);

        if (event.isCancelled()) {
            PwiLogger.debug(String.format("Player '%s' was switching worlds, but the teleport was canceled during the teleport process.",
              player.getName()));
            event.setCancelled(true);
            return;
        }

        if (to.getWorld().equals(from.getWorld())) {
            PwiLogger.debug(String.format("Player '%s' was switching worlds, but somehow the teleport process ended up teleporting the player back to '%s'. Cancelling teleport.",
              player.getName(),
              from.getWorld().getName()));
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Process a player teleporting to another world. This is where
     * last-known-location redirecting happens.
     * both in-world redirect and to-other-world-in-group redirect
     *
     * @param player The {@link Player} to process.
     * @param event  The {@link PlayerTeleportEvent} that started the teleport
     * @param from   The {@link Location} they're coming from. SIDEFFECT: Might be mutated
     * @param to     The {@link Location} they're going to. SIDEFFECT: Might be mutated
     */
    protected void processTeleport(Player player, PlayerTeleportEvent event, Location from, Location to) {
        Group groupFrom = groupManager.getGroupFromWorld(from.getWorld().getName());
        Group groupTo = groupManager.getGroupFromWorld(to.getWorld().getName());

        Map<String, Location> lastLocInWorlds = metaDataManager.getLastLocationInWorldMap(player);
        Map<String, String> lastWorldInGroups = metaDataManager.getLastWorldInGroupMap(player);
        TeleportCause cause = event.getCause();

        PwiLogger.debug(String.format("Player '%s' is teleporting from world '%s' to world '%s' cause '%s'",
          player.getName(),
          from.getWorld().getName(),
          to.getWorld().getName(),
          cause));
        if (groupTo.isConfigured()) {
            if(groupTo.equals(groupFrom)) {
                if(groupTo.shouldUseLastPosWithinGroup(cause)) {
                    PwiLogger.debug("In group world change and group '"+groupTo.getName()+"' is configured to enforce last position during internal world change on cause '"+cause+"' redirecting...");
                    Location newTo = lastLocInWorlds.get(to.getWorld().getName());
                    if(newTo != null) {
                        // We don't want weird stuff to happen in case of
                        // deserialized location missing world.
                        newTo.setWorld(to.getWorld());
                        to = newTo;
                        event.setTo(to);
                    } else {
                        PwiLogger.debug("Player has not visited world '"+to.getWorld().getName()+"' yet. Redirecting to spawn...");
                        to = to.getWorld().getSpawnLocation();
                        event.setTo(to);
                    }
                } else {
                    PwiLogger.debug("Group '" + groupTo.getName() + "' is NOT configured to enforce location during in-world change on cause '"+ cause +"'.");
                }
            } else {
                World toLastWorld = server.getWorld(lastWorldInGroups.get(groupTo.getName()));
                if(groupTo.shouldUseLastWorld(cause)) {
                    PwiLogger.debug("Group '" + groupTo.getName() + "' is configured to enforce last world on cause '"+ cause +"'. Redirecting...");
                    if(toLastWorld != null) {
                        to = toLastWorld.getSpawnLocation();
                    } else {
                        if(groupTo.getDefaultWorld() != null) {
                            PwiLogger.debug("It appears the player has never been in group '"+ groupTo.getName()+ "' Redirecting to default world '"+ groupTo.getDefaultWorld() +"'");
                            World defaultWorld = server.getWorld(groupTo.getDefaultWorld());
                            if(defaultWorld != null) {
                                to = defaultWorld.getSpawnLocation();
                            } else {
                                PwiLogger.warning("It appears the player has never been in group '"+ groupTo.getName()+ "' and the default world '" + groupTo.getDefaultWorld() + "' is missing. dismissing world redirect.");
                            }
                        } else {
                            PwiLogger.debug("It appears the player has never been in group '"+ groupTo.getName()+ "' and no default world has been set. dismissing world redirect.");
                        }
                    }
                } else {
                    PwiLogger.debug("Group '" + groupTo.getName() + "' is NOT configured to enforce world during group change on cause '"+ cause +"'.");
                }
                if(groupTo.shouldUseLastPosToGroup(cause)){
                    PwiLogger.debug("Group '" + groupTo.getName() + "' is configured to enforce last location during group change on cause '"+ cause +"'. Redirecting...");
                    Location newTo = lastLocInWorlds.get(to.getWorld().getName());
                    if(newTo != null) {
                        // We don't want weird stuff to happen in case of
                        // deserialized location missing world.
                        newTo.setWorld(to.getWorld());
                        to = newTo;
                        event.setTo(to);
                    } else {
                        PwiLogger.debug("Player has not visited world '"+to.getWorld().getName()+"' yet. Redirecting to spawn...");
                        to = to.getWorld().getSpawnLocation();
                        event.setTo(to);
                    }
                } else {
                    PwiLogger.debug("Group '" + groupTo.getName() + "' is NOT configured to enforce location during group change on cause '"+ cause +"'.");
                }
            }
        } else {
            PwiLogger.debug("TO group (" + groupTo.getName() + ") is not defined, dismissing redirect.");
        }

        lastLocInWorlds.put(from.getWorld().getName(),from);
        lastWorldInGroups.put(groupFrom.getName(),from.getWorld().getName());
    }

}
