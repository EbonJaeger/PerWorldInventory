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
import me.gnat008.perworldinventory.config.defaults.ConfigValues;
import me.gnat008.perworldinventory.data.DataSerializer;
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
    private int taskID;

    private Map<Group, Set<PWIPlayer>> playerCache = new ConcurrentHashMap<>();

    public PWIPlayerManager(PerWorldInventory plugin) {
        this.plugin = plugin;

        this.taskID = scheduleRepeatingTask();
    }

    /**
     * Called when the plugin is disabled.
     * <p>
     * This method immediately saves any player to the database if they
     * have not already been saved.
     */
    public void onDisable() {
        Bukkit.getScheduler().cancelTask(taskID);

        for (Group group : playerCache.keySet()) {
            for (PWIPlayer player : playerCache.get(group)) {
                if (!player.isSaved()) {
                    player.setSaved(true);
                    plugin.getSerializer().saveToDatabase(
                            group,
                            ConfigValues.SEPARATE_GAMEMODE_INVENTORIES.getBoolean() ? player.getGamemode() : GameMode.SURVIVAL,
                            player
                    );
                }
            }
        }

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
        Set<PWIPlayer> players = playerCache.get(group);
        if (players == null) {
            players = new HashSet<>();

            players.add(new PWIPlayer(player, group));
            playerCache.put(group, players);
        } else {
            PWIPlayer cachedPlayer = getCachedPlayer(group, player.getGameMode(), player.getUniqueId());
            if (cachedPlayer != null) {
                updateCache(player, cachedPlayer);
            } else {
                players.add(new PWIPlayer(player, group));
                playerCache.put(group, players);
            }
        }
    }

    /**
     * Removes a player from the cache.
     * <p>
     * This method will only remove a player with the given GameMode, leaving
     * any other instances of the same player alone.
     *
     * @param group The {@link me.gnat008.perworldinventory.groups.Group} to remove
     * @param gameMode The {@link org.bukkit.GameMode} to remove
     */
    public void removePlayer(Group group, GameMode gameMode, UUID uuid) {
        if (playerCache.containsKey(group)) {
            Set<PWIPlayer> players = playerCache.get(group);
            Iterator<PWIPlayer> itr = players.iterator();
            while (itr.hasNext()) {
                PWIPlayer cachedPlayer = itr.next();
                if (cachedPlayer.getUuid().equals(uuid) && cachedPlayer.getGamemode() == gameMode) {
                    itr.remove();
                }
            }
        }
    }

    public void getPlayerData(Group group, GameMode gamemode, Player player) {
        boolean isInCache = getDataFromCache(group, gamemode, player);

        if(!isInCache) {
            plugin.getSerializer().getFromDatabase(group, gamemode, player);
        }
    }

    private boolean getDataFromCache(Group group, GameMode gamemode, Player player) {
        PWIPlayer cachedPlayer = getCachedPlayer(group, gamemode, player.getUniqueId());
        if (cachedPlayer == null)
            return false;

        if (ConfigValues.ENDER_CHEST.getBoolean())
            player.getEnderChest().setContents(cachedPlayer.getEnderChest());
        if (ConfigValues.INVENTORY.getBoolean()) {
            player.getInventory().setContents(cachedPlayer.getInventory());
            player.getInventory().setArmorContents(cachedPlayer.getArmor());
        }
        if (ConfigValues.STATS.getBoolean()) {
            if (ConfigValues.CAN_FLY.getBoolean())
                player.setAllowFlight(cachedPlayer.getCanFly());
            if (ConfigValues.DISPLAY_NAME.getBoolean())
                player.setDisplayName(cachedPlayer.getDisplayName());
            if (ConfigValues.EXHAUSTION.getBoolean())
                player.setExhaustion(cachedPlayer.getExhaustion());
            if (ConfigValues.EXP.getBoolean())
                player.setExp(cachedPlayer.getExperience());
            if (ConfigValues.FLYING.getBoolean())
                player.setFlying(cachedPlayer.isFlying());
            if (ConfigValues.FOOD.getBoolean())
                player.setFoodLevel(cachedPlayer.getFoodLevel());
            if (ConfigValues.HEALTH.getBoolean()) {
                if (cachedPlayer.getHealth() <= player.getMaxHealth())
                    player.setHealth(cachedPlayer.getHealth());
                else
                    player.setHealth(player.getMaxHealth());
            }
            if (ConfigValues.GAMEMODE.getBoolean() && (!ConfigValues.SEPARATE_GAMEMODE_INVENTORIES.getBoolean()))
                player.setGameMode(cachedPlayer.getGamemode());
            if (ConfigValues.LEVEL.getBoolean())
                player.setLevel(cachedPlayer.getLevel());
            if (ConfigValues.POTION_EFFECTS.getBoolean()) {
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                player.addPotionEffects(cachedPlayer.getPotionEffects());
            }
            if (ConfigValues.SATURATION.getBoolean())
                player.setSaturation(cachedPlayer.getSaturationLevel());
        }
        if (ConfigValues.ECONOMY.getBoolean()) {
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
        if (playerCache.containsKey(group)) {
            Set<PWIPlayer> players = playerCache.get(group);
            for (PWIPlayer player : players) {
                if (player.getUuid().equals(uuid) &&
                        player.getGamemode() == gameMode ||
                        !ConfigValues.SEPARATE_GAMEMODE_INVENTORIES.getBoolean())
                    return player;
            }
        }

        return null;
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
                for (final Group group : playerCache.keySet()) {
                    Set<PWIPlayer> players = playerCache.get(group);
                    Iterator<PWIPlayer> itr = players.iterator();
                    while (itr.hasNext()) {
                        plugin.getLogger().info("Iterator has next!");
                        final PWIPlayer player = itr.next();
                        if (!player.isSaved()) {
                            plugin.getLogger().info(player.getName() + " is not saved!");
                            player.setSaved(true);
                            plugin.getSerializer().saveToDatabase(group, player.getGamemode(), player);
                        } else {
                            plugin.getLogger().info("Removing " + player.getName() + " from cache!");
                            itr.remove();
                        }
                    }
                }
            }
        }, 6000L, 6000L);
    }

    /**
     * Updates all the values of a player in the cache.
     *
     * @param newData The current snapshot of the Player
     * @param currentPlayer The PWIPlayer currently in the cache
     */
    private void updateCache(Player newData, PWIPlayer currentPlayer) {
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

        if (PerWorldInventory.getInstance().getEconomy() != null) {
            currentPlayer.setBankBalance(PerWorldInventory.getInstance().getEconomy().bankBalance(newData.getName()).balance);
            currentPlayer.setBalance(PerWorldInventory.getInstance().getEconomy().getBalance(newData));
        }
    }
}
