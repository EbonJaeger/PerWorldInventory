package me.gnat008.perworldinventory.task;

import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.service.ConvertService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Task to convert player data from MultiVerse-Inventories to PWI.
 */
public class ConvertTask extends BukkitRunnable {

    private static final int CONVERTS_PER_TICK = 5;

    private final ConvertService convertService;
    private final int totalConvertCount;
    private final OfflinePlayer[] offlinePlayers;
    private final Set<OfflinePlayer> toConvert;
    private final UUID sender;

    private int currentPage = 0;

    public ConvertTask(ConvertService convertService, CommandSender sender,
                       OfflinePlayer[] offlinePlayers, Set<OfflinePlayer> toConvert) {
        this.convertService = convertService;
        this.offlinePlayers = offlinePlayers;
        this.toConvert = toConvert;
        this.totalConvertCount = toConvert.size();

        if (sender instanceof Player) {
            this.sender = ((Player) sender).getUniqueId();
        } else {
            this.sender = null;
        }
    }

    @Override
    public void run() {
        if (toConvert.isEmpty()) {
            finish();
            return;
        }

        Set<OfflinePlayer> playerPortion = new HashSet<>(CONVERTS_PER_TICK);

        for (int i = 0; i < CONVERTS_PER_TICK; i++) {
            int nextPosition = (currentPage * CONVERTS_PER_TICK) + i;
            if (offlinePlayers.length <= nextPosition) {
                // There are no more players on this page
                break;
            }

            OfflinePlayer offlinePlayer = offlinePlayers[nextPosition];
            if (offlinePlayer.getName() != null) {
                playerPortion.add(offlinePlayer);
            }

            if (!toConvert.isEmpty() && playerPortion.isEmpty()) {
                PwiLogger.info("Finished lookup of offline players.");

                toConvert.clear();
            }

            currentPage++;

            convertService.executeConvert(playerPortion);
            if (currentPage % 20 == 0) {
                int completed = totalConvertCount - toConvert.size();
                sendMessage("[PerWorldInventory] Convert progress: " + completed + "/" + totalConvertCount);
            }
        }
    }

    private void finish() {
        cancel();

        sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + " Conversion has been completed! Disabling Multiverse-Inventories...");

        convertService.disableMVI();
        sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY + "Multiverse-Inventories disabled! Don't forget to remove the .jar!");

        convertService.setConverting(false);
    }

    private void sendMessage(String message) {
        if (sender == null) {
            Bukkit.getConsoleSender().sendMessage(message);
        } else {
            Player player = Bukkit.getPlayer(sender);
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.GOLD + message);
            }
        }
    }
}
