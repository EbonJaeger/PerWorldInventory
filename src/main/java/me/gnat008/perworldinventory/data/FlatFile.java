/*
 * Copyright (C) 2014-2016  Gnat008
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

package me.gnat008.perworldinventory.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import me.gnat008.perworldinventory.BukkitService;
import me.gnat008.perworldinventory.ConsoleLogger;
import me.gnat008.perworldinventory.DataFolder;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.data.players.PWIPlayerFactory;
import me.gnat008.perworldinventory.data.serializers.DeserializeCause;
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
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.UUID;

import static me.gnat008.perworldinventory.util.FileUtils.createFileIfNotExists;
import static me.gnat008.perworldinventory.util.FileUtils.writeData;
import static me.gnat008.perworldinventory.util.Utils.zeroPlayer;

public class FlatFile implements DataSource {

    private final File FILE_PATH;

    private final PerWorldInventory plugin;
    private final BukkitService bukkitService;
    private final PlayerSerializer playerSerializer;
    private final PWIPlayerFactory pwiPlayerFactory;

    @Inject
    FlatFile(@DataFolder File dataFolder, PerWorldInventory plugin, BukkitService bukkitService, PlayerSerializer playerSerializer,
             PWIPlayerFactory pwiPlayerFactory) {
        this.FILE_PATH = new File(dataFolder, "data");
        this.plugin = plugin;
        this.bukkitService = bukkitService;
        this.playerSerializer = playerSerializer;
        this.pwiPlayerFactory = pwiPlayerFactory;
    }

    @Override
    public void saveLogoutData(PWIPlayer player, boolean createTask) {
        File file = new File(getUserFolder(player.getUuid()), "last-logout.json");

        if (createTask) {
            bukkitService.runTaskAsync(() -> saveLogout(file, player));
        } else {
            saveLogout(file, player);
        }
    }

    private void saveLogout(File file, PWIPlayer player) {
        try {
            createFileIfNotExists(file);
        } catch (IOException ex) {
            if (!(ex instanceof FileAlreadyExistsException)) {
                ConsoleLogger.severe("Error creating file '" + file.getPath() + "':", ex);
                return;
            }
        }

        String data = LocationSerializer.serialize(player.getLocation());
        writeData(file, data);
    }

    @Override
    public void saveToDatabase(Group group, GameMode gamemode, PWIPlayer player) {
        File file = getFile(gamemode, group, player.getUuid());
        ConsoleLogger.debug("Saving data for player '" + player.getName() + "' in file '" + file.getPath() + "'");

        try {
            createFileIfNotExists(file);
        } catch (IOException ex) {
            if (!(ex instanceof FileAlreadyExistsException)) {
                ConsoleLogger.severe("Error creating file '" + file.getPath() + "':", ex);
                return;
            }
        }

        ConsoleLogger.debug("Writing player data for player '" + player.getName() + "' to file");

        String data = playerSerializer.serialize(player);
        writeData(file, data);
    }

    @Override
    public void getFromDatabase(Group group, GameMode gamemode, Player player, DeserializeCause cause) {
        File file = getFile(gamemode, group, player.getUniqueId());

        ConsoleLogger.debug("Getting data for player '" + player.getName() + "' from file '" + file.getPath() + "'");

        bukkitService.runTaskAsync(() -> {
            try (JsonReader reader = new JsonReader(new FileReader(file))) {
                JsonParser parser = new JsonParser();
                JsonObject data = parser.parse(reader).getAsJsonObject();

                bukkitService.runTask(() -> playerSerializer.deserialize(data, player, cause));
            } catch (FileNotFoundException ex) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdir();
                }

                ConsoleLogger.debug("File not found for player '" + player.getName() + "' for group '" + group.getName() + "'. Getting data from default sources");

                getFromDefaults(group, player, cause);
            } catch (IOException exIO) {
                ConsoleLogger.severe("Unable to read data for '" + player.getName() + "' for group '" + group.getName() +
                        "' in gamemode '" + gamemode.toString() + "' for reason:", exIO);
            }
        });
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
            ConsoleLogger.warning("Unable to get logout location data for '" + player.getName() + "':", ioEx);
            location = null;
        }

        return location;
    }

    private void getFromDefaults(Group group, Player player, DeserializeCause cause) {
        File file = new File(FILE_PATH + File.separator + "defaults", group.getName() + ".json");

        try (JsonReader reader = new JsonReader(new FileReader(file))) {
            JsonParser parser = new JsonParser();
            JsonObject data = parser.parse(reader).getAsJsonObject();

            bukkitService.runTask(() -> playerSerializer.deserialize(data, player, cause));
        } catch (FileNotFoundException ex) {
            file = new File(FILE_PATH + File.separator + "defaults", "__default.json");

            try (JsonReader reader = new JsonReader(new FileReader(file))) {
                JsonParser parser = new JsonParser();
                JsonObject data = parser.parse(reader).getAsJsonObject();
                bukkitService.runTask(() -> playerSerializer.deserialize(data, player, cause));
            } catch (FileNotFoundException ex2) {
                player.sendMessage(ChatColor.RED + "» " + ChatColor.GRAY + "Something went horribly wrong when loading your inventory! " +
                        "Please notify a server administrator!");
                ConsoleLogger.severe("Unable to find inventory data for player '" + player.getName() +
                        "' for group '" + group.getName() + "':", ex2);
            } catch (IOException exIO) {
                ConsoleLogger.severe("Unable to read data for '" + player.getName() + "' for group '" + group.getName() +
                        "' for reason:", exIO);
            }
        } catch (IOException exIO) {
            ConsoleLogger.severe("Unable to read data for '" + player.getName() + "' for group '" + group.getName() +
                    "' for reason:", exIO);
        }
    }

    /**
     * Get the data file for a player.
     *
     * @param gamemode The game mode for the group we are looking for.
     * @param group The group we are looking for.
     * @param uuid The UUID of the player.
     *
     * @return The data file to read from or write to.
     */
    public File getFile(GameMode gamemode, Group group, UUID uuid) {
        File dir = getUserFolder(uuid);
        File file;
        switch(gamemode) {
            case ADVENTURE:
                file = new File(dir, group.getName() + "_adventure.json");
                break;
            case CREATIVE:
            case SPECTATOR:
                file = new File(dir, group.getName() + "_creative.json");
                break;
            default:
                file = new File(dir, group.getName() + ".json");
                break;
        }

        return file;
    }

    /**
     * Return the folder in which data is stored for the player.
     *
     * @param uuid The player's UUID
     * @return The data folder of the player
     */
    private File getUserFolder(UUID uuid) {
        return new File(FILE_PATH, uuid.toString());
    }

    @Override
    public void setGroupDefault(Player player, Group group) {
        File file = new File(plugin.getDefaultFilesDirectory(), group.getName() + ".json");
        if (!file.exists()) {
            player.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "Default file for this group not found!");
            return;
        }

        File tmp = new File(getUserFolder(player.getUniqueId()), "tmp.json");
        try {
            createFileIfNotExists(tmp);
        } catch (IOException ex) {
            if (!(ex instanceof FileAlreadyExistsException)) {
                player.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY +  "Could not create temporary file! Aborting!");
                return;
            }
        }
        Group tempGroup = new Group("tmp", null, null);
        writeData(tmp, playerSerializer.serialize(pwiPlayerFactory.create(player, tempGroup)));

        zeroPlayer(plugin, player, false);

        writeData(file, playerSerializer.serialize(pwiPlayerFactory.create(player, group)));

        getFromDatabase(tempGroup, GameMode.SURVIVAL, player, DeserializeCause.CHANGED_DEFAULTS);
        tmp.delete();
        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY +  "Defaults for '" + group.getName() + "' set!");
    }
}
