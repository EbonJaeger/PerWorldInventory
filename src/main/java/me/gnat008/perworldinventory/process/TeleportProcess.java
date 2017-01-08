package me.gnat008.perworldinventory.process;

import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.config.PwiProperties;
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

        if (!settings.getProperty(PwiProperties.ENABLE_LAST_LOCATION)) {
            if (settings.getProperty(PwiProperties.LAST_LOCATION_OVERRIDE_STORAGE)) {
                Location from = event.getFrom();
                Group groupFrom = groupManager.getGroupFromWorld(from.getWorld().getName());
                Map<String, Location> lastLocInWorlds = metaDataManager.getLastLocationInWorldMap(player);
                Map<String, String> lastWorldInGroups = metaDataManager.getLastWorldInGroupMap(player);
                PwiLogger.debug("Last location storage override enabled; Keeping player '"+player.getName()+"' last location.");
                lastLocInWorlds.put(from.getWorld().getName(),from);
                lastWorldInGroups.put(groupFrom.getName(),from.getWorld().getName());
            }
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        TeleportCause cause = event.getCause();

        if (event.isCancelled()) {
            PwiLogger.debug(String.format("Player '%s' tried to teleport from '%s' to '%s', but the event was previously cancelled, dismissing handler",
              player.getName(),
              from,
              to));
            return;
        }

        if (from == null || from.getWorld() == null) {
            PwiLogger.warning(String.format("Player '%s' tried to teleport from '%s' to '%s', but got null on origin. dismissing handler.",
              player.getName(),
              from,
              to));
            return;
        }

        if (to == null || to.getWorld() == null) {
            PwiLogger.warning(String.format("Player '%s' tried to teleport from '%s' to '%s', but got null on origin. dismissing handler.",
              player.getName(),
              from,
              to));
            return;
        }

        if (from.getWorld().equals(to.getWorld())) {
            PwiLogger.debug(String.format("Player '%s' is teleporting from world '%s' to world '%s', in-world teleport, dismissing handler.",
              player.getName(),
              from.getWorld().getName(),
              to.getWorld().getName()));
            return;
        }

        processTeleport(player, event, cause, from, to);
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
    protected void processTeleport(Player player, PlayerTeleportEvent event, TeleportCause cause, Location from, Location to) {
        Group groupFrom = groupManager.getGroupFromWorld(from.getWorld().getName());
        Group groupTo = groupManager.getGroupFromWorld(to.getWorld().getName());

        Map<String, Location> lastLocInWorlds = metaDataManager.getLastLocationInWorldMap(player);
        Map<String, String> lastWorldInGroups = metaDataManager.getLastWorldInGroupMap(player);

        PwiLogger.debug("Player '"
        + player.getName() + "' is teleporting from world '"
        + from.getWorld().getName() + "' to world '" + to.getWorld().getName()
        + "' cause '" + cause.name() + "'");

        if (groupTo.isConfigured()) {
            if(groupTo.equals(groupFrom)) {
                if (groupTo.shouldUseLastPosWithinGroup(cause)) {
                    PwiLogger.debug("Group '" + groupTo.getName()
                    + "' is configured to enforce last location during in-group world change on cause '"
                    + cause.name() +"'. Redirecting...");

                    enforceLastLocationInWorld(player, event, to, lastLocInWorlds);
                }
            } else {
                processToGroupTeleport(player, event, cause, to, groupTo,
                  lastLocInWorlds, lastWorldInGroups);
            }
        } else {
            PwiLogger.debug("TO group (" + groupTo.getName() + ") is not defined, dismissing redirect.");
        }

        lastLocInWorlds.put(from.getWorld().getName(),from);
        lastWorldInGroups.put(groupFrom.getName(),from.getWorld().getName());
    }

    private void enforceLastLocationInWorld (Player player, PlayerTeleportEvent event, Location to, Map<String, Location> lastLocInWorlds) {
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
    }

    private void processToGroupTeleport (Player player, PlayerTeleportEvent event,
    TeleportCause cause, Location to, Group group,
    Map<String, Location> lastLocInWorlds,
    Map<String, String> lastWorldInGroups) {
        if(group.shouldUseLastWorld(cause)) {
            PwiLogger.debug("Group '" + group.getName()
            + "' is configured to enforce last world on cause '"
            + cause.name() +"'. Redirecting...");

            to = processWorldRedirect(to, group, lastWorldInGroups);
            event.setTo(to);
        } else {
            PwiLogger.debug("Group '" + group.getName()
            + "' is NOT configured to enforce world during group change on cause '"
            + cause.name() + "'.");
        }
        if(group.shouldUseLastPosToGroup(cause)){
            enforceLastLocationInWorld(player, event, to, lastLocInWorlds);
        } else {
            PwiLogger.debug("Group '" + group.getName()
            + "' is NOT configured to enforce location during group change on cause '"
            + cause.name() +"'.");
        }
    }

    private Location processWorldRedirect(Location to, Group group, Map<String, String> lastWorldInGroups) {
        World toLastWorld = server.getWorld(lastWorldInGroups.get(group.getName()));
        if(toLastWorld != null) {
            if(toLastWorld.equals(to.getWorld())) {
                PwiLogger.debug("Player is already changing to world '"
                + toLastWorld.getName()+"', dismissing world redirect.");

                return to;
            }
            return toLastWorld.getSpawnLocation();
        } else {
            if(group.getDefaultWorld() != null) {
                PwiLogger.debug("It appears the player has never been in group '"+ group.getName()+ "' Redirecting to default world '"+ group.getDefaultWorld() +"'");
                World defaultWorld = server.getWorld(group.getDefaultWorld());
                if(defaultWorld != null) {
                    return defaultWorld.getSpawnLocation();
                } else {
                    PwiLogger.warning("It appears the player has never been in group '"+ group.getName()+ "' and the default world '" + group.getDefaultWorld() + "' is missing. dismissing world redirect.");
                    return to;
                }
            } else {
                PwiLogger.debug("It appears the player has never been in group '"+ group.getName()+ "' and no default world has been set. dismissing world redirect.");
                return to;
            }
        }
    }
}
