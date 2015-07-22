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

import com.sun.rowset.CachedRowSetImpl;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.defaults.ConfigValues;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.bukkit.Bukkit;

import javax.sql.rowset.CachedRowSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class MySQL {

    private static MySQL INSTANCE = null;

    private static DataSource pool = new DataSource();

    private String hostname;
    private int port;
    private String databaseName;
    private String username;
    private String password;

    private MySQL(String hostname, int port, String dbName, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.databaseName = dbName;
        this.username = username;
        this.password = password;
    }

    /**
     * Get an instance of the MySQL Singleton. If it has not already
     * been initialized, it will be initialized, and variables grabbed
     * from the config.
     *
     * @return The MySQL instance
     */
    public static MySQL getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MySQL(
                    ConfigValues.HOSTNAME.getString(),
                    ConfigValues.PORT.getInt(),
                    ConfigValues.DATABASE_NAME.getString(),
                    ConfigValues.USERNAME.getString(),
                    ConfigValues.PASSWORD.getString()
            );
        }

        return INSTANCE;
    }

    /**
     * Called on onDisable() in the main class. Disconnects the database pool,
     * and sets the static instance variable to null to avoid leaking memory.
     */
    public void disable() {
        if (pool != null) {
            pool.close();
        }

        INSTANCE = null;
    }

    /**
     * Executes a SQL statement.
     *
     * @param statement The statement to execute
     */
    public void execute(final PreparedStatement statement) {
        if (isConnected()) {
            Bukkit.getScheduler().runTaskAsynchronously(PerWorldInventory.getInstance(), new Runnable() {
                @Override
                public void run() {
                    try {
                        statement.execute();

                        statement.getConnection().close();
                    } catch (SQLException ex) {
                        handleDatabaseException(ex);
                    }
                }
            });
        }
    }

    /**
     * Handles a SQLException should one be thrown. It will attempt to restore the connection.
     * If it cannot, it will print the stacktrace. If there was a MySQL crash, it will print
     * a message to the console with information regarding it.
     *
     * @param ex The exception that was thrown
     */
    public void handleDatabaseException(SQLException ex) {
        try {
            if (attemptToRescueConnection(ex)) {
                return;
            }
        } catch (SQLException ignored) {}

        PerWorldInventory.getInstance().getLogger().severe("Database connection error: " + ex.getMessage());
        if (ex.getMessage().contains("marked as crashed")) {
            PerWorldInventory.getInstance().getLogger().severe("If MySQL crashes during a write it may corrupt its indexes!");
            PerWorldInventory.getInstance().getLogger().severe("Try running `CHECK TABLE " + ConfigValues.PREFIX.getString() +
                    "player_data` and then `REPAIR TABLE " + ConfigValues.PREFIX.getString() + "player_data`.");
        }

        ex.printStackTrace();
    }

    /**
     * Attempt to rebuild the pool. This is useful for reloads and failed database
     * connections being restored.
     */
    public void rebuildPool() {
        if (pool != null) {
            pool.close();
        }

        pool = initalizeDbPool();
    }

    /**
     * Get a connection from the pool.
     *
     * @return A Connection
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = pool.getConnection();
        } catch (SQLException ex) {
            PerWorldInventory.getInstance().getLogger().severe("Unable to connect to database: " + ex.getMessage());
            if (!ex.getMessage().contains("Pool empty")) {
                ex.printStackTrace();
            }
        }

        return conn;
    }

    /**
     * Get the database pool.
     *
     * @return The pool
     */
    public static DataSource getPool() {
        return pool;
    }

    /**
     * Open a test connection to see if the database is connected.
     * The connection is closed afterwards.
     *
     * @return True if connected
     */
    public boolean isConnected() {
        Connection testConn = getConnection();
        if (pool != null && testConn != null) {
            try {
                testConn.close();
            } catch (SQLException ex) {
                handleDatabaseException(ex);
            }

            return true;
        }

        return false;
    }

    /**
     * Queries the database, and stores the results in a CachedRowSet.
     *
     * @param statement The SQL statement to execute
     * @return The CachedRowSet with the results
     */
    public CachedRowSet query(final PreparedStatement statement) {
        CachedRowSet rowSet = null;

        if (isConnected()) {
            try {
                ExecutorService exe = Executors.newCachedThreadPool();

                Future<CachedRowSet> future = exe.submit(new Callable<CachedRowSet>() {
                    @Override
                    public CachedRowSet call() throws Exception {
                        try {
                            ResultSet resultSet = statement.executeQuery();

                            CachedRowSet cachedRowSet = new CachedRowSetImpl();
                            cachedRowSet.populate(resultSet);
                            resultSet.close();

                            statement.getConnection().close();

                            if (cachedRowSet.next()) {
                                return cachedRowSet;
                            }
                        } catch (SQLException ex) {
                            handleDatabaseException(ex);
                        }

                        return null;
                    }
                });

                if (future.get() != null) {
                    rowSet = future.get();
                }
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        }

        return rowSet;
    }

    /**
     * Initialize the database pool.
     *
     * @return The database pool
     */
    public DataSource initalizeDbPool() {
        DataSource pool;

        final String url = "jdbc:mysql://" + hostname + ":" + port + "/" + databaseName;
        pool = new DataSource();
        pool.setDriverClassName("com.mysql.jdbc.Driver");
        pool.setUrl(url);
        pool.setUsername(username);
        pool.setPassword(password);
        pool.setMaxActive(20);
        pool.setMaxIdle(10);
        pool.setMaxWait(30000);
        pool.setRemoveAbandoned(true);
        pool.setRemoveAbandonedTimeout(60);
        pool.setTestOnBorrow(true);
        pool.setValidationQuery("/* ping */SELECT 1");
        pool.setValidationInterval(30000);

        return pool;
    }

    /**
     * Prepare a statement with no arguments.
     *
     * @param sql The SQL to prepare
     * @return The resulting PreparedStatement
     */
    public PreparedStatement prepareStatement(String sql) {
        try {
            return getConnection().prepareStatement(sql);
        } catch (SQLException ex) {
            PerWorldInventory.getInstance().getLogger().severe("Unable to prepare statement: " + ex.getMessage());
        }

        return null;
    }

    /**
     * Attempt to reconnect to the database when a SQLException is
     * thrown.
     *
     * @param ex The thrown exception
     * @return True if the Connection was restored
     * @throws SQLException If an exception is thrown during checking
     * connectivity
     */
    protected boolean attemptToRescueConnection(SQLException ex) throws SQLException {
        if (ex.getMessage().contains("connection closed")) {
            rebuildPool();
            if (pool != null) {
                final Connection conn = getConnection();
                if (conn != null && !conn.isClosed()) {
                    return true;
                }
            }
        }

        return false;
    }
}
