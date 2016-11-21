package me.gnat008.perworldinventory.commands;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.permission.AdminPermission;
import me.gnat008.perworldinventory.permission.PermissionNode;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import java.util.List;

public class VersionCommand implements ExecutableCommand {

    @Inject
    private PerWorldInventory plugin;


    @Override
    public void executeCommand(CommandSender sender, List<String> args) {
        String version = plugin.getDescription().getVersion();
        String authors = plugin.getDescription().getAuthors().toString().replace("[", "").replace("]", "");

        sender.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Version: " + ChatColor.BLUE + version);
        sender.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Author: " + ChatColor.BLUE + authors);
    }

    @Override
    public PermissionNode getRequiredPermission() {
        return AdminPermission.VERSION;
    }
}
