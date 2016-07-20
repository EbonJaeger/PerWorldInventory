package me.gnat008.perworldinventory.commands;

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
}
