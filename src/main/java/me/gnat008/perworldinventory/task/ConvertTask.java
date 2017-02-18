package me.gnat008.perworldinventory.task;

import me.gnat008.perworldinventory.service.ConvertService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Task to convert player data from MultiVerse-Inventories to PWI.
 */
public class ConvertTask extends BukkitRunnable {

    private static final int CONVERTS_PER_TICK = 5;

    private final ConvertService convertService;
    private final OfflinePlayer[] offlinePlayers;
    private final UUID sender;

    private final int maxPage;
    private int currentPage = 0;

    public ConvertTask(ConvertService convertService, CommandSender sender, OfflinePlayer[] offlinePlayers) {
        this.convertService = convertService;
        this.offlinePlayers = offlinePlayers;
        this.maxPage = offlinePlayers.length / CONVERTS_PER_TICK;

        if (sender instanceof Player) {
            this.sender = ((Player) sender).getUniqueId();
        } else {
            this.sender = null;
        }
    }

    @Override
    public void run() {
        if (currentPage > maxPage) {
            finish();
            return;
        }

        int stopIndex = currentPage * CONVERTS_PER_TICK + CONVERTS_PER_TICK;
        int currentIndex = currentPage * CONVERTS_PER_TICK;
        List<OfflinePlayer> playersInPage = new ArrayList<>(CONVERTS_PER_TICK);

        while (currentIndex < stopIndex && currentIndex < offlinePlayers.length) {
            playersInPage.add(offlinePlayers[currentIndex]);
            currentIndex++;
        }

        currentPage++;

        convertService.executeConvert(playersInPage);
        if (currentPage % 20 == 0) {
            sendMessage("[PerWorldInventory] Convert progress: " + stopIndex + "/" + offlinePlayers.length);
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
