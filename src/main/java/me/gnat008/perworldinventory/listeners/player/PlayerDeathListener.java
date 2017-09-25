package me.gnat008.perworldinventory.listeners.player;

import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;

import javax.inject.Inject;

/**
 * Listens for any {@link PlayerDeathEvent} in order to prevent
 * inventory duplication.
 */
public class PlayerDeathListener implements Listener {

    private GroupManager groupManager;
    private PWIPlayerManager playerManager;

    @Inject
    PlayerDeathListener(GroupManager groupManager, PWIPlayerManager playerManager) {
        this.groupManager = groupManager;
        this.playerManager = playerManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Group group = groupManager.getGroupFromWorld(player.getLocation().getWorld().getName());

        if (!event.getKeepInventory()) {
            player.getInventory().clear();
        }

        if (!event.getKeepLevel()) {
            player.setExp(event.getNewExp());
            player.setLevel(event.getNewLevel());
        }

        player.setFoodLevel(20);
        player.setSaturation(5f);
        player.setExhaustion(0f);
        player.setFallDistance(0f);
        player.setFireTicks(0);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        playerManager.addPlayer(player, group);
    }
}
