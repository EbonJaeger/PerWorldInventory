/*
 * Copyright (C) 2014-2016  Gnat008
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

package me.gnat008.perworldinventory.data;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.defaults.ConfigValues;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.database.Database;
import me.gnat008.perworldinventory.database.Operator;
import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SQLSerializer extends DataSerializer {

    private final Database database;

    public SQLSerializer(PerWorldInventory plugin) {
        super(plugin);

        this.database = plugin.getSQLDatabase();
    }

    /**
     * Save a player's data to a SQL database.
     * <p>
     * The method will check if data for the given player, group, and gamemode
     * already exists. If data does exist, it will delete the existing data,
     * and then re-insert it.
     * <p>
     * Everything is saved to the database, regardless of configuration settings.
     * All config checking will be done on data retrieval.
     *
     * @param group The {@link me.gnat008.perworldinventory.groups.Group} to save for
     * @param gamemode The {@link org.bukkit.GameMode} to save for
     * @param player The {@link me.gnat008.perworldinventory.data.players.PWIPlayer} we are saving
     */
    public void saveToDatabase(Group group, GameMode gamemode, PWIPlayer player) {
        String prefix = ConfigValues.PREFIX.getString();

        String dataUUID = checkIfDataExists(group, gamemode, player, prefix);

        if (dataUUID != null) {
            deleteExistingData(prefix, dataUUID);

            createStatements(prefix, dataUUID, player, group, gamemode, false);
        } else {
            createStatements(prefix, player, group, gamemode, true);
        }
    }

    /**
     * Query the database to see if there is any existing data for the
     * given player for a GameMode and Group.
     *
     * @param group The {@link me.gnat008.perworldinventory.groups.Group} to check for
     * @param gamemode The GameMode to check for
     * @param player The Player to check
     * @param prefix The SQL table prefix
     * @return The existing data UUID if found, or null
     */
    private String checkIfDataExists(Group group, GameMode gamemode, PWIPlayer player, String prefix) {
        String findDataQuery = database.createQuery().select("data_uuid").from(prefix + "player_data").where("uuid", Operator.EQUAL)
                .and("data_group", Operator.EQUAL).and("gamemode", Operator.EQUAL).buildQuery();
        PreparedStatement findData;
        try {
            findData = database.prepareStatement(findDataQuery);
            findData.setString(1, player.getUuid().toString());
            findData.setString(2, group.getName());
            findData.setString(3, gamemode.toString().toLowerCase());

            CachedRowSet result = database.queryDb(findData);
            if (result != null) {
                return result.getString("data_uuid");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            plugin.getLogger().severe("Unable to check for existing data for a player: " + ex.getMessage());
            return null;
        }

        return null;
    }

    /**
     * Delete existing rows from the database.
     * <p>
     * This is called when data already exists, so we can just
     * re-insert the changed data afterwards.
     *
     * @param prefix The table prefix
     * @param uuid The UUID of the data to delete
     */
    private void deleteExistingData(String prefix, String uuid) {
        final PreparedStatement deleteEnderChest, deleteArmor, deleteInventory, deleteStats, deleteEcon;

        String deleteECQuery = database.createQuery().delete().from(prefix + "ender_chests").where("data_uuid", Operator.EQUAL).buildQuery();
        String deleteArmorQuery = database.createQuery().delete().from(prefix + "armor").where("data_uuid", Operator.EQUAL).buildQuery();
        String deleteInvQuery = database.createQuery().delete().from(prefix + "inventory").where("data_uuid", Operator.EQUAL).buildQuery();
        String deleteStatsQuery = database.createQuery().delete().from(prefix + "player_stats").where("data_uuid", Operator.EQUAL).buildQuery();
        String deleteEconQuery = database.createQuery().delete().from(prefix + "economy").where("data_uuid", Operator.EQUAL).buildQuery();

        try {
            deleteEnderChest = database.prepareStatement(deleteECQuery);
            deleteEnderChest.setString(1, uuid);
            deleteArmor = database.prepareStatement(deleteArmorQuery);
            deleteArmor.setString(1, uuid);
            deleteInventory = database.prepareStatement(deleteInvQuery);
            deleteInventory.setString(1, uuid);
            deleteStats = database.prepareStatement(deleteStatsQuery);
            deleteStats.setString(1, uuid);
            deleteEcon = database.prepareStatement(deleteEconQuery);
            deleteEcon.setString(1, uuid);
        } catch (ClassNotFoundException | SQLException ex) {
            plugin.getLogger().severe("Unable to delete database rows: " + ex.getMessage());
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    database.updateDb(deleteEnderChest);
                    database.updateDb(deleteArmor);
                    database.updateDb(deleteInventory);
                    database.updateDb(deleteStats);
                    database.updateDb(deleteEcon);
                } catch (ClassNotFoundException | SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the {@link java.sql.PreparedStatement}s to write a
     * player's data to the database. This method saves everything
     * regardless of the configuration options.
     * <p>
     * This method creates a new, random UUID for the data.
     *
     * @param prefix The SQL table prefix
     * @param player The {@link me.gnat008.perworldinventory.data.players.PWIPlayer} to grab data from
     * @param group The {@link me.gnat008.perworldinventory.groups.Group} the player is in
     * @param gamemode The {@link org.bukkit.GameMode} we are saving
     * @param createIndexEntry If an index entry needs to be created
     */
    private void createStatements(String prefix, PWIPlayer player, Group group, GameMode gamemode, boolean createIndexEntry) {
        String dataUUID = UUID.randomUUID().toString();

        createStatements(prefix, dataUUID, player, group, gamemode, createIndexEntry);
    }

    /**
     * Create the {@link java.sql.PreparedStatement}s to write a
     * player's data to the database. This method saves everything
     * regardless of the configuration options.
     *
     * @param prefix The SQL table prefix
     * @param dataUUID The UUID to use to tie data together
     * @param player The {@link me.gnat008.perworldinventory.data.players.PWIPlayer} to grab data from
     * @param group The {@link me.gnat008.perworldinventory.groups.Group} the player is in
     * @param gamemode The {@link org.bukkit.GameMode} we are saving
     * @param createIndexEntry If an index entry needs to be created
     */
    private void createStatements(String prefix, String dataUUID, PWIPlayer player, Group group, GameMode gamemode, boolean createIndexEntry) {
        Set<PreparedStatement> statements = new HashSet<>();
        if (createIndexEntry)
            statements.add(createIndexEntry(prefix, dataUUID, player.getUuid().toString(), group, gamemode));
        PreparedStatement insEnderChest, insArmor, insInv, insStats, insEcon;
        String insEnderChestQuery, insArmorQuery, insInvQuery, insStatsQuery, insEconQuery = null;
        insEnderChestQuery = database.createQuery().insertInto(prefix + "ender_chests").values(3).buildQuery();
        insArmorQuery = database.createQuery().insertInto(prefix + "armor").values(3).buildQuery();
        insInvQuery = database.createQuery().insertInto(prefix + "inventory").values(3).buildQuery();
        insStatsQuery = database.createQuery().insertInto(prefix + "player_stats").values(13).buildQuery();
        if (ConfigValues.ECONOMY.getBoolean())
            insEconQuery = database.createQuery().insertInto(prefix + "economy").values(4).buildQuery();

        try {
            insEnderChest = database.prepareStatement(insEnderChestQuery);
            insEnderChest.setNull(1, Types.INTEGER);
            insEnderChest.setString(2, dataUUID);
            Blob ec = new SerialBlob(player.getEnderChest().toString().getBytes());
            insEnderChest.setBlob(3, ec);
            statements.add(insEnderChest);

            insArmor = database.prepareStatement(insArmorQuery);
            insArmor.setNull(1, Types.INTEGER);
            insArmor.setString(2, dataUUID);
            Blob armor = new SerialBlob(player.getArmor().toString().getBytes());
            insArmor.setBlob(3, armor);
            statements.add(insArmor);

            insInv = database.prepareStatement(insInvQuery);
            insInv.setNull(1, Types.INTEGER);
            insInv.setString(2, dataUUID);
            Blob inv = new SerialBlob(player.getInventory().toString().getBytes());
            insInv.setBlob(3, inv);
            statements.add(insInv);

            insStats = database.prepareStatement(insStatsQuery);
            insStats.setNull(1, Types.INTEGER);
            insStats.setString(2, dataUUID);
            insStats.setBoolean(3, player.getCanFly());
            insStats.setString(4, player.getDisplayName());
            insStats.setFloat(5, player.getExhaustion());
            insStats.setFloat(6, player.getExperience());
            insStats.setBoolean(7, player.isFlying());
            insStats.setInt(8, player.getFoodLevel());
            insStats.setDouble(9, player.getHealth());
            insStats.setString(10, player.getGamemode().toString().toLowerCase());
            insStats.setInt(11, player.getLevel());
            insStats.setBytes(12, player.getPotionEffects().toString().getBytes());
            insStats.setFloat(13, player.getSaturationLevel());
            statements.add(insStats);

            if (ConfigValues.ECONOMY.getBoolean() && insEconQuery != null) {
                insEcon = database.prepareStatement(insEconQuery);
                insEcon.setNull(1, Types.INTEGER);
                insEcon.setString(2, dataUUID);
                insEcon.setDouble(3, player.getBankBalance());
                insEcon.setDouble(4, player.getBalance());
                statements.add(insEcon);
            }

            updateDatabase(statements);
        } catch (ClassNotFoundException | SQLException ex) {
            plugin.getLogger().severe("There was an error inserting stats for '" + player.getName() + "': " + ex.getMessage());
        }
    }

    private PreparedStatement createIndexEntry(String prefix, String dataUUID, String playerUUID, Group group, GameMode gamemode) {
        String query = database.createQuery().insertInto(prefix + "player_data").values(5).buildQuery();
        try {
            PreparedStatement statement = database.prepareStatement(query);
            statement.setNull(1, Types.INTEGER);
            statement.setString(2, playerUUID);
            statement.setString(3, group.getName());
            statement.setString(4, gamemode.toString().toLowerCase());
            statement.setString(5, dataUUID);
            return statement;
        } catch (ClassNotFoundException | SQLException ex) {
            plugin.getLogger().severe("There was an error creating a player index entry: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Executes a bunch of {@link java.sql.PreparedStatement}s.
     *
     * @param statements The PreparedStatements to execute
     */
    private void updateDatabase(final Set<PreparedStatement> statements) {
        try {
            for (PreparedStatement statement : statements) {
                database.updateDb(statement);
            }
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void getFromDatabase(Group group, GameMode gamemode, Player player) {
        //TODO: Implement getting data from the database
    }
}
