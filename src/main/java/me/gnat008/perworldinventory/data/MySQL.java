package me.gnat008.perworldinventory.data;

import com.google.common.annotations.VisibleForTesting;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import me.gnat008.perworldinventory.BukkitService;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.config.DatabaseProperties;
import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.data.serializers.InventorySerializer;
import me.gnat008.perworldinventory.data.serializers.PotionEffectSerializer;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.sql.*;
import java.util.UUID;

/**
 * Class to handle MySQL storage.
 */
public class MySQL implements DataSource {

    private String hostname, port, username, password, databaseName, prefix;
    private int poolSize;
    private boolean separateGamemodeInventories;
    private HikariDataSource dataSource;
    private PerWorldInventory plugin;
    private BukkitService bukkitService;
    private InventorySerializer inventorySerializer;

    /**
     * Constructor
     *
     * @param plugin {@link PerWorldInventory} plugin instance
     * @param settings Plugin settings
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    @Inject
    public MySQL(PerWorldInventory plugin, BukkitService bukkitService, InventorySerializer inventorySerializer,
                 Settings settings) throws ClassNotFoundException, SQLException {
        this.plugin = plugin;
        this.bukkitService = bukkitService;
        this.inventorySerializer = inventorySerializer;
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
    public void saveLogoutData(PWIPlayer player, boolean async) {
        String uuid = player.getUuid().toString().replace("-", "");
        Location location = player.getLocation();

        bukkitService.runTaskAsync(() -> {
            String sql = "SELECT id FROM " + prefix + "logout_location WHERE uuid=?;";
            try (Connection conn = getConnection(); PreparedStatement query = conn.prepareStatement(sql)) {
                query.setString(1, uuid);
                ResultSet existing = query.executeQuery();

                if (existing.next()) { // Data exists
                    sql = "UPDATE " + prefix + "logout_location " +
                            "SET world=?,x=?,y=?,z=?,pitch=?,yaw=? " +
                            "WHERE uuid=?;";
                    try (PreparedStatement update = conn.prepareStatement(sql)) {
                        update.setString(1, location.getWorld().getName());
                        update.setDouble(2, location.getX());
                        update.setDouble(3, location.getY());
                        update.setDouble(4, location.getZ());
                        update.setFloat(5, location.getPitch());
                        update.setFloat(6, location.getYaw());
                        update.setString(7, uuid);

                        update.executeUpdate();
                    }
                } else {
                    sql = "INSERT INTO " + prefix + "logout_location (uuid,world,x,y,z,pitch,yaw) " +
                            "VALUES (?,?,?,?,?,?,?);";
                    try (PreparedStatement insert = conn.prepareStatement(sql)) {
                        insert.setString(1, uuid);
                        insert.setString(2, location.getWorld().getName());
                        insert.setDouble(3, location.getX());
                        insert.setDouble(4, location.getY());
                        insert.setDouble(5, location.getZ());
                        insert.setFloat(6, location.getPitch());
                        insert.setFloat(7, location.getYaw());

                        insert.executeUpdate();
                    } catch (SQLException ex) {
                        PwiLogger.severe("Error while inserting logout location for '" + player.getName() + "' in database:", ex);
                    }
                }
            } catch (SQLException ex) {
                PwiLogger.severe("Error checking for logout location data:", ex);
            }
        });
    }

    @Override
    public void saveToDatabase(Group group, GameMode gamemode, PWIPlayer player, boolean async) {
        bukkitService.runTaskOptionallyAsync(() -> saveToDatabase(group, gamemode, player), async);
    }

    @Override
    public void saveToDatabase(Group group, GameMode gamemode, PWIPlayer player) {
        String uuid = player.getUuid().toString();
        if (gamemode == GameMode.SPECTATOR) {
            gamemode = GameMode.CREATIVE;
        }

        String pid = isDataStored(uuid, group.getName(), gamemode.name());
        if (pid == null || pid.isEmpty()) { // Data already exists, update
            try {
                updateExisting(pid, player);
            } catch (SQLException ex) {
                PwiLogger.severe("Error updating data in database for player '" + player.getName() + "' for " +
                        "group '" + group.getName() + "' and GameMode '" + gamemode.name() + "':", ex);
            }
        } else { // Insert data for the first time
            try {
                insertIntoDatabase(group, gamemode, player, uuid);
            } catch (SQLException ex) {
                PwiLogger.severe("Error inserting data to database for player '" + player.getName() + "' for " +
                        "group '" + group.getName() + "' and GameMode '" + gamemode.name() + "':", ex);
            }
        }
    }

    /**
     * Create and execute PreparedStatements to insert data into the MySQL
     * database for the first time.
     *
     * @param group The Group the data is for.
     * @param gameMode The GameMode the data is for.
     * @param player The Player the data is from.
     * @param uuid The UUID of the player as a String with all '-' stripped.
     * @throws SQLException If an error occurs while updating the database.
     */
    private void insertIntoDatabase(Group group, GameMode gameMode, PWIPlayer player, String uuid) throws SQLException {
        String dataUUID = UUID.randomUUID().toString();

        String sql = "INSERT INTO " + prefix + "players VALUES (null,?,?,?,?);";
        try (Connection conn = getConnection()) {
            PreparedStatement playersStatement = conn.prepareStatement(sql);
            playersStatement.setString(1, uuid);
            playersStatement.setString(2, group.getName());
            playersStatement.setString(3, gameMode.name().toLowerCase());
            playersStatement.setString(3, dataUUID);
            playersStatement.executeUpdate();

            sql = "INSERT INTO " + prefix + "armor VALUES (?,?);";
            PreparedStatement armorStatement = conn.prepareStatement(sql);
            String armor = inventorySerializer.serializeInventoryForSQL(player.getArmor());
            Blob armorBlob = conn.createBlob();
            armorBlob.setBytes(1, armor.getBytes());
            armorStatement.setString(1, dataUUID);
            armorStatement.setBlob(2, armorBlob);
            armorStatement.executeUpdate();

            sql = "INSERT INTO " + prefix + "inventory VALUES (?,?);";
            PreparedStatement invStatement = conn.prepareStatement(sql);
            String inv = inventorySerializer.serializeInventoryForSQL(player.getInventory());
            Blob invBlob = conn.createBlob();
            invBlob.setBytes(1, inv.getBytes());
            invStatement.setString(1, dataUUID);
            invStatement.setBlob(2, invBlob);
            invStatement.executeUpdate();

            sql = "INSERT INTO " + prefix + "enderchest VALUES (?,?);";
            PreparedStatement ecStatement = conn.prepareStatement(sql);
            String ec = inventorySerializer.serializeInventoryForSQL(player.getEnderChest());
            Blob ecBlob = conn.createBlob();
            ecBlob.setBytes(1, ec.getBytes());
            ecStatement.setString(1, dataUUID);
            ecStatement.setBlob(2, ecBlob);
            ecStatement.executeUpdate();

            sql = "INSERT INTO " + prefix + "stats VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
            PreparedStatement statsStatement = conn.prepareStatement(sql);
            statsStatement.setString(1, dataUUID);
            statsStatement.setBoolean(2, player.getCanFly());
            statsStatement.setString(3, player.getDisplayName());
            statsStatement.setFloat(4, player.getExhaustion());
            statsStatement.setFloat(5, player.getExperience());
            statsStatement.setBoolean(6, player.isFlying());
            statsStatement.setInt(7, player.getFoodLevel());
            statsStatement.setDouble(8, player.getHealth());
            statsStatement.setInt(9, player.getLevel());
            statsStatement.setFloat(10, player.getSaturationLevel());
            statsStatement.setFloat(11, player.getFallDistance());
            statsStatement.setInt(12, player.getFireTicks());
            statsStatement.setInt(13, player.getMaxAir());
            statsStatement.setInt(14, player.getRemainingAir());
            statsStatement.executeUpdate();

            sql = "INSERT INTO " + prefix + "potion_effects VALUES (?,?);";
            PreparedStatement peStatement = conn.prepareStatement(sql);
            String pe = PotionEffectSerializer.serializeForSQL(player.getPotionEffects());
            Blob peBlob = conn.createBlob();
            peBlob.setBytes(1, pe.getBytes());
            peStatement.setString(1, dataUUID);
            peStatement.setBlob(2, peBlob);
            peStatement.executeUpdate();

            if (plugin.isEconEnabled()) {
                sql = "INSERT INTO " + prefix + "economy VALUES (?,?,?);";
                PreparedStatement econStatement = conn.prepareStatement(sql);
                econStatement.setString(1, dataUUID);
                econStatement.setDouble(2, player.getBankBalance());
                econStatement.setDouble(3, player.getBalance());
                econStatement.executeUpdate();
            }
        }
    }

