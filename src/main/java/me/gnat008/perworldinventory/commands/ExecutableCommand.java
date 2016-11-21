package me.gnat008.perworldinventory.commands;

import me.gnat008.perworldinventory.permission.PermissionNode;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface ExecutableCommand {

    /**
     * Executes the command with the given arguments.
     *
     * @param sender The person performing the command.
     * @param args The arguments given by the sender.
     */
    void executeCommand(CommandSender sender, List<String> args);

    /**
     * Returns the permission required to execute this command, or null if it is not restricted.
     *
     * @return the required permission node, or null
     */
    default PermissionNode getRequiredPermission() {
        return null;
    }
}
