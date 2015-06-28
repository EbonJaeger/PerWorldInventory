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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.gnat008.perworldinventory.config.defaults.ConfigValues;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class MySQL {

    private static final MySQL INSTANCE;

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
        Properties props = new Properties();
        props.setProperty("dataSourceClassName", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        props.setProperty("serverName", hostname);
        props.setProperty("port", String.valueOf(port));
        props.setProperty("dataSource.databaseName", dbName);
        props.setProperty("dataSource.user", username);
        props.setProperty("dataSource.password", password);
        props.setProperty("dataSource.cachePrepStmts", "true");
        props.setProperty("dataSource.prepStmtCacheSize", "50");

        HikariConfig config = new HikariConfig(props);
        hikariDS = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return INSTANCE.hikariDS.getConnection();
    }
}
