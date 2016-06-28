package me.gnat008.perworldinventory.listeners.server;

import me.gnat008.perworldinventory.permission.PermissionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class PluginListener implements Listener {

    private PermissionManager permissionManager;

    public PluginListener(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginDisable(PluginDisableEvent event) {
        final String name = event.getPlugin().getName();

        permissionManager.onPluginDisable(name);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginEnable(PluginEnableEvent event) {
        final String name = event.getPlugin().getName();

        permissionManager.onPluginEnable(name);
    }
}
