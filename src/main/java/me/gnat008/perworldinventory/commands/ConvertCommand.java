package me.gnat008.perworldinventory.commands;

import me.gnat008.perworldinventory.permission.AdminPermission;
import me.gnat008.perworldinventory.permission.PermissionNode;
import me.gnat008.perworldinventory.conversion.ConvertService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;

import javax.inject.Inject;
import java.util.List;

public class ConvertCommand implements ExecutableCommand {

    private final PluginManager pluginManager;
    private final ConvertService convertService;

    @Inject
    ConvertCommand(PluginManager pluginManager, ConvertService convertService) {
        this.pluginManager = pluginManager;
        this.convertService = convertService;
    }

    @Override
    public void executeCommand(CommandSender sender, List<String> args) {
        // Check number of args
        if (args.size() != 1) {
            sender.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "Incorrect usage. Type /pwi help for help.");
            return;
        }

        // Check if arg is valid
        String format = args.get(0);
        if (!format.equalsIgnoreCase("multiverse") && !format.equalsIgnoreCase("multiinv")) {
            sender.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "Invalid argument. Please specify 'multiverse' or 'multiinv'.");
            return;
        }

        // Expand the plugin name if it is MultiVerse
        if (format.equalsIgnoreCase("multiverse")) {
            format = "Multiverse-Inventories";
        } else {
            format = "MultiInv";
        }

        if (isPluginInstalled(format)) {
            if (format.equalsIgnoreCase("Multiverse-Inventories")) {
                sender.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Converting from Multiverse-Inventories! This may take a while for many players...");
                sender.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Check the console to see when it is done.");
                convertService.runConversion(sender);
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "Converting from MultiInv is unsupported at this time.");
            }
        }
    }

    @Override
    public PermissionNode getRequiredPermission() {
        return AdminPermission.CONVERT;
    }

    /**
     * Check if a plugin is both installed and enabled on the server.
     *
     * @param pluginName The plugin name to check for.
     * @return If the plugin is installed and enabled.
     */
    boolean isPluginInstalled(String pluginName) {
        if (pluginManager.getPlugin(pluginName) != null && pluginManager.isPluginEnabled(pluginName)) {
            return true;
        }
        return false;
    }
}
