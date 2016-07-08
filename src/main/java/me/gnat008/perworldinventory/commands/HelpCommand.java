package me.gnat008.perworldinventory.commands;

import me.gnat008.perworldinventory.permission.AdminPermission;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PermissionNode;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.util.List;

public class HelpCommand implements ExecutableCommand {

    @Inject
    private PermissionManager permissionManager;

    private PermissionNode permissionNode = AdminPermission.HELP;

    @Override
    public void executeCommand(CommandSender sender, List<String> args) {
        if (!permissionManager.hasPermission(sender, permissionNode)) {
            sender.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "You do not have permission to do that.");
            return;
        }

        if (sender instanceof Player) {
            // Send the pretty version
            sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
            sender.sendMessage(ChatColor.DARK_GRAY + "                [ " + ChatColor.BLUE + "PerWorldInventory Commands" + ChatColor.DARK_GRAY + " ]");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "/perworldinventory convert multiverse" + ChatColor.BLUE + " - " + ChatColor.GRAY + "Convert data from Multiverse-Inventories");
            sender.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "/perworldinventory help" + ChatColor.BLUE + " - " + ChatColor.GRAY + "Shows this help page");
            sender.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "/perworldinventory reload" + ChatColor.BLUE + " - " + ChatColor.GRAY + "Reloads all configuration files");
            sender.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "/perworldinventory version" + ChatColor.BLUE + " - " + ChatColor.GRAY + "Shows the version and authors of the server");
            sender.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "/perworldinventory setworlddefault [group|serverDefault]" + ChatColor.BLUE + " - " + ChatColor.GRAY + "Set the default inventory loadout for a world, or the server default." + '\n' + ChatColor.YELLOW + "The group you are standing in will be used if no group is specified.");
            sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
        } else {
            // Sender is the console, no pretty stuff for you!
            sender.sendMessage("Available commands:");
            sender.sendMessage("/perworldinventory convert - Convert MultiVerse-Inventories data");
            sender.sendMessage("/perworldinventory help - Displays this help");
            sender.sendMessage("/perworldinventory version - Shows the version of the server");
            sender.sendMessage("/perworldinventory reload - Reload config and world files");
        }
    }
}
