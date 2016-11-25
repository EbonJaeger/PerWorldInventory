package me.gnat008.perworldinventory.data;

import ch.jalu.injector.Injector;
import me.gnat008.perworldinventory.DataFolder;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.config.DatabaseProperties;
import me.gnat008.perworldinventory.config.Settings;
import org.bukkit.Bukkit;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Creates the data source
 */
public class DataSourceProvider implements Provider<DataSource> {

    private static final int SQLITE_MAX_SIZE = 4000;

    @Inject
    @DataFolder
    private File dataFolder;
    @Inject
    private Settings settings;
    @Inject
    private PerWorldInventory plugin;
    @Inject
    private Injector injector;

    DataSourceProvider() {}

    @Override
    public DataSource get() {
        try {
            return createDataSource();
        } catch (Exception ex) {
            PwiLogger.severe("Unable to create data source:", ex);
            throw new IllegalStateException("Error during initialization of data source", ex);
        }
    }

    private DataSource createDataSource() throws ClassNotFoundException, SQLException, IOException {
        DataSourceType type = settings.getProperty(DatabaseProperties.BACKEND);
        DataSource dataSource;

        switch(type) {
            case FLATFILE:
                dataSource = injector.getSingleton(FlatFile.class);
                break;
            case MYSQL:
                dataSource = injector.getSingleton(MySQL.class);
                break;
            case SQLITE:
                throw new UnsupportedOperationException("SQLite not yet implemented");
            default:
                throw new UnsupportedOperationException("Unknown data source type '" + type + "'");
        }

        return dataSource;
    }
}
