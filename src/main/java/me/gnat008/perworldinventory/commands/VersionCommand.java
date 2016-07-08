package me.gnat008.perworldinventory.commands;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.permission.AdminPermission;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PermissionNode;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import java.util.List;

public class VersionCommand implements ExecutableCommand {

    @Inject
    private PerWorldInventory plugin;
    @Inject
    private PermissionManager permissionManager;

    private PermissionNode permissionNode = AdminPermission.VERSION;

    @Override
    public void executeCommand(CommandSender sender, List<String> args) {
        if (!permissionManager.hasPermission(sender, permissionNode)) {
            sender.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "You do not have permission to do that.");
            return;
        }

        String version = plugin.getDescription().getVersion();
        String authors = plugin.getDescription().getAuthors().toString().replace("[", "").replace("]", "");

        sender.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Version: " + ChatColor.BLUE + version);
        sender.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Author: " + ChatColor.BLUE + authors);
    }
}
