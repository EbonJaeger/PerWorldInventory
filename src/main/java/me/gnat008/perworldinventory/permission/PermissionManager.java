package me.gnat008.perworldinventory.permission;

import me.gnat008.perworldinventory.ConsoleLogger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import javax.inject.Inject;

public class PermissionManager {

    private final PluginManager pluginManager;

    private boolean usingPermissionsPlugin;

    @Inject
    PermissionManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        checkForPlugins();
    }

    /**
     * Evaluate whether the sender has permission to perform an action.
     *
     * @param sender The sender to evaluate.
     * @param node The permission node.
     * @return If the sender has permission.
     */
    public boolean hasPermission(CommandSender sender, PermissionNode node) {
        if (node == null)
            return true;

        if (!(sender instanceof Player) || !usingPermissionsPlugin)
            return node.getDefaultPermission().evaluate(sender);

        Player player = (Player) sender;
        return player.hasPermission(node.getNode());
    }

    public void onPluginDisable(String pluginName) {
        if (PermissionSystem.isPermissionSystem(pluginName)) {
            checkForPlugins();
        }
    }

    public void onPluginEnable(String pluginName) {
        if (PermissionSystem.isPermissionSystem(pluginName))
            checkForPlugins();
    }

    private void checkForPlugins() {
        this.usingPermissionsPlugin = false;

        for (PermissionSystem type : PermissionSystem.values()) {
            try {
                Plugin plugin = pluginManager.getPlugin(type.getPluginName());

                if (plugin == null)
                    continue;
                if (!plugin.isEnabled())
                    continue;

                this.usingPermissionsPlugin = true;
            } catch (Exception ex) {
                ConsoleLogger.warning("Error encountered while checking for permission plugin:", ex);
            }
        }
    }
}
