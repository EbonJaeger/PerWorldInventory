/*
 * Copyright (C) 2014-2015  Erufael
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.gnat008.perworldinventory.data.players;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.groups.Group;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to manage cached players.
 * Players are meant to be added when data needs to be saved, and removed
 * when the data has been saved to the database, whether it be MySQL or
 * flat files.
 */
public class PWIPlayerManager {

    private PerWorldInventory plugin;
    private int interval;
    private int taskID;

    // Key format: uuid.group.gamemode
    private Map<String, PWIPlayer> playerCache = new ConcurrentHashMap<>();

    public PWIPlayerManager(PerWorldInventory plugin) {
        this.plugin = plugin;
        int setting = Settings.getInt("save-interval");
        this.interval = (setting != -1 ? setting : 300) * 20;
        this.taskID = scheduleRepeatingTask();
    }

    /**
     * Called when the plugin is disabled.
     */
    public void onDisable() {
        Bukkit.getScheduler().cancelTask(taskID);
        playerCache.clear();
    }

    /**
     * Add a new player to the cache.
     * <p>
     * Players will be tied to the group they were in. This allows us to have
     * multiple PWIPlayers cached at the same time in case they rapidely change
     * gamemodes or worlds. We can grab data directly from this cache in case
     * they haven't been saved to the database yet.
     *
     * @param player The Player to add
     * @param group The Group the player is in
     */
    public void addPlayer(Player player, Group group) {
        String key = player.getUniqueId().toString() + "." + group.getName() + ".";
        if (Settings.getBoolean("separate-gamemode-inventories"))
            key += player.getGameMode().toString().toLowerCase();
        else
            key += "survival";

        if (playerCache.containsKey(key))
            updateCache(player, playerCache.get(key));
        else
            playerCache.put(key, new PWIPlayer(player, group));
    }

    /**
     * Get a player from the cache. This method will
     * return null if no player with the same group and gamemode
     * is cached.
     *
     * @param group The Group the player is in
     * @param player The Player
     * @return The PWIPlayer in the cache, or null
     */
    public PWIPlayer getPlayer(Group group, Player player) {
        String key = player.getUniqueId().toString() + "." + group.getName() + ".";
        if (Settings.getBoolean("separate-gamemode-inventories"))
            key += player.getGameMode().toString().toLowerCase();
        else
            key += "survival";

        return playerCache.get(key);
    }

    /**
     * Get player data from the cache and apply it to
     * the player.
     *
     * @param group The Group the player is in
     * @param gamemode The Gamemode the player is in
     * @param player The Player to get the data for
     */
    public void getPlayerData(Group group, GameMode gamemode, Player player) {
        boolean isInCache = getDataFromCache(group, gamemode, player);

        if(!isInCache)
            plugin.getSerializer().getFromDatabase(group, gamemode, player);
    }

    private boolean getDataFromCache(Group group, GameMode gamemode, Player player) {
        PWIPlayer cachedPlayer = getCachedPlayer(group, gamemode, player.getUniqueId());
        if (cachedPlayer == null)
            return false;

        if (Settings.getBoolean("player.ender-chest"))
            player.getEnderChest().setContents(cachedPlayer.getEnderChest());
        if (Settings.getBoolean("player.inventory")) {
            player.getInventory().setContents(cachedPlayer.getInventory());
            player.getInventory().setArmorContents(cachedPlayer.getArmor());
        }
        if (Settings.getBoolean("player.stats.can-fly"))
            player.setAllowFlight(cachedPlayer.getCanFly());
        if (Settings.getBoolean("player.stats.display-name"))
            player.setDisplayName(cachedPlayer.getDisplayName());
        if (Settings.getBoolean("player.stats.exhaustion"))
            player.setExhaustion(cachedPlayer.getExhaustion());
        if (Settings.getBoolean("player.stats.exp"))
            player.setExp(cachedPlayer.getExperience());
        if (Settings.getBoolean("player.stats.flying"))
            player.setFlying(cachedPlayer.isFlying());
        if (Settings.getBoolean("player.stats.food"))
            player.setFoodLevel(cachedPlayer.getFoodLevel());
        if (Settings.getBoolean("player.stats.health")) {
            if (cachedPlayer.getHealth() <= player.getMaxHealth())
                player.setHealth(cachedPlayer.getHealth());
            else
                player.setHealth(player.getMaxHealth());
        }
        if (Settings.getBoolean("player.stats.gamemode") && (!Settings.getBoolean("separate-gamemode-inventories")))
            player.setGameMode(cachedPlayer.getGamemode());
        if (Settings.getBoolean("player.stats.level"))
            player.setLevel(cachedPlayer.getLevel());
        if (Settings.getBoolean("player.stats.potion-effects")) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            player.addPotionEffects(cachedPlayer.getPotionEffects());
        }
        if (Settings.getBoolean("player.stats.saturation"))
            player.setSaturation(cachedPlayer.getSaturationLevel());
        if (Settings.getBoolean("player.stats.fall-distance"))
            player.setFallDistance(cachedPlayer.getFallDistance());
        if (Settings.getBoolean("player.stats.fire-ticks"))
            player.setFireTicks(cachedPlayer.getFireTicks());
        if (Settings.getBoolean("player.stats.max-air"))
            player.setMaximumAir(cachedPlayer.getMaxAir());
        if (Settings.getBoolean("player.stats.remaining-air"))
            player.setRemainingAir(cachedPlayer.getRemainingAir());
        if (Settings.getBoolean("player.economy")) {
            Economy econ = plugin.getEconomy();
            econ.bankWithdraw(player.getName(), econ.bankBalance(player.getName()).balance);
            econ.bankDeposit(player.getName(), cachedPlayer.getBankBalance());

            econ.withdrawPlayer(player, econ.getBalance(player));
            econ.depositPlayer(player, cachedPlayer.getBalance());
        }

