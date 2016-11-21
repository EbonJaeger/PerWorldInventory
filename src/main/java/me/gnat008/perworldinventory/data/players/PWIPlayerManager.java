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
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.DataWriter;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to manage cached players.
 * Players are meant to be added when data needs to be saved, and removed
 * when the data has been saved to the database, whether it be MySQL or
 * flat files.
 */
public class PWIPlayerManager {

    private PerWorldInventory plugin;
    private DataWriter dataWriter;
    private GroupManager groupManager;
    private PWIPlayerFactory pwiPlayerFactory;
    private Settings settings;

    private int interval;
    private int taskID;

    // Key format: uuid.group.gamemode
    private Map<String, PWIPlayer> playerCache = new ConcurrentHashMap<>();

    @Inject
    PWIPlayerManager(PerWorldInventory plugin, DataWriter dataWriter, GroupManager groupManager,
                     PWIPlayerFactory pwiPlayerFactory, Settings settings) {
        this.plugin = plugin;
        this.dataWriter = dataWriter;
        this.groupManager = groupManager;
        this.pwiPlayerFactory = pwiPlayerFactory;
        this.settings = settings;

        int setting = settings.getProperty(PwiProperties.SAVE_INTERVAL);
        this.interval = (setting != -1 ? setting : 300) * 20;
    }

    /**
     * Called when the server is disabled.
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
     *
     * @return The key used to get the player data.
     */
    public String addPlayer(Player player, Group group) {
        String key = player.getUniqueId().toString() + "." + group.getName() + ".";
        if (settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES))
            key += player.getGameMode().toString().toLowerCase();
        else
            key += "survival";

        PwiLogger.debug("Adding player '" + player.getName() + "' to cache; key is '" + key + "'");

        if (playerCache.containsKey(key)) {
            PwiLogger.debug("Player '" + player.getName() + "' found in cache! Updating cache");
            updateCache(player, playerCache.get(key));
        } else {
            playerCache.put(key, pwiPlayerFactory.create(player, group));
        }

