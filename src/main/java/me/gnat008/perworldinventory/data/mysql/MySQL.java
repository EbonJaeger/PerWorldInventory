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
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.defaults.ConfigValues;
import org.bukkit.Bukkit;

import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.*;

class MySQL {

    public static final MySQL INSTANCE;

    private final HikariDataSource hikariDS;

    static {
        INSTANCE = new MySQL(
                ConfigValues.HOSTNAME.getString(),
                ConfigValues.PORT.getInt(),
                ConfigValues.DATABASE_NAME.getString(),
                ConfigValues.USERNAME.getString(),
                ConfigValues.PASSWORD.getString()
        );
    }

    private MySQL(String hostname, int port, String dbName, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + dbName);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);

        hikariDS = new HikariDataSource(config);
    }

    public void connect() {
        isConnected();
    }

    public void disconnect() {
        hikariDS.shutdown();
    }

    public CachedRowSet query(final PreparedStatement stmt) {
        CachedRowSet rowSet = null;

        if (isConnected()) {
            try {
                ExecutorService exe = Executors.newCachedThreadPool();

                Future<CachedRowSet> future = exe.submit(new Callable<CachedRowSet>() {
                    @Override
                    public CachedRowSet call() throws Exception {
                        try {
                            ResultSet resultSet = stmt.executeQuery();

                            CachedRowSet cachedRowSet = new CachedRowSetImpl();
                            cachedRowSet.populate(resultSet);
                            resultSet.close();

                            stmt.getConnection().close();

                            if (cachedRowSet.next()) {
                                return cachedRowSet;
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
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

    public void execute(final PreparedStatement stmt) {
        if (isConnected()) {
            Bukkit.getScheduler().runTaskAsynchronously(PerWorldInventory.getInstance(), new Runnable() {
                @Override
                public void run() {
                    try {
                        stmt.execute();

                        stmt.getConnection().close();
                    } catch (SQLException ex) {
                        PerWorldInventory.getInstance().getLogger().severe("Error executing statement: " + ex.getMessage());
                    }
                }
            });
        }
    }

    public Connection getConnection() {
        try {
            return hikariDS.getConnection();
        } catch (SQLException ex) {
            PerWorldInventory.getInstance().getLogger().severe("Unable to connect to database: " + ex.getMessage());
        }

        return null;
    }

    public boolean isConnected() {
        try {
            hikariDS.getConnection();
        } catch (SQLException ex) {
            return false;
        }

        return true;
    }
}
