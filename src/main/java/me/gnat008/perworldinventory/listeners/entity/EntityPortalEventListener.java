package me.gnat008.perworldinventory.listeners.entity;

import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;

import javax.inject.Inject;

/**
 * Class to process when an item attempts to go through a portal.
 * If the groups on either side of the portal are different, then the
 * event will be canceled and the item will not be teleported.
 */
public class EntityPortalEventListener implements Listener {

    @Inject
    private GroupManager groupManager;

    EntityPortalEventListener() {}

    public void onEntityPortalTeleport(EntityPortalEvent event) {
        if (!(event.getEntity() instanceof Item))
            return;

        PwiLogger.debug("A '" + event.getEntity().getName() + "' is going through a portal!");

        String worldFrom = event.getFrom().getWorld().getName();
        String worldTo = event.getTo().getWorld().getName();
        Group from = groupManager.getGroupFromWorld(worldFrom);
        Group to = groupManager.getGroupFromWorld(worldTo);

        // If the groups are different, cancel the event
        if (!from.equals(to)) {
            PwiLogger.debug("Group '" + from.getName() + "' and group '" + to.getName() + "' are different! Canceling event!");
            event.setCancelled(true);
        }
    }
}