        return key;
    }

    /**
     * Removes a player from the cache. They key will be made from the player's UUID,
     * the group they are in, and the gamemode of the player.
     *
     * @param player The player to remove from the cache
     */
    public void removePlayer(Player player) {
        for (String key : playerCache.keySet()) {
            if (key.startsWith(player.getUniqueId().toString())) {
                playerCache.remove(key);
            }
        }
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
        if (settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES))
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
        PwiLogger.debug("Trying to get data from cache for player '" + player.getName() + "'");

        if(isPlayerCached(group, gamemode, player)) {
            getDataFromCache(group, gamemode, player);
        } else {
            PwiLogger.debug("Player was not in cache! Loading from file");
            dataWriter.getFromDatabase(group, gamemode, player);
        }
    }

    /**
     * Save all cached instances of a player to the disk.
     *
     * @param group The Group the player is currently in.
     * @param player The player to save.
     */
    public void savePlayer(Group group, Player player) {
        String key = player.getUniqueId().toString() + "." + group.getName() + ".";
        if (settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES))
            key += player.getGameMode().toString().toLowerCase();
        else
            key += "survival";

        // Remove any entry with the current key, if one exists
        // Should remove the possibility of having to write the same data twice
        playerCache.remove(key);

        for (String cachedKey : playerCache.keySet()) {
            if (cachedKey.startsWith(player.getUniqueId().toString())) {
                PWIPlayer cached = playerCache.get(cachedKey);
                if (cached.isSaved()) {
                    continue;
                }

                String[] parts = cachedKey.split("\\.");
                Group groupKey = groupManager.getGroup(parts[1]);
                GameMode gamemode = GameMode.valueOf(parts[2].toUpperCase());

                PwiLogger.debug("Saving cached player '" + cached.getName() + "' for group '" + groupKey.getName() + "' with gamemdde '" + gamemode.name() + "'");

                cached.setSaved(true);
                dataWriter.saveToDatabase(groupKey, gamemode, cached, true);
            }
        }

        PWIPlayer pwiPlayer = pwiPlayerFactory.create(player, group);
        dataWriter.saveToDatabase(group,
                settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES) ? player.getGameMode() : GameMode.SURVIVAL,
                pwiPlayer,
                true);
        dataWriter.saveLogoutData(pwiPlayer);
        removePlayer(player);
    }

    /**
     * Return whether a player in a given group is currently cached.
     *
     * @param group The group the player was in.
     * @param player The player to check for.
     *
     * @return True if a {@link PWIPlayer} is cached.
     */
    public boolean isPlayerCached(Group group, GameMode gameMode, Player player) {
        String key = player.getUniqueId().toString() + "." + group.getName() + ".";
        if (settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES))
            key += gameMode.toString().toLowerCase();
        else
            key += "survival";

        return playerCache.containsKey(key);
    }

    private void getDataFromCache(Group group, GameMode gamemode, Player player) {
        PWIPlayer cachedPlayer = getCachedPlayer(group, gamemode, player.getUniqueId());
        if (cachedPlayer == null) {
            PwiLogger.debug("No data for player '" + player.getName() + "' found in cache");

            return;
        }

        PwiLogger.debug("Player '" + player.getName() + "' found in cache! Setting their data");

        if (settings.getProperty(PwiProperties.LOAD_ENDER_CHESTS))
            player.getEnderChest().setContents(cachedPlayer.getEnderChest());
        if (settings.getProperty(PwiProperties.LOAD_INVENTORY)) {
            player.getInventory().setContents(cachedPlayer.getInventory());
            player.getInventory().setArmorContents(cachedPlayer.getArmor());
        }
        if (settings.getProperty(PwiProperties.LOAD_CAN_FLY))
            player.setAllowFlight(cachedPlayer.getCanFly());
        if (settings.getProperty(PwiProperties.LOAD_DISPLAY_NAME))
            player.setDisplayName(cachedPlayer.getDisplayName());
        if (settings.getProperty(PwiProperties.LOAD_EXHAUSTION))
            player.setExhaustion(cachedPlayer.getExhaustion());
        if (settings.getProperty(PwiProperties.LOAD_EXP))
            player.setExp(cachedPlayer.getExperience());
        if (settings.getProperty(PwiProperties.LOAD_FLYING))
            player.setFlying(cachedPlayer.isFlying());
        if (settings.getProperty(PwiProperties.LOAD_HUNGER))
            player.setFoodLevel(cachedPlayer.getFoodLevel());
        if (settings.getProperty(PwiProperties.LOAD_HEALTH)) {
            if (cachedPlayer.getHealth() <= player.getMaxHealth())
                player.setHealth(cachedPlayer.getHealth());
            else
                player.setHealth(player.getMaxHealth());
        }
        if (settings.getProperty(PwiProperties.LOAD_GAMEMODE) && (!settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)))
            player.setGameMode(cachedPlayer.getGamemode());
        if (settings.getProperty(PwiProperties.LOAD_LEVEL))
            player.setLevel(cachedPlayer.getLevel());
        if (settings.getProperty(PwiProperties.LOAD_POTION_EFFECTS)) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            player.addPotionEffects(cachedPlayer.getPotionEffects());
        }
        if (settings.getProperty(PwiProperties.LOAD_SATURATION))
            player.setSaturation(cachedPlayer.getSaturationLevel());
        if (settings.getProperty(PwiProperties.LOAD_FALL_DISTANCE))
            player.setFallDistance(cachedPlayer.getFallDistance());
        if (settings.getProperty(PwiProperties.LOAD_FIRE_TICKS))
            player.setFireTicks(cachedPlayer.getFireTicks());
        if (settings.getProperty(PwiProperties.LOAD_MAX_AIR))
            player.setMaximumAir(cachedPlayer.getMaxAir());
        if (settings.getProperty(PwiProperties.LOAD_REMAINING_AIR))
            player.setRemainingAir(cachedPlayer.getRemainingAir());
        if (settings.getProperty(PwiProperties.USE_ECONOMY)) {
            Economy econ = plugin.getEconomy();
            econ.bankWithdraw(player.getName(), econ.bankBalance(player.getName()).balance);
            econ.bankDeposit(player.getName(), cachedPlayer.getBankBalance());

            econ.withdrawPlayer(player, econ.getBalance(player));
            econ.depositPlayer(player, cachedPlayer.getBalance());
        }
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
        if (settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES))
            key += gameMode.toString().toLowerCase();
        else
            key += "survival";

        PwiLogger.debug("Looking for cached data with key '" + key + "'");

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
     */
    @PostConstruct
    private void scheduleRepeatingTask() {
        this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (String key : playerCache.keySet()) {
                PWIPlayer player = playerCache.get(key);
                if (!player.isSaved()) {
                    String[] parts = key.split("\\.");
                    Group group = groupManager.getGroup(parts[1]);
                    GameMode gamemode = GameMode.valueOf(parts[2].toUpperCase());

                    PwiLogger.debug("Saving cached player '" + player.getName() + "' for group '" + group.getName() + "' with gamemdde '" + gamemode.name() + "'");

                    player.setSaved(true);
                    dataWriter.saveToDatabase(group, gamemode, player, true);
                } else {
                    PwiLogger.debug("Removing player '" + player.getName() + "' from cache");
                    playerCache.remove(key);
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
        PwiLogger.debug("Updating player '" + newData.getName() + "' in the cache");

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

        if (plugin.getEconomy() != null) {
            currentPlayer.setBankBalance(plugin.getEconomy().bankBalance(newData.getName()).balance);
            currentPlayer.setBalance(plugin.getEconomy().getBalance(newData));
        }
    }
}
