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

import java.sql.*;

public class MySQLManager {

    private static MySQLManager instance = null;

    private final String prefix;

    private Connection conn = null;
    private PerWorldInventory plugin;

    private MySQLManager() {
        this.plugin = PerWorldInventory.getInstance();
        prefix = ConfigValues.PREFIX.getString();
    }

    public static MySQLManager getInstance() {
        if (instance == null) {
            instance = new MySQLManager();
        }

        return instance;
    }

    public void disable() {
        try {
            MySQL.INSTANCE.disconnect();

            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            plugin.getLogger().severe("Could not close connection: " + ex.getMessage());
        }

        instance = null;
    }

    public void startConnection() throws SQLException {
        MySQL.INSTANCE.connect();
        if (MySQL.INSTANCE.isConnected()) {
            plugin.getLogger().info("Connected to MySQL database!");
        }

        setupTables();
    }

    public void updateDatabase(String uuid, String group, String gamemode, byte[] data) throws SQLException {
        if (!MySQL.INSTANCE.isConnected()) {
            MySQL.INSTANCE.connect();
        }

        // Check if data for the same player, group, and gamemode already exist
        PreparedStatement query = MySQL.INSTANCE.getConnection().prepareStatement(
                "SELECT id FROM `" + prefix + "data` WHERE `uuid` = ? AND `group` = ? AND `gamemode` = ?;"
        );
        query.setString(1, uuid);
        query.setString(2, group);
        query.setString(3, gamemode);

        ResultSet check = MySQL.INSTANCE.query(query);
        if (check != null && check.next()) {
            // Data exists, UPDATE instead of INSERT
            int id = check.getInt("id");
            PreparedStatement update = MySQL.INSTANCE.getConnection().prepareStatement(
                    "UPDATE `" + prefix + "data` SET `data` = ? WHERE `id` = ?"
            );
            update.setBytes(1, data);
            update.setInt(2, id);

            MySQL.INSTANCE.execute(update);
        } else {
            // Pre-existing data was not found, INSERT into the table
            PreparedStatement insert = MySQL.INSTANCE.getConnection().prepareStatement(
                    "INSERT INTO `" + prefix + "data` VALUES (NULL, ?, ?, ?, ?);"
            );
            insert.setString(1, uuid);
            insert.setString(2, group);
            insert.setString(3, gamemode);
            insert.setBytes(4, data);

            MySQL.INSTANCE.execute(insert);
        }
    }

    private void setupTables() throws SQLException {
        PreparedStatement stmt = MySQL.INSTANCE.getConnection().prepareStatement(
                "CREATE TABLE IF NOT EXISTS `" + prefix + "data` (" +
                        "`id` INT NOT NULL AUTO_INCREMENT," +
                        "`uuid` CHAR(36) NOT NULL," +
                        "`group` VARCHAR(255) NOT NULL," +
                        "`gamemode` ENUM('adventure', 'creative', 'survival') NOT NULL," +
                        "`data` BLOB NOT NULL," +
                        "PRIMARY KEY (id));");

        MySQL.INSTANCE.execute(stmt);
    }
}
