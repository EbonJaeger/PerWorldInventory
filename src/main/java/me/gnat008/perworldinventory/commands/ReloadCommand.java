package me.gnat008.perworldinventory.commands;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.permission.AdminPermission;
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
    private Settings settings;


    @Override
    public void executeCommand(CommandSender sender, List<String> args) {
        settings.reload();
        plugin.reload();
        groupManager.loadGroupsToMemory(plugin.getWorldsConfig());

        sender.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Configuration files reloaded!");
    }

    @Override
    public PermissionNode getRequiredPermission() {
        return AdminPermission.RELOAD;
    }
}
