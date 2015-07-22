/*
 * Copyright (C) 2014-2015  Gnat008
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

package me.gnat008.perworldinventory.data.mysql;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.defaults.ConfigValues;
import org.json.JSONObject;

import java.sql.*;
import java.util.UUID;

public class MySQLManager {

    private static MySQLManager instance = null;

    private final MySQL mySQL;
    private final String prefix;

    private PerWorldInventory plugin;

    private MySQLManager() {
        this.plugin = PerWorldInventory.getInstance();
        mySQL = MySQL.getInstance();
        prefix = ConfigValues.PREFIX.getString();
    }

    /**
     * Get the Singleton instance of the MySQLManager. If it
     * has not already been initialized, it will be initialized.
     *
     * @return MySQLManager instance
     */
    public static MySQLManager getInstance() {
        if (instance == null) {
            instance = new MySQLManager();
        }

        return instance;
    }

    /**
     * Disables MySQL and closes the database pool, as well as
     * sets the static instance variable to null.
     */
    public void disable() {
        mySQL.disable();
        instance = null;
    }

    /**
     * Starts the database connection pool.
     *
     * @throws SQLException If an exception is encountered
     */
    public void startConnection() throws SQLException {
        MySQL.getInstance().initalizeDbPool();
        if (mySQL.isConnected()) {
            plugin.getLogger().info("Connected to MySQL database!");
        }

        setupTables();
    }

    /**
     * Puts player information into the database. Called when
     * MySQL is enabled, and they change worlds, gamemode, or log
     * off. Method checks for existing data. In the event of existing
     * data, most of it will be updated instead of inserted.
     *
     * @param uuid Player's UUID
     * @param group Group the Player was in
     * @param gamemode Player's GameMode
     * @param data Player data serialized as a JSONObject
     * @throws SQLException If an exception is encountered while updating the database
     */
    public void updateDatabase(String uuid, String group, String gamemode, JSONObject data) throws SQLException {

        // Check if data for the same player, group, and gamemode already exist
        PreparedStatement query = mySQL.prepareStatement(
                "SELECT id FROM `" + prefix + "data` WHERE `uuid` = ? AND `group` = ? AND `gamemode` = ?;"
        );
        query.setString(1, uuid);
        query.setString(2, group);
        query.setString(3, gamemode);

        ResultSet check = mySQL.query(query);
        if (check != null && check.next()) {
            // Data exists, UPDATE instead of INSERT
            String data_uuid = check.getString("data_uuid");
            updateExising(data_uuid, data);
        } else {
            // Pre-existing data was not found, INSERT into the table
            String data_uuid = UUID.randomUUID().toString();
            insertData(data_uuid, data);
        }
    }

    /**
     * Insert Player data into the database.
     *
     * @param data_uuid The generated UUID to identify the correct data
     * @param data The Player data in JSON form
     * @throws SQLException If an exception is encountered
     */
    private void insertData(String data_uuid, JSONObject data) throws SQLException {
        if (data.has("ender-chest")) {
            PreparedStatement insertEnderChest = mySQL.prepareStatement(
                    "INSERT INTO `" + prefix + "ender_chest` VALUES (NULL, ?, ?);"
            );
            insertEnderChest.setString(1, data_uuid);
            insertEnderChest.setBytes(2, data.getJSONArray("ender-chest").toString().getBytes());
            mySQL.execute(insertEnderChest);
        }

        if (data.has("inventory")) {
            byte[] armor = data.getJSONObject("inventory").getJSONArray("armor").toString().getBytes();
            byte[] inventory = data.getJSONObject("inventory").getJSONArray("inventory").toString().getBytes();

            PreparedStatement insertArmor = mySQL.prepareStatement(
                    "INSERT INTO `" + prefix + "armor` VALUES (NULL, ?, ?);"
            );
            insertArmor.setString(1, data_uuid);
            insertArmor.setBytes(2, armor);
            mySQL.execute(insertArmor);

            PreparedStatement insertInventory = mySQL.prepareStatement(
                    "INSERT INTO `" + prefix + "inventory` VALUES (NULL, ?, ?);"
            );
            insertArmor.setString(1, data_uuid);
            insertArmor.setBytes(2, inventory);
            mySQL.execute(insertInventory);
        }

        if (data.has("stats")) {
            JSONObject stats = data.getJSONObject("stats");
            PreparedStatement insertStats = mySQL.prepareStatement(
                    "INSERT INTO `" + prefix + "player_stats` VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
            );
            insertStats.setString(1, data_uuid);
            insertStats.setBoolean(2, stats.has("can-fly") ? stats.getBoolean("can-fly") : null);
            insertStats.setString(3, stats.has("display-name") ? stats.getString("display-name") : null);
            insertStats.setFloat(4, stats.has("exhaustion") ? (float) stats.getDouble("exhaustion") : null);
            insertStats.setFloat(5, stats.has("exp") ? (float) stats.getDouble("exp") : null);
            insertStats.setBoolean(6, stats.has("flying") ? stats.getBoolean("flying") : null);
            insertStats.setInt(7, stats.has("food") ? stats.getInt("food") : null);
            insertStats.setDouble(8, stats.has("health") ? stats.getDouble("health") : null);
            insertStats.setInt(9, stats.has("level") ? stats.getInt("level") : null);
            insertStats.setBytes(10, stats.has("potion-effects") ? stats.getString("potion-effects").getBytes() : null);
            insertStats.setInt(11, stats.has("saturation") ? stats.getInt("saturation") : null);
            mySQL.execute(insertStats);
        }

        if (data.has("economy")) {
            JSONObject econ = data.getJSONObject("economy");
            PreparedStatement insertEcon = mySQL.prepareStatement(
                    "INSERT INTO `" + prefix + "economy` VALUES (NULL, ?, ?, ?);"
            );
            insertEcon.setString(1, data_uuid);
            insertEcon.setDouble(2, econ.has("bank-balance") ? econ.getDouble("bank-balance") : null);
            insertEcon.setDouble(3, econ.getDouble("balance"));
            mySQL.execute(insertEcon);
        }
    }

    /**
     * Updates already existing Player data in the database. In some
     * tables, rows are deleted and re-inserted because it's easier.
     *
     * @param data_uuid UUID of the data
     * @param data The data to update
     * @throws SQLException If an exception is encountered
     */
    private void updateExising(String data_uuid, JSONObject data) throws SQLException {
        if (data.has("ender-chest")) {
            PreparedStatement updateEnderChest = mySQL.prepareStatement(
                    "UPDATE `" + prefix + "ender_chests` SET `items` = ? WHERE `data_uuid` = ?;"
            );
            updateEnderChest.setBytes(1, data.getJSONArray("ender-chest").toString().getBytes());
            updateEnderChest.setString(2, data_uuid);
            mySQL.execute(updateEnderChest);
        }

        if (data.has("inventory")) {
            PreparedStatement updateArmor = mySQL.prepareStatement(
                    "UPDATE `" + prefix + "armor` SET `items` = ? WHERE `data_uuid` = ?;"
            );
            updateArmor.setBytes(1, data.getJSONObject("inventory").getJSONArray("armor").toString().getBytes());
            updateArmor.setString(2, data_uuid);

            PreparedStatement updateInventory = mySQL.prepareStatement(
                    "UPDATE `" + prefix + "inventory` SET `items` = ? WHERE `data_uuid` = ?;"
            );
            updateArmor.setBytes(1, data.getJSONObject("inventory").getJSONArray("inventory").toString().getBytes());
            updateArmor.setString(2, data_uuid);

            mySQL.execute(updateArmor);
            mySQL.execute(updateInventory);
        }

        if (data.has("stats")) {
            JSONObject stats = data.getJSONObject("stats");
            PreparedStatement deleteRow = mySQL.prepareStatement(
                    "DELETE FROM `" + prefix + "player_stats` WHERE `data_uuid` = ?;"
            );
            deleteRow.setString(1, data_uuid);
            mySQL.execute(deleteRow);

            PreparedStatement insertStats = mySQL.prepareStatement(
                    "INSERT INTO `" + prefix + "player_stats` VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
            );
            insertStats.setString(1, data_uuid);
            insertStats.setBoolean(2, stats.has("can-fly") ? stats.getBoolean("can-fly") : null);
            insertStats.setString(3, stats.has("display-name") ? stats.getString("display-name") : null);
            insertStats.setFloat(4, stats.has("exhaustion") ? (float) stats.getDouble("exhaustion") : null);
            insertStats.setFloat(5, stats.has("exp") ? (float) stats.getDouble("exp") : null);
            insertStats.setBoolean(6, stats.has("flying") ? stats.getBoolean("flying") : null);
            insertStats.setInt(7, stats.has("food") ? stats.getInt("food") : null);
            insertStats.setDouble(8, stats.has("health") ? stats.getDouble("health") : null);
            insertStats.setInt(9, stats.has("level") ? stats.getInt("level") : null);
            insertStats.setBytes(10, stats.has("potion-effects") ? stats.getString("potion-effects").getBytes() : null);
            insertStats.setInt(11, stats.has("saturation") ? stats.getInt("saturation") : null);
            mySQL.execute(insertStats);
        }

        if (data.has("economy")) {
            JSONObject econ = data.getJSONObject("economy");
            PreparedStatement deleteRow = mySQL.prepareStatement(
                    "DELETE FROM `" + prefix + "economy` WHERE `data_uuid` = ?;"
            );
            deleteRow.setString(1, data_uuid);
            mySQL.execute(deleteRow);

            PreparedStatement insertEcon = mySQL.prepareStatement(
                    "INSERT INTO `" + prefix + "economy` VALUES (NULL, ?, ?, ?);"
            );
            insertEcon.setString(1, data_uuid);
            insertEcon.setDouble(2, econ.has("bank-balance") ? econ.getDouble("bank-balance") : null);
            insertEcon.setDouble(3, econ.getDouble("balance"));
            mySQL.execute(insertEcon);
        }
    }

    /**
     * Called on initialization. Sets up all database
     * tables if they do not exist.
     *
     * @throws SQLException If an exception is encountered
     */
    private void setupTables() throws SQLException {
        PreparedStatement playerData = mySQL.prepareStatement(
                "CREATE TABLE IF NOT EXISTS `" + prefix + "player_data` (" +
                        "`id` INT NOT NULL AUTO_INCREMENT," +
                        "`uuid` CHAR(36) NOT NULL," +
                        "`group` VARCHAR(255) NOT NULL," +
                        "`gamemode` ENUM('adventure', 'creative', 'survival') NOT NULL," +
                        "`data_uuid` CHAR(36) NOT NULL," +
                        "PRIMARY KEY (id));"
        );
        mySQL.execute(playerData);

        PreparedStatement playerEnderChest = mySQL.prepareStatement(
                "CREATE TABLE IF NOT EXISTS `" + prefix + "ender_chests` (" +
                        "`id` INT NOT NULL AUTO_INCREMENT," +
                        "`data_uuid` CHAR(36) NOT NULL," +
                        "`items` BLOB NOT NULL," +
                        "PRIMARY KEY (id));"
        );
        mySQL.execute(playerEnderChest);

        PreparedStatement playerArmor = mySQL.prepareStatement(
                "CREATE TABLE IF NOT EXISTS `" + prefix + "armor` (" +
                        "`id` INT NOT NULL AUTO_INCREMENT," +
                        "`data_uuid` CHAR(36) NOT NULL," +
                        "`items` BLOB NOT NULL," +
                        "PRIMARY KEY (id));"
        );
        mySQL.execute(playerArmor);

        PreparedStatement playerInventory = mySQL.prepareStatement(
                "CREATE TABLE IF NOT EXISTS `" + prefix + "inventory` (" +
                        "`id` INT NOT NULL AUTO_INCREMENT," +
                        "`data_uuid` CHAR(36) NOT NULL," +
                        "`items` BLOB NOT NULL," +
                        "PRIMARY KEY (id));"
        );
        mySQL.execute(playerInventory);


        PreparedStatement playerStats = mySQL.prepareStatement(
                "CREATE TABLE IF NOT EXISTS `" + prefix + "player_stats` (" +
                        "`id` INT NOT NULL AUTO_INCREMENT," +
                        "`data_uuid` CHAR(36) NOT NULL," +
                        "`can_fly` BIT," +
                        "`display_name` VARCHAR(16)," +
                        "`exhaustion` FLOAT," +
                        "`exp` FLOAT," +
                        "`flying` BIT," +
                        "`food` INT(20)," +
                        "`health` DOUBLE," +
                        "`level` INT," +
                        "`potion_effects` BLOB," +
                        "`saturation` INT(20)," +
                        "PRIMARY KEY (id));"
        );
        mySQL.execute(playerStats);

        PreparedStatement economy = mySQL.prepareStatement(
                "CREATE TABLE IF NOT EXISTS `" + prefix + "economy` (" +
                        "`id` INT NOT NULL AUTO_INCREMENT," +
                        "`data_uuid` CHAR(36) NOT NULL," +
                        "`bank_balance` DOUBLE," +
                        "`balance` DOUBLE NOT NULL," +
                        "PRIMARY KEY (id));"
        );
        mySQL.execute(economy);
    }
}
