package me.gnat008.perworldinventory.commands;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.data.FileWriter;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.permission.AdminPermission;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PermissionNode;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.util.List;

public class SetWorldDefaultCommand implements ExecutableCommand {

    @Inject
    private PerWorldInventory plugin;
    @Inject
    private FileWriter fileSerializer;
    @Inject
    private GroupManager groupManager;
    @Inject
    private PermissionManager permissionManager;

    private PermissionNode permissionNode = AdminPermission.SETDEFAULTS;

    @Override
    public void executeCommand(CommandSender sender, List<String> args) {
        // Check Permission
        if (!permissionManager.hasPermission(sender, permissionNode)) {
            sender.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "You do not have permission to do that.");
            return;
        }

        // Check if player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "This command may only be run from ingame.");
            return;
        }

        Player player = (Player) sender;

        // Check args
        Group group;
        if (args.size() == 1) {
            String name = args.get(0);
            group = name.equalsIgnoreCase("serverDefault") ? new Group("__default", null, null) : groupManager.getGroup(name);
        } else if (args.size() == 0 || args.isEmpty()) {
            try {
                group = groupManager.getGroupFromWorld(player.getWorld().getName());
            } catch (IllegalArgumentException ex) {
                player.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "You are not standing in a valid world!");
                group = null;
            }
        } else {
            group = null;
            player.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "Incorrect number of arguments! See "
                    + ChatColor.WHITE + "/pwi help" + ChatColor.GRAY + " for usage.");
        }

        if (group != null) {
            fileSerializer.setGroupDefault(player, group);
        }
    }
}
