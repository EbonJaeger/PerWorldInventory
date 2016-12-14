package me.gnat008.perworldinventory.data;

import com.google.common.annotations.VisibleForTesting;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.utils.Utils;
import me.gnat008.perworldinventory.config.DatabaseProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.sql.*;

/**
 * Class to handle MySQL storage.
 */
public class MySQL implements DataSource {

    private String hostname, port, username, password, databaseName, prefix;
    private int poolSize;
    private HikariDataSource dataSource;

    /**
     * Constructor
     *
     * @param settings Plugin settings
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    @Inject
    public MySQL(Settings settings) throws ClassNotFoundException, SQLException {
        loadSettings(settings);

        // Set up connection
        try {
            this.setConnectionArguments();
        } catch (RuntimeException ex) {
            if (ex instanceof IllegalArgumentException) {
                PwiLogger.warning("Invalid database arguments! Please check your configuration!");
                PwiLogger.warning("If this error persists, please report it to the developer!");
            }

            if (ex instanceof HikariPool.PoolInitializationException) {
                PwiLogger.warning("Can't initialize database connection! Please check your configuration!");
                PwiLogger.warning("If this error persists, please report it to the developer!");
            }

            PwiLogger.warning("Can't use the Hikari Connection Pool! Please report this error to the developer!");
            throw ex;
        }

        // Initialize database tables
        try {
            checkTablesAndColumns();
        } catch (SQLException ex) {
            close();
            PwiLogger.severe("Can't initialize the MySQL database:", ex);
            PwiLogger.warning("Please check your database settings in the config.yml file!");

            throw ex;
        }
    }

    /**
     * Constructor for testing.
     *
     * @param settings Plugin settings
     * @param dataSource Data source
     */
    @VisibleForTesting
    MySQL(Settings settings, HikariDataSource dataSource) {
        this.dataSource = dataSource;
        loadSettings(settings);
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.MYSQL;
    }

    @Override
    public void reload() {
        if (dataSource != null) {
            dataSource.close();
        }

        setConnectionArguments();
        PwiLogger.info("Hikari database settings reloaded!");
    }

    @Override
    public void saveLogoutData(PWIPlayer player) {

    }

    @Override
    public void saveToDatabase(Group group, GameMode gamemode, PWIPlayer player, boolean async) {

    }

    @Override
    public void saveToDatabase(Group group, GameMode gamemode, PWIPlayer player) {

    }

    @Override
    public void getFromDatabase(Group group, GameMode gamemode, Player player) {

    }

    @Override
    public Location getLogoutData(Player player) {
        return null;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void checkTablesAndColumns() throws SQLException {
        try (Connection conn = getConnection(); Statement statement = conn.createStatement()) {
            // Players, groups, and gamemodes
            String sql = "CREATE TABLE IF NOT EXISTS " + prefix + "players (" +
                    "pid INT NOT NULL AUTO_INCREMENT," +
                    "uuid CHAR(36) NOT NULL," +
                    "group VARCHAR(255) NOT NULL," +
                    "gamemode ENUM('adventure', 'creative', 'survival') NOT NULL," +
                    "PRIMARY KEY (pid));";
            statement.executeUpdate(sql);

            // Armor
            sql = "CREATE TABLE IF NOT EXISTS " + prefix + "armo` (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "pid INT NOT NULL," +
                    "items BLOB NOT NULL," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (pid) REFERENCES " + prefix + "players(pid));";
            statement.executeUpdate(sql);

            // Inventory
            sql = "CREATE TABLE IF NOT EXISTS " + prefix + "inventory (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "pid INT NOT NULL," +
                    "items BLOB NOT NULL," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (pid) REFERENCES " + prefix + "players(pid));";
            statement.executeUpdate(sql);

            // Enderchest
            sql = "CREATE TABLE IF NOT EXISTS " + prefix + "enderchest (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "pid INT NOT NULL," +
                    "items BLOB NOT NULL," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (pid) REFERENCES " + prefix + "players(pid));";
            statement.executeUpdate(sql);

