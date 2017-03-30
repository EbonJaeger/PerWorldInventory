package me.gnat008.perworldinventory.listeners.server;

import me.gnat008.perworldinventory.data.serializers.DeserializeCause;
import me.gnat008.perworldinventory.events.InventoryLoadCompleteEvent;
import me.gnat008.perworldinventory.process.InventoryChangeProcess;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import javax.inject.Inject;

/**
 * Class for listening for inventory loading events.
 */
public class InventoryLoadingListener implements Listener {

    private InventoryChangeProcess process;

    @Inject
    InventoryLoadingListener(InventoryChangeProcess process) {
        this.process = process;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoadComplete(InventoryLoadCompleteEvent event) {
        if (event.getCause() == DeserializeCause.WORLD_CHANGE) {
            process.postProcessWorldChange(event.getPlayer());
        }
    }
}