    /**
     * Updates existing player data in the MySQL database.
     *
     * @param pid The pid index to use to find and update the records.
     * @param player The player to update the data for.
     * @throws SQLException If an error occurs while updating the database.
     */
    private void updateExisting(String pid, PWIPlayer player) throws SQLException {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE " + prefix + "armor " +
                    "SET items=? WHERE pid=?;";
            PreparedStatement uArmor = conn.prepareStatement(sql);
            String armor = inventorySerializer.serializeInventoryForSQL(player.getArmor());
            Blob armorBlob = conn.createBlob();
            armorBlob.setBytes(1, armor.getBytes());
            uArmor.setBlob(1, armorBlob);
            uArmor.setString(2, pid);
            uArmor.executeUpdate();

            sql = "UPDATE " + prefix + "inventory " +
                    "SET items=? WHERE pid=?;";
            PreparedStatement uInv = conn.prepareStatement(sql);
            String inv = inventorySerializer.serializeInventoryForSQL(player.getArmor());
            Blob invBlob = conn.createBlob();
            armorBlob.setBytes(1, inv.getBytes());
            uInv.setBlob(1, invBlob);
            uInv.setString(2, pid);
            uInv.executeUpdate();

            sql = "UPDATE " + prefix + "enderchest " +
                    "SET items=? WHERE pid=?;";
            PreparedStatement uEnderChest = conn.prepareStatement(sql);
            String enderChest = inventorySerializer.serializeInventoryForSQL(player.getArmor());
            Blob ecBlob = conn.createBlob();
            armorBlob.setBytes(1, enderChest.getBytes());
            uEnderChest.setBlob(1, ecBlob);
            uEnderChest.setString(2, pid);
            uEnderChest.executeUpdate();

            sql = "UPDATE " + prefix + "stats " +
                    "SET can_fly=?,display_name=?,exhaustion=?,exp=?,flying=?,food=?," +
                    "health=?,level=?,saturation=?,fall_distance=?,fire_ticks=?,max_air=?,remaining_air=? " +
                    "WHERE pid=?;";
            PreparedStatement uStats = conn.prepareStatement(sql);
            uStats.setBoolean(1, player.getCanFly());
            uStats.setString(2, player.getDisplayName());
            uStats.setFloat(3, player.getExhaustion());
            uStats.setFloat(4, player.getExperience());
            uStats.setBoolean(5, player.isFlying());
            uStats.setInt(6, player.getFoodLevel());
            uStats.setDouble(7, player.getHealth());
            uStats.setInt(8, player.getLevel());
            uStats.setFloat(9, player.getSaturationLevel());
            uStats.setFloat(10, player.getFallDistance());
            uStats.setInt(11, player.getFireTicks());
            uStats.setInt(12, player.getMaxAir());
            uStats.setInt(13, player.getRemainingAir());
            uStats.setString(14, pid);
            uStats.executeUpdate();

            sql = "UPDATE " + prefix + "potion_effects " +
                    "SET potion_effects=? WHERE pid=?;";
            PreparedStatement uPotions = conn.prepareStatement(sql);
            String pe = PotionEffectSerializer.serializeForSQL(player.getPotionEffects());
            Blob peBlob = conn.createBlob();
            peBlob.setBytes(1, pe.getBytes());
            uPotions.setBlob(1, peBlob);
            uPotions.setString(2, pid);
            uPotions.executeUpdate();

            if (plugin.isEconEnabled()) {
                sql = "UPDATE " + prefix + "economy " +
                        "SET bank_balance=?,balance=? WHERE pid=?;";
                PreparedStatement uEcon = conn.prepareStatement(sql);
                uEcon.setDouble(1, player.getBankBalance());
                uEcon.setDouble(2, player.getBalance());
                uEcon.setString(3, pid);
                uEcon.executeUpdate();
            }
        }
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
        PwiLogger.info("Creating SQL database tables if they don't exist...");

