package me.gnat008.perworldinventory.listeners.server;

import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.events.InventoryLoadCompleteEvent;
import me.gnat008.perworldinventory.events.InventoryLoadEvent;
import me.gnat008.perworldinventory.process.InventoryChangeProcess;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import javax.inject.Inject;

/**
 * Class for listening for inventory loading events.
 */
public class InventoryLoadingListener implements Listener {

    private InventoryChangeProcess process;
    private PWIPlayerManager playerManager;

    @Inject
    InventoryLoadingListener(InventoryChangeProcess process, PWIPlayerManager playerManager) {
        this.process = process;
        this.playerManager = playerManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoadComplete(InventoryLoadCompleteEvent event) {
        switch (event.getCause()) {
            case WORLD_CHANGE:
                process.postProcessWorldChange(event.getPlayer());
                break;
            case GAMEMODE_CHANGE:
                if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                    // Sometimes some players get Creative mode, but cannot fly
                    event.getPlayer().setAllowFlight(true);
                }
                break;
            case CHANGED_DEFAULTS:
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryLoad(InventoryLoadEvent event) {
        if (!event.isCancelled()) {
            playerManager.getPlayerData(
                    event.getGroup(),
                    event.getNewGameMode(),
                    event.getPlayer(),
                    event.getCause());
        }
    }
}
