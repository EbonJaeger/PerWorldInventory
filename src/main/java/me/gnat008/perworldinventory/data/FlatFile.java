package me.gnat008.perworldinventory.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import me.gnat008.perworldinventory.DataFolder;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.data.players.PWIPlayerFactory;
import me.gnat008.perworldinventory.data.serializers.LocationSerializer;
import me.gnat008.perworldinventory.data.serializers.PlayerSerializer;
import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;

import static me.gnat008.perworldinventory.utils.FileUtils.getFile;
import static me.gnat008.perworldinventory.utils.FileUtils.readData;
import static me.gnat008.perworldinventory.utils.FileUtils.writeData;

public class FlatFile implements DataSource {

    private final File DATA_FOLDER;

    private PerWorldInventory plugin;
    private PlayerSerializer playerSerializer;
    private PWIPlayerFactory pwiPlayerFactory;

    FlatFile(File dataFolder, PerWorldInventory plugin,
             PlayerSerializer serializer, PWIPlayerFactory pwiPlayerFactory) throws IOException {
        this.DATA_FOLDER = dataFolder;
        this.plugin = plugin;
        this.playerSerializer = serializer;
        this.pwiPlayerFactory = pwiPlayerFactory;

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IOException("Could not create data directory '" + dataFolder.getPath() + "'");
        }
    }

    @Override
    public void saveLogoutData(PWIPlayer player) {
        File file = new File(getUserFolder(player.getUuid()), "last-logout.json");

        try {
            if (!file.getParentFile().exists())
                file.getParentFile().mkdir();
            if (!file.exists())
                file.createNewFile();

            String data = LocationSerializer.serialize(player.getLocation());
            writeData(file, data);
        } catch (IOException ex) {
            PwiLogger.warning("Error creating file '" + file.getPath() + "':", ex);
        }
    }

    @Override
    public void saveToDatabase(final Group group, final GameMode gamemode, final PWIPlayer player, boolean async) {
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveToDatabase(group, gamemode, player));
        } else {
            saveToDatabase(group, gamemode, player);
        }
    }

    @Override
    public void saveToDatabase(Group group, GameMode gamemode, PWIPlayer player) {
        File file = getFile(DATA_FOLDER, gamemode, group, player.getUuid());
        PwiLogger.debug("Saving data for player '" + player.getName() + "' in file '" + file.getPath() + "'");

        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

            PwiLogger.debug("Writing player data for player '" + player.getName() + "' to file");

            String data = playerSerializer.serialize(player);
            writeData(file, data);
        } catch (IOException ex) {
            PwiLogger.severe("Error creating file '" + file + "':", ex);
        }
    }

    @Override
    public void getFromDatabase(Group group, GameMode gamemode, Player player) {
        File file = getFile(DATA_FOLDER, gamemode, group, player.getUniqueId());

        PwiLogger.debug("Getting data for player '" + player.getName() + "' from file '" + file.getPath() + "'");

        JsonObject data;
        try {
            data = readData(file);
            playerSerializer.deserialize(data, player);
        } catch (FileNotFoundException ex) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }

            PwiLogger.debug("File not found for player '" + player.getName() + "' for group '" + group.getName() + "'. Getting data from default sources");

            getFromDefaults(group, player);
        } catch (IOException exIO) {
            PwiLogger.severe("Unable to read data for '" + player.getName() + "' for group '" + group.getName() +
                    "' in gamemode '" + gamemode.toString() + "' for reason:", exIO);
        }
    }

    @Override
    public Location getLogoutData(Player player) {
        File file = new File(getUserFolder(player.getUniqueId()), "last-logout.json");

        Location location;
        try (JsonReader reader = new JsonReader(new FileReader(file))) {
            JsonParser parser = new JsonParser();
            JsonObject data = parser.parse(reader).getAsJsonObject();
            location = LocationSerializer.deserialize(data);
        } catch (FileNotFoundException ex) {
            // Player probably logged in for the first time, not really an error
            location = null;
        } catch (IOException ioEx) {
            // Something went wrong
            PwiLogger.warning("Unable to get logout location data for '" + player.getName() + "':", ioEx);
            location = null;
        }

        return location;
    }

    @Override
    public void close() {
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.FLATFILE;
    }

    @Override
    public void reload() {
    }

    private void getFromDefaults(Group group, Player player) {
        File file = new File(DATA_FOLDER, "defaults" + File.separator + group.getName() + ".json");
        JsonObject data;

        try {
            data = readData(file);
            playerSerializer.deserialize(data, player);
        } catch (FileNotFoundException ex) {
            file = new File(DATA_FOLDER, "defaults" + File.separator + "__default.json");

            try {
                data = readData(file);
                playerSerializer.deserialize(data, player);
            } catch (FileNotFoundException ex2) {
                player.sendMessage(ChatColor.RED + "» " + ChatColor.GRAY + "Something went horribly wrong when loading your inventory! " +
                        "Please notify a server administrator!");
                PwiLogger.severe("Unable to find inventory data for player '" + player.getName() +
                        "' for group '" + group.getName() + "':", ex2);
            } catch (IOException exIO) {
                PwiLogger.severe("Unable to read data for '" + player.getName() + "' for group '" + group.getName() +
                        "' for reason:", exIO);
            }
        } catch (IOException exIO) {
            PwiLogger.severe("Unable to read data for '" + player.getName() + "' for group '" + group.getName() +
                    "' for reason:", exIO);
        }
    }

    /**
     * Return the folder in which data is stored for the player.
     *
     * @param uuid The player's UUID
     * @return The data folder of the player
     */
    private File getUserFolder(UUID uuid) {
        return new File(DATA_FOLDER, uuid.toString());
    }
}