        try (Connection conn = getConnection(); Statement statement = conn.createStatement()) {
            // Players, groups, and gamemodes
            String sql = "CREATE TABLE IF NOT EXISTS " + prefix + "players (" +
                    "id INT UNSIGNED NOT NULL AUTO_INCREMENT," +
                    "uuid CHAR(36) NOT NULL," +
                    "group VARCHAR(255) NOT NULL," +
                    "gamemode ENUM('adventure', 'creative', 'survival') NOT NULL," +
                    "pid CHAR(36) NOT NULL," +
                    "PRIMARY KEY (id));";
            statement.executeUpdate(sql);

            // Armor
            sql = "CREATE TABLE IF NOT EXISTS " + prefix + "armor (" +
                    "pid CHAR(36) NOT NULL," +
                    "items BLOB NOT NULL," +
                    "PRIMARY KEY (pid));";
            statement.executeUpdate(sql);

            // Inventory
            sql = "CREATE TABLE IF NOT EXISTS " + prefix + "inventory (" +
                    "pid CHAR(36) NOT NULL," +
                    "items BLOB NOT NULL," +
                    "PRIMARY KEY (pid));";
            statement.executeUpdate(sql);

            // Enderchest
            sql = "CREATE TABLE IF NOT EXISTS " + prefix + "enderchest (" +
                    "pid CHAR(36) NOT NULL," +
                    "items BLOB NOT NULL," +
                    "PRIMARY KEY (pid));";
            statement.executeUpdate(sql);

            // Economy
            sql = "CREATE TABLE IF NOT EXISTS " + prefix + "economy (" +
                    "pid CHAR(36) NOT NULL," +
                    "bank_balance DOUBLE," +
                    "balance DOUBLE," +
                    "PRIMARY KEY (pid));";
            statement.executeUpdate(sql);

            // Stats
            sql = "CREATE TABLE IF NOT EXISTS " + prefix + "stats (" +
                    "pid CHAR(36) NOT NULL," +
                    "can_fly BOOL NOT NULL," +
                    "display_name VARCHAR(16) NOT NULL," +
                    "exhaustion FLOAT NOT NULL," +
                    "exp FLOAT NOT NULL," +
                    "flying BOOL NOT NULL," +
                    "food INT(20) NOT NULL," +
                    "health DOUBLE NOT NULL," +
                    "level INT(11) NOT NULL," +
                    "saturation FLOAT NOT NULL," +
                    "fall_distance FLOAT NOT NULL," +
                    "fire_ticks INT NOT NULL," +
                    "max_air INT NOT NULL," +
                    "remaining_air INT NOT NULL," +
                    "PRIMARY KEY (pid));";
            statement.executeUpdate(sql);

            // Potion effects
            sql = "CREATE TABLE IF NOT EXISTS " + prefix + "potion_effects (" +
                    "pid CHAR(36) NOT NULL," +
                    "potion_effects BLOB NOT NULL," +
                    "PRIMARY KEY (pid));";
            statement.executeUpdate(sql);

            // Logout location
            sql = "CREATE TABLE IF NOT EXISTS " + prefix + "logout_location (" +
                    "id INT UNSIGNED NOT NULL AUTO_INCREMENT," +
                    "uuid CHAR(36) NOT NULL," +
                    "world VARCHAR(255) NOT NULL," +
                    "x DOUBLE NOT NULL," +
                    "y DOUBLE NOT NULL," +
                    "z DOUBLE NOT NULL," +
                    "pitch FLOAT NOT NULL," +
                    "yaw FLOAT NOT NULL," +
                    "PRIMARY KEY (id),";
            statement.executeUpdate(sql);
        }
    }

    /**
     * Check if data with the given parameters are already stored in the database.
     * This method will return the pid to use in the lookup of more data if data exists
     * with the given parameters. If none exists, the method will return null.
     *
     * @param uuid The UUID of the player as a String with all '-' characters removed.
     * @param group The name of the group to check for.
     * @param gameMode The GameMode of the player as a lowercase String.
     * @return The pid key if data exists, null if not.
     */
    private String isDataStored(String uuid, String group, String gameMode) {
        String sql = "SELECT pid FROM " + prefix + "players WHERE uuid=? AND group=? AND gamemode=?;";
        ResultSet rs = null;
        try (Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, uuid);
            statement.setString(2, group);
            statement.setString(3, gameMode);

            rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getString("pid");
            }
        } catch (SQLException ex) {
            PwiLogger.severe("Error checking for existing data:", ex);
        } finally {
            close(rs);
        }

        return null;
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

        this.separateGamemodeInventories = settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES);
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
