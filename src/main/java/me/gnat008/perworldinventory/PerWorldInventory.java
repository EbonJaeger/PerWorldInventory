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

package me.gnat008.perworldinventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import me.gnat008.perworldinventory.commands.PerWorldInventoryCommand;
import me.gnat008.perworldinventory.config.ConfigManager;
import me.gnat008.perworldinventory.config.ConfigType;
import me.gnat008.perworldinventory.config.defaults.ConfigValues;
import me.gnat008.perworldinventory.data.DataConverter;
import me.gnat008.perworldinventory.data.DataSerializer;
import me.gnat008.perworldinventory.database.Column;
import me.gnat008.perworldinventory.database.Database;
import me.gnat008.perworldinventory.database.mysql.MySQL;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.listeners.PlayerChangedWorldListener;
import me.gnat008.perworldinventory.listeners.PlayerGameModeChangeListener;
import me.gnat008.perworldinventory.listeners.PlayerQuitListener;
import me.gnat008.perworldinventory.util.Printer;
import net.milkbowl.vault.economy.Economy;

public class PerWorldInventory extends JavaPlugin {

    private Economy economy;
    private Database database;
    private Connection connection;
    private DataSerializer serializer;

    private static PerWorldInventory instance = null;

    @Override
    public void onEnable() {
        instance = this;

        if (!(new File(getDataFolder() + File.separator + "data" + File.separator + "defaults").exists())) {
            new File(getDataFolder() + File.separator + "data" + File.separator + "defaults").mkdirs();
        }

        if (!(new File(getDataFolder() + File.separator + "__default.json").exists())) {
            saveResource("default.json", false);
            File dFile = new File(getDataFolder() + File.separator + "default.json");
            dFile.renameTo(new File(getDefaultFilesDirectory() + File.separator + "__default.json"));
        }

        getConfigManager().addConfig(ConfigType.CONFIG, new File(getDataFolder() + File.separator + "config.yml"));
        getConfigManager().addConfig(ConfigType.WORLDS, new File(getDataFolder() + File.separator + "worlds.yml"));

        getGroupManager().loadGroupsToMemory();

        this.serializer = new DataSerializer(this);

        if (ConfigValues.ENABLE_METRICS.getBoolean()) {
            getLogger().info("Starting metrics...");
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            } catch (IOException e) {
                getLogger().info("Failed to start metrics!");
            }
        }
        
        getLogger().info("Registering commands...");
        getCommand("pwi").setExecutor(new PerWorldInventoryCommand(this));
        getLogger().info("Commands registered! Registering listeners...");
        getServer().getPluginManager().registerEvents(new PlayerChangedWorldListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);

