package me.gnat008.perworldinventory.config;

import com.github.authme.configme.Comment;
import com.github.authme.configme.SectionComments;
import com.github.authme.configme.SettingsHolder;
import com.github.authme.configme.properties.Property;
import me.gnat008.perworldinventory.data.DataSourceType;

import java.util.HashMap;
import java.util.Map;

import static com.github.authme.configme.properties.PropertyInitializer.newProperty;

/**
 * Holds the properties for the SQL database
 */
public final class DatabaseProperties implements SettingsHolder {

    @Comment({"Type of database to use",
        "Valid values: flatfile, sqlite, mysql"})
    public static final Property<DataSourceType> BACKEND =
            newProperty(DataSourceType.class, "database.type", DataSourceType.FLATFILE);

    @Comment("Enable database caching if using SQL; may improve performance")
    public static final Property<Boolean> USE_CACHING =
            newProperty("database.caching", true);

    @Comment("MySQL database host address")
    public static final Property<String> HOSTNAME =
            newProperty("database.hostname", "localhost");

    @Comment("MySQL database port number")
    public static final Property<String> PORT =
            newProperty("database.port", "3306");

    @Comment("MySQL database username")
    public static final Property<String> USERNAME =
            newProperty("database.username", "admin");

    @Comment("MySQL database password")
    public static final Property<String> PASSWORD =
            newProperty("database.password", "12345");

    @Comment("Name of the database to use")
    public static final Property<String> DATABASE_NAME =
            newProperty("database.databaseName", "perworldinventory");

    @Comment({"Override the size of the database connection pool",
        "Set to -1 for this to be determined automatically"})
    public static final Property<Integer> DATABASE_POOL_SIZE =
            newProperty("database.poolSize", -1);

    private DatabaseProperties() {}

    @SectionComments
    public static Map<String, String[]> buildSectionComments() {
        Map<String, String[]> comments = new HashMap<>();
        comments.put("database", new String[]{
                "All database settings are here",
                "If the type is set to flatfile, no other settings here will do anything"});

        return comments;
    }
}