        return true;
    }

    /**
     * Get a PWI player from a UUID.
     * <p>
     * This method will return null if no player is found, or if they have not been
     * saved with the Group given.
     *
     * @param group The Group to grab data from
     * @param gameMode The GameMode to get the data for
     * @return The PWIPlayer
     */
    private PWIPlayer getCachedPlayer(Group group, GameMode gameMode, UUID uuid) {
        String key = uuid.toString() + "." + group.getName() + ".";
        if (Settings.getBoolean("separate-gamemode-inventories"))
            key += gameMode.toString().toLowerCase();
        else
            key += "survival";

        return playerCache.get(key);
    }

    /**
     * Starts a synchronized repeating task to iterate through all PWIPlayers in the player
     * cache. If the player has not yet been saved to a database, they will be saved.
     * <p>
     * Additionally, if a player is still in the cache, but they have already been saved,
     * remove them from the cache.
     * <p>
     * By default, this task will execute once every 5 minutes. This will likely be
     * configurable in the future.
     *
     * @return The task ID number
     */
    private int scheduleRepeatingTask() {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (String key : playerCache.keySet()) {
                    PWIPlayer player = playerCache.get(key);
                    if (!player.isSaved()) {
                        String[] parts = key.split("\\.");
                        Group group = plugin.getGroupManager().getGroup(parts[1]);
                        GameMode gamemode = GameMode.valueOf(parts[2].toUpperCase());

                        player.setSaved(true);
                        plugin.getSerializer().saveToDatabase(group, gamemode, player, true);
                    } else {
                        playerCache.remove(key);
                    }
                }
            }
        }, interval, interval);
    }

    /**
     * Updates all the values of a player in the cache.
     *
     * @param newData The current snapshot of the Player
     * @param currentPlayer The PWIPlayer currently in the cache
     */
    public void updateCache(Player newData, PWIPlayer currentPlayer) {
        currentPlayer.setSaved(false);

        currentPlayer.setArmor(newData.getInventory().getArmorContents());
        currentPlayer.setEnderChest(newData.getEnderChest().getContents());
        currentPlayer.setInventory(newData.getInventory().getContents());

        currentPlayer.setCanFly(newData.getAllowFlight());
        currentPlayer.setDisplayName(newData.getDisplayName());
        currentPlayer.setExhaustion(newData.getExhaustion());
        currentPlayer.setExperience(newData.getExp());
        currentPlayer.setFlying(newData.isFlying());
        currentPlayer.setFoodLevel(newData.getFoodLevel());
        currentPlayer.setHealth(newData.getHealth());
        currentPlayer.setLevel(newData.getLevel());
        currentPlayer.setSaturationLevel(newData.getSaturation());
        currentPlayer.setPotionEffects(newData.getActivePotionEffects());
        currentPlayer.setFallDistance(newData.getFallDistance());
        currentPlayer.setFireTicks(newData.getFireTicks());
        currentPlayer.setMaxAir(newData.getMaximumAir());
        currentPlayer.setRemainingAir(newData.getRemainingAir());

        if (PerWorldInventory.getInstance().getEconomy() != null) {
            currentPlayer.setBankBalance(PerWorldInventory.getInstance().getEconomy().bankBalance(newData.getName()).balance);
            currentPlayer.setBalance(PerWorldInventory.getInstance().getEconomy().getBalance(newData));
        }
    }
}
