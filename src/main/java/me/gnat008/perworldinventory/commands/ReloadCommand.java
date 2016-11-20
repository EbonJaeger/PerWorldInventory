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
    @Inject
    private Settings settings;

    private PermissionNode permissionNode = AdminPermission.RELOAD;

    @Override
    public void executeCommand(CommandSender sender, List<String> args) {
        if (!permissionManager.hasPermission(sender, permissionNode)) {
            sender.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "You do not have permission to do that.");
            return;
        }

        settings.reload();
        plugin.reload();
        groupManager.loadGroupsToMemory(plugin.getWorldsConfig());

        sender.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Configuration files reloaded!");
    }
}
