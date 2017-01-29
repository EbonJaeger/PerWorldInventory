package me.gnat008.perworldinventory.data;

import com.google.gson.JsonObject;
import me.gnat008.perworldinventory.BukkitService;
import me.gnat008.perworldinventory.DataFolder;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.data.serializers.LocationSerializer;
import me.gnat008.perworldinventory.data.serializers.PlayerSerializer;
import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import static me.gnat008.perworldinventory.utils.FileUtils.*;

public class FlatFile implements DataSource {

    private final File dataFolder;
    private final PerWorldInventory plugin;
    private final BukkitService bukkitService;
    private final PlayerSerializer playerSerializer;

    @Inject
    FlatFile(@DataFolder File dataFolder, PerWorldInventory plugin, BukkitService bukkitService, PlayerSerializer serializer) throws IOException {
        this.dataFolder = dataFolder;
        this.plugin = plugin;
        this.bukkitService = bukkitService;
        this.playerSerializer = serializer;

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IOException("Could not create data directory '" + dataFolder.getPath() + "'");
        }
    }

    @Override
    public void saveLogoutData(PWIPlayer player, boolean async) {
        File file = new File(getUserFolder(player.getUuid()), "last-logout.json");

        bukkitService.runTaskOptionallyAsync(() -> {
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
        }, async);

    }

    @Override
    public void saveToDatabase(final Group group, final GameMode gamemode, final PWIPlayer player, boolean async) {
        bukkitService.runTaskOptionallyAsync(() -> saveToDatabase(group, gamemode, player), async);
    }

    @Override
    public void saveToDatabase(Group group, GameMode gamemode, PWIPlayer player) {
        File file = getFile(dataFolder, gamemode, group, player.getUuid());
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
        File file = getFile(dataFolder, gamemode, group, player.getUniqueId());

        PwiLogger.debug("Getting data for player '" + player.getName() + "' from file '" + file.getPath() + "'");

        bukkitService.runTaskAsync(() -> {
            try {
                JsonObject data = readData(file);

                bukkitService.runTask(() -> playerSerializer.deserialize(data, player));
            } catch (FileNotFoundException ex) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdir();
                }

                PwiLogger.debug("File not found for player '" + player.getName() + "' for group '" + group.getName() + "'. Getting data from default sources");

                getFromDefaults(group, player);
            } catch (IOException ioEx) {
                PwiLogger.severe("Unable to read data for '" + player.getName() + "' for group '" + group.getName() +
                        "' in gamemode '" + gamemode.toString() + "' for reason:", ioEx);
            }
        });
    }

    @Override
    public Location getLogoutData(Player player) {
        File file = new File(getUserFolder(player.getUniqueId()), "last-logout.json");
        Location location;

        try {
            JsonObject data = readData(file);
            location = LocationSerializer.deserialize(data);
        } catch (IOException ex) {
            if (ex instanceof FileNotFoundException) {
                // Player probably logged in for the first time, not really an error
                location = null;
            } else {
                // Something went wrong
                PwiLogger.warning("Unable to get logout location data for '" + player.getName() + "':", ex);
                location = null;
            }
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
        final File file = new File(dataFolder, "defaults" + File.separator + group.getName() + ".json");

        bukkitService.runTaskAsync(() -> {
            try {
                JsonObject data = readData(file);

                bukkitService.runTask(() -> playerSerializer.deserialize(data, player));
            } catch (FileNotFoundException ex) {
               final File serverDef = new File(dataFolder, "defaults" + File.separator + "__default.json");

               try {
                   JsonObject defData = readData(serverDef);

                   bukkitService.runTask(() -> playerSerializer.deserialize(defData, player));
               } catch (FileNotFoundException ex2) {
                   player.sendMessage(ChatColor.RED + "» " + ChatColor.GRAY + "Something went horribly wrong when loading your inventory! " +
                           "Please notify a server administrator!");
                   PwiLogger.severe("Unable to find inventory data for player '" + player.getName() +
                           "' for group '" + group.getName() + "':", ex2);
               } catch (IOException ioEx2) {
                   PwiLogger.severe("Unable to read data for '" + player.getName() + "' for group '" + group.getName() +
                           "' for reason:", ioEx2);
               }
            } catch (IOException ioEx) {
                PwiLogger.severe("Unable to read data for '" + player.getName() + "' for group '" + group.getName() +
                        "' for reason:", ioEx);
            }
        });
    }

    /**
     * Return the folder in which data is stored for the player.
     *
     * @param uuid The player's UUID
     * @return The data folder of the player
     */
    private File getUserFolder(UUID uuid) {
        return new File(dataFolder, uuid.toString());
    }
}
