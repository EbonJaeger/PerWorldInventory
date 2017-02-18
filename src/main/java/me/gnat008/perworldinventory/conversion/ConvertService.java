package me.gnat008.perworldinventory.conversion;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.service.BukkitService;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;

import javax.inject.Inject;
import java.util.*;

/**
 * Initiates conversion tasks.
 */
public class ConvertService {

    private BukkitService bukkitService;
    private ConvertExecutor convertExecutor;
    private GroupManager groupManager;
    private PluginManager pluginManager;

    private boolean isConverting;

    @Inject
    ConvertService(BukkitService bukkitService, ConvertExecutor convertExecutor,
                   GroupManager groupManager, PluginManager pluginManager) {
        this.bukkitService = bukkitService;
        this.convertExecutor = convertExecutor;
        this.groupManager = groupManager;
        this.pluginManager = pluginManager;
    }

    public void runConversion(CommandSender sender) {
        MultiverseInventories mvinventories = (MultiverseInventories) pluginManager.getPlugin("Multiverse-Inventories");
        if (mvinventories == null) {
            logAndSendWarning(sender, "MultiVerse-Inventories is not installed! Cannot convert; aborting.");
            return;
        }

        OfflinePlayer[] offlinePlayers = bukkitService.getOfflinePlayers();
        convertPlayers(sender, offlinePlayers, mvinventories);
    }

    private void convertPlayers(CommandSender sender, OfflinePlayer[] offlinePlayers, MultiverseInventories mvi) {
        if (isConverting) {
            logAndSendWarning(sender, "Conversion is already in progress!");
            return;
        }

        isConverting = true;
        convertExecutor.setMultiverseGroups(mvi);

        List<WorldGroupProfile> mvgroups = mvi.getGroupManager().getGroups();
        for (WorldGroupProfile mvgroup : mvgroups) {
            //Ensure that the group exists first, otherwise you get nulls down the road
            Group pwiGroup = groupManager.getGroup(mvgroup.getName());
            List<String> worlds = new ArrayList<>(mvgroup.getWorlds());

            if (pwiGroup == null) {
                groupManager.addGroup(mvgroup.getName(), worlds);
            } else {
                pwiGroup.addWorlds(worlds);
            }
        }

        ConvertTask convertTask = new ConvertTask(this, sender, offlinePlayers);
        bukkitService.runRepeatingTaskAsynchronously(convertTask, 0, 1);
    }

    /**
     * Set if a conversion operation is in progress.
     *
     * @param converting Conversion state.
     */
    void setConverting(boolean converting) {
        this.isConverting = converting;
    }

    void disableMVI() {
        MultiverseInventories mvinventories = (MultiverseInventories) pluginManager.getPlugin("Multiverse-Inventories");
        if (mvinventories != null && pluginManager.isPluginEnabled(mvinventories)) {
            pluginManager.disablePlugin(mvinventories);
        }
    }

    void executeConvert(Collection<OfflinePlayer> batch) {
        batch.forEach(convertExecutor::executeConvert);
    }

    private void logAndSendWarning(CommandSender sender, String message) {
        PwiLogger.warning(message);

        if (sender != null && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatColor.RED + "[PerWorldInventory] " + message);
        }
    }
}
