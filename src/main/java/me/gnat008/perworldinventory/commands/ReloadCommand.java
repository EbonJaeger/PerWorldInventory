package me.gnat008.perworldinventory.commands;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.permission.AdminPermission;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PermissionNode;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import java.util.List;

public class ReloadCommand implements ExecutableCommand {

    @Inject
    private PerWorldInventory plugin;
    @Inject
    private GroupManager groupManager;
    @Inject
    private PermissionManager permissionManager;

    private PermissionNode permissionNode = AdminPermission.RELOAD;

    @Override
    public void executeCommand(CommandSender sender, List<String> args) {
        if (!permissionManager.hasPermission(sender, permissionNode)) {
            sender.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "You do not have permission to do that.");
            return;
        }

        plugin.reloadConfig();
        Settings.reloadSettings(plugin.getConfig());
        if (Settings.getInt("config-version") < PerWorldInventory.CONFIG_VERSION) {
            plugin.getLogger().warning("Your PerWorldInventory config is out of date! Some options may be missing.");
            plugin.getLogger().warning("Copy the new options from here: https://www.spigotmc.org/resources/per-world-inventory.4482/");
        }
        groupManager.loadGroupsToMemory(plugin.getWorldsConfig());

        sender.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Configuration files reloaded!");
    }
}
