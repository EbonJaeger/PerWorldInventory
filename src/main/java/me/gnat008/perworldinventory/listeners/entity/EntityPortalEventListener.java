package me.gnat008.perworldinventory.listeners.entity;

import me.gnat008.perworldinventory.ConsoleLogger;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityPortalTeleport(EntityPortalEvent event) {
        if (!(event.getEntity() instanceof Item))
            return;

        ConsoleLogger.debug("[ENTITYPORTALEVENT] A '" + event.getEntity().getName() + "' is going through a portal!");

        String worldFrom = event.getFrom().getWorld().getName();

        // For some reason, event.getTo().getWorld().getName() is sometimes null
        if (event.getTo() == null || event.getTo().getWorld() == null) { // Not gonna bother checking name; its already a WTF that this is needed
            ConsoleLogger.debug("[ENTITYPORTALEVENT] event.getTo().getWorld().getName() would throw a NPE! Exiting method!");
            return;
        }

        String worldTo = event.getTo().getWorld().getName();
        Group from = groupManager.getGroupFromWorld(worldFrom);
        Group to = groupManager.getGroupFromWorld(worldTo);

        // If the groups are different, cancel the event
        if (!from.equals(to)) {
            ConsoleLogger.debug("[ENTITYPORTALEVENT] Group '" + from.getName() + "' and group '" + to.getName() + "' are different! Canceling event!");
            event.setCancelled(true);
        }
    }
}
