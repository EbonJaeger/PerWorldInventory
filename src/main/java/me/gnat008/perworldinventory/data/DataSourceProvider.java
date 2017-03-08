package me.gnat008.perworldinventory.data;

import ch.jalu.injector.Injector;
import me.gnat008.perworldinventory.ConsoleLogger;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;

/**
 * Creates the data source
 */
public class DataSourceProvider implements Provider<DataSource> {

    @Inject
    private Injector injector;

    DataSourceProvider() {}

    @Override
    public DataSource get() {
        try {
            return createDataSource();
        } catch (Exception ex) {
            ConsoleLogger.severe("Unable to create data source:", ex);
            throw new IllegalStateException("Error during initialization of data source", ex);
        }
    }

    private DataSource createDataSource() throws ClassNotFoundException, IOException {
        DataSourceType type = DataSourceType.FLATFILE;
        DataSource dataSource;

        switch(type) {
            case FLATFILE:
                dataSource = injector.getSingleton(FlatFile.class);
                break;
            default:
                throw new UnsupportedOperationException("Unknown data source type '" + type + "'");
        }

        return dataSource;
    }
}