        if (ConfigValues.SEPARATE_GAMEMODE_INVENTORIES.getBoolean()) {
            getServer().getPluginManager().registerEvents(new PlayerGameModeChangeListener(this), this);
            getLogger().info("Registered PlayerGameModeChangeListener.");
        }
        getLogger().info("Listeners enabled!");

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            getLogger().info("Vault found! Hooking into it...");
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
                getLogger().info("Hooked into Vault!");
            } else {
                getLogger().warning("Unable to hook into Vault!");
            }
        }

        if (ConfigValues.USE_SQL.getBoolean()) {
            getLogger().info("Configured to use SQL! Attempting to connect to database...");
            if (setupDatabase()) {
                getLogger().info("Successfully connected to the database!");
            } else {
                getLogger().warning("Setting 'use-sql' to 'false' in the config, and switching to flatfiles!");
                //TODO: Set config setting to false
            }
        }
    }

    @Override
    public void onDisable() {
        Printer.disable();
        DataConverter.disable();
        getConfigManager().disable();
        getGroupManager().disable();
        getServer().getScheduler().cancelTasks(this);
        instance = null;
    }

    public static PerWorldInventory getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Cannot get instance before onEnable() has been called!");
        }

        return instance;
    }

    public ConfigManager getConfigManager() {
        return ConfigManager.getInstance();
    }
    
    public Connection getConnection() {
        return connection;
    }

    public DataConverter getDataConverter() {
        return DataConverter.getInstance(this);
    }
    
    public Database getSQLDatabase() {
        return database;
    }

    public DataSerializer getSerializer() {
        return serializer;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public File getDefaultFilesDirectory() {
        return new File(getDataFolder() + File.separator + "data" + File.separator + "defaults");
    }

    public GroupManager getGroupManager() {
        return GroupManager.getInstance(this);
    }

    public Printer getPrinter() {
        return Printer.getInstance(this);
    }

    public void copyFile(File from, File to) {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(to);

            byte[] buff = new byte[1024];
            int len;
            while ((len = in.read(buff)) > 0) {
                out.write(buff, 0, len);
            }
        } catch (IOException ex) {
            getPrinter().printToConsole("An error occurred copying file '" + from.getName() + "' to '" + to.getName() + "': " + ex.getMessage(), true);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
    
    private boolean setupDatabase() {
        if (ConfigValues.SQL_DRIVER.getString().equalsIgnoreCase("mysql")) {
            getLogger().info("Attempting to connect to MySQL database...");
            database = new MySQL(this, 
                    ConfigValues.DATABASE_NAME.getString(),
                    ConfigValues.HOSTNAME.getString() + ":" + ConfigValues.PORT.getInt(),
                    ConfigValues.USERNAME.getString(),
                    ConfigValues.PASSWORD.getString());
            if (!openDatabaseConnection())
                return false;
        }/* else if () {
            //TODO: Implement SQLlite
        }*/ else {
            getLogger().warning("Valid database driver not detected!");
            getLogger().warning("Valid database drivers are: mysql or sqllite");
            return false;
        }
        
        try {
            setupTables();
        } catch (SQLException | ClassNotFoundException ex) {
            getLogger().severe("Unable to create the database tables: " + ex.getMessage());
            return false;
        }
        
        return true;
    }
    
    private boolean openDatabaseConnection() {
        if (database == null) {
            getLogger().severe("No database loaded!");
            return false;
        }
        
        try {
            connection = database.openConnection();
        } catch (SQLException sqlEx) {
            getLogger().severe("Unable to create a connection to the " + database.getType() + " database!");
            getLogger().severe("Please make sure your configuration settings are correct!");
            return false;
        } catch (ClassNotFoundException cnfEx) {
            getLogger().severe("No database driver found for " + database.getType() + "!");
            getLogger().severe(cnfEx.getMessage());
            return false;
        }
        
        return true;
    }
    
    private void setupTables() throws SQLException, ClassNotFoundException {
        String prefix = ConfigValues.PREFIX.getString();
        
        Column[] pd = new Column[5];
        pd[0] = database.createColumn("id").type("INT").notNull().primaryKey().autoIncrement();
        pd[1] = database.createColumn("uuid").type("CHAR", 36).notNull();
        pd[2] = database.createColumn("data_group").type("VARCHAR", 255).notNull();
        pd[3] = database.createColumn("gamemode").type("ENUM('survival', 'creative', 'adventure')").notNull();
        pd[4] = database.createColumn("data_uuid").type("CHAR", 36).notNull();
        String playerDataQuery = database.createQuery().createTable(prefix + "player_data", true, pd).buildQuery();
        PreparedStatement playerData = database.prepareStatement(playerDataQuery);
        database.updateDb(playerData);
        
        Column[] invs = new Column[3];
        invs[0] = database.createColumn("id").type("INT").notNull().primaryKey().autoIncrement();
        invs[1] = database.createColumn("data_uuid").type("CHAR", 36).notNull();
        invs[2] = database.createColumn("items").type("BLOB").notNull();
        String enderChestQuery = database.createQuery().createTable(prefix + "ender_chests", true, invs).buildQuery();
        PreparedStatement enderChest = database.prepareStatement(enderChestQuery);
        database.updateDb(enderChest);
        
        String armorQuery = database.createQuery().createTable(prefix + "armor", true, invs).buildQuery();
        PreparedStatement armor = database.prepareStatement(armorQuery);
        database.updateDb(armor);
        
        String inventoryQuery = database.createQuery().createTable(prefix + "inventory", true, invs).buildQuery();
        PreparedStatement inventory = database.prepareStatement(inventoryQuery);
        database.updateDb(inventory);
        
        Column[] ps = new Column[12];
        ps[0] = database.createColumn("id").type("INT").notNull().primaryKey().autoIncrement();
        ps[1] = database.createColumn("uuid").type("CHAR", 36).notNull();
        ps[2] = database.createColumn("can_fly").type("BIT");
        ps[3] = database.createColumn("display_name").type("VARCHAR", 16);
        ps[4] = database.createColumn("exhaustion").type("FLOAT");
        ps[5] = database.createColumn("exp").type("FLOAT");
        ps[6] = database.createColumn("flying").type("BIT");
        ps[7] = database.createColumn("food").type("INT", 20);
        ps[8] = database.createColumn("health").type("DOUBLE");
        ps[9] = database.createColumn("level").type("INT");
        ps[10] = database.createColumn("potion_effects").type("BLOB");
        ps[11] = database.createColumn("saturation").type("INT", 20);
        String playerStatsQuery = database.createQuery().createTable(prefix + "player_stats", true, ps).buildQuery();
        PreparedStatement playerStats = database.prepareStatement(playerStatsQuery);
        database.updateDb(playerStats);
    }
}
