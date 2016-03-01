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

package me.gnat008.perworldinventory.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sql.rowset.CachedRowSet;

import com.sun.rowset.CachedRowSetImpl;

import me.gnat008.perworldinventory.PerWorldInventory;
import org.bukkit.Bukkit;

public abstract class Database {

    protected PerWorldInventory plugin;
    protected String type;
    protected Connection connection;
    
    protected String database;
    
    protected Database(PerWorldInventory plugin, String database) {
        this.plugin = plugin;
        this.database = database;
        this.connection = null;
    }

    /**
     * Get the type of database being used.
     * 
     * @return SQLite or MySQL
     */
    public String getType() {
        return type;
    }

    /**
     * Get the connection to the database.
     * 
     * @return The {@link java.sql.Connection}
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Open a connection to the database.
     * 
     * @return The opened {@link java.sql.Connection}
     * @throws SQLException If a database error occurs
     * @throws ClassNotFoundException If the database driver is not found
     */
    public abstract Connection openConnection() throws SQLException, ClassNotFoundException;
    
    /**
     * Check if the database is connected.
     * 
     * @return If the database is connected
     * @throws SQLException If there is an error checking if the {@link java.sql.Connection} is closed
     */
    public boolean isConnected() throws SQLException {
        return connection != null && !connection.isClosed();
    }
    
    /**
     * Close the connection to the database.
     * 
     * @return If the connection closed successfully
     * @throws SQLException If there was a problem closing the {@link java.sql.Connection}
     */
    public boolean closeConnection() throws SQLException {
        if (connection == null)
            return false;
        
        connection.close();
        return true;
    }
    
    /**
     * Queries the database, and stores the results in a CachedRowSet.
     *
     * @param query The SQL statement to execute
     * @throws SQLException If a database error occurs
     */
    public void queryDb(final PreparedStatement query, final Callback<ResultSet> callback) throws SQLException {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    final ResultSet result = query.executeQuery();

                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(result);
                        }
                    });
                } catch (SQLException ex) {
                    callback.onFailure(ex);
                }
            }
        });
    }
    
    /**
     * Update the database with information.
     * 
     * @param update The SQL statement to execute
     * @throws SQLException If a database error occurs
     * @throws ClassNotFoundException If the database driver is not found
     */
    public void updateDb(final PreparedStatement update) throws SQLException, ClassNotFoundException {
        if (!isConnected())
            openConnection();
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    update.executeUpdate();
                } catch (SQLException ex) {
                    plugin.getLogger().severe("Unable to update database: " + ex.getMessage());
                }
            }
        });
    }
    
    /**
     * Create a new {@link me.gnat008.perworldinventory.database.Column}.
     * 
     * @param name The name of the column
     * @return The Column object
     */
    public Column createColumn(String name) {
        return new Column(name);
    }
    
    /**
     * Create a new {@link me.gnat008.perworldinventory.database.Query} to
     * build a SQL statement.
     * 
     * @return The Query object
     */
    public Query createQuery() {
        return new Query();
    }
    
    /**
     * Create a {@link java.sql.PreparedStatement} from a String.
     * 
     * @param sql The String to prepare
     * @return The SQL statement
     * @throws SQLException If a database error occurs
     * @throws ClassNotFoundException If the database driver is not found
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException, ClassNotFoundException {
        if (!isConnected())
            openConnection();
        
        return connection.prepareStatement(sql);
    }

    public interface Callback<T> {
        void onSuccess(T done);
        void onFailure(Throwable cause);
    }
}