            // Economy
            sql = "CREATE TABLE IF NOT EXISTS " + prefix + "economy (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "pid INT NOT NULL," +
                    "bank_balance DOUBLE," +
                    "balance DOUBLE," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (pid) REFERENCES " + prefix + "players(pid));";
            statement.executeUpdate(sql);

            // Stats
            sql = "CREATE TABLE IF NOT EXISTS " + prefix + "stats (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "pid INT NOT NULL," +
                    "can_fly BOOL NOT NULL," +
                    "display_name VARCHAR(16) NOT NULL," +
                    "exhaustion FLOAT NOT NULL," +
                    "exp FLOAT NOT NULL," +
                    "flying BOOL NOT NULL," +
                    "food INT(20) NOT NULL," +
                    "health DOUBLE NOT NULL," +
                    "level INT(11) NOT NULL," +
                    "potion_effects BLOB NOT NULL," +
                    "saturation INT(20) NOT NULL," +
                    "fall_distance FLOAT NOT NULL," +
                    "fire_ticks INT NOT NULL," +
                    "max_air INT NOT NULL," +
                    "remaining_air INT NOT NULL," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (pid) REFERENCES " + prefix + "players(pid));";
            statement.executeUpdate(sql);

            // Logout location
            sql = "CREATE TABLE IF NOT EXISTS " + prefix + "logout_location (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "pid INT NOT NULL," +
                    "world VARCHAR(255) NOT NULL," +
                    "x DOUBLE NOT NULL," +
                    "y DOUBLE NOT NULL," +
                    "z DOUBLE NOT NULL," +
                    "pitch FLOAT NOT NULL," +
                    "yaw FLOAT NOT NULL," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (pid) REFERENCES " + prefix + "players(pid));";
            statement.executeUpdate(sql);

            PwiLogger.info("MySQL tables created!");
        }
    }

    private void setConnectionArguments() {
        dataSource = new HikariDataSource();
        dataSource.setPoolName("PerWorldInventorySQLPool");

        dataSource.setMaximumPoolSize(poolSize);
        dataSource.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + databaseName);

        // Authentication
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // Encoding
        dataSource.addDataSourceProperty("characterEncoding", "utf8");
        dataSource.addDataSourceProperty("encoding","UTF-8");
        dataSource.addDataSourceProperty("useUnicode", "true");

        // Random stuff
        dataSource.addDataSourceProperty("rewriteBatchedStatements", "true");
        dataSource.addDataSourceProperty("jdbcCompliantTruncation", "false");

        // Caching
        dataSource.addDataSourceProperty("cachePrepStmts", "true");
        dataSource.addDataSourceProperty("prepStmtCacheSize", "275");
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        PwiLogger.info("MySQL database parameters set!");
    }

    private void loadSettings(Settings settings) {
        this.hostname = settings.getProperty(DatabaseProperties.HOSTNAME);
        this.port = settings.getProperty(DatabaseProperties.PORT);
        this.username = settings.getProperty(DatabaseProperties.USERNAME);
        this.password = settings.getProperty(DatabaseProperties.PASSWORD);
        this.databaseName = settings.getProperty(DatabaseProperties.DATABASE_NAME);
        this.prefix = settings.getProperty(DatabaseProperties.PREFIX);
        this.poolSize = settings.getProperty(DatabaseProperties.DATABASE_POOL_SIZE);

        if (poolSize == -1) {
            this.poolSize = Utils.getCoreCount();
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private static void close(ResultSet rs) {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
        } catch (SQLException ex) {
            PwiLogger.severe("Could not close ResultSet", ex);
        }
    }

    private static void close(PreparedStatement statement) {
        try {
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
        } catch (SQLException ex) {
            PwiLogger.severe("Could not close PreparedStatement", ex);
        }
    }
}
