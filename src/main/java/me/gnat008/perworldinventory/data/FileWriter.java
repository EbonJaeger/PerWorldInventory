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
import me.gnat008.perworldinventory.DataFolder;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
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

public class FileWriter implements DataWriter {

    private final File FILE_PATH;

    private final PerWorldInventory plugin;
    private final PlayerSerializer playerSerializer;

    @Inject
    FileWriter(@DataFolder File dataFolder, PerWorldInventory plugin, PlayerSerializer playerSerializer) {
        this.FILE_PATH = new File(dataFolder, "data");
        this.plugin = plugin;
        this.playerSerializer = playerSerializer;
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
        File file = getFile(gamemode, group, player.getUuid());
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

    public void writeData(final File file, final String data) {
        java.io.FileWriter writer = null;
        try {
            writer = new java.io.FileWriter(file);
            writer.write(data);
        } catch (IOException ex) {
            PwiLogger.severe("Could not write data to file '" + file + "':", ex);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ex) {
                PwiLogger.warning("Failed to clease writer for '" + file + "':", ex);
            }
        }
    }

    @Override
    public void getFromDatabase(Group group, GameMode gamemode, Player player) {
        File file = getFile(gamemode, group, player.getUniqueId());

        PwiLogger.debug("Getting data for player '" + player.getName() + "' from file '" + file.getPath() + "'");

        try (JsonReader reader = new JsonReader(new FileReader(file))) {
            JsonParser parser = new JsonParser();
            JsonObject data = parser.parse(reader).getAsJsonObject();
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

    private void getFromDefaults(Group group, Player player) {
        File file = new File(FILE_PATH, "defaults" + File.separator + group.getName() + ".json");

        try (JsonReader reader = new JsonReader(new FileReader(file))) {
            JsonParser parser = new JsonParser();
            JsonObject data = parser.parse(reader).getAsJsonObject();
            playerSerializer.deserialize(data, player);
        } catch (FileNotFoundException ex) {
            file = new File(FILE_PATH, "defaults" + File.separator + "__default.json");

            try (JsonReader reader = new JsonReader(new FileReader(file))) {
                JsonParser parser = new JsonParser();
                JsonObject data = parser.parse(reader).getAsJsonObject();
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

    /**
     * Set the default inventory loadout for a group. This is the inventory that will
     * be given to a player the first time they enter a world in the group.
     * <p>
     * A snapshot of the player will be taken and saved to a temp file to be deleted after.
     * This is so some stats are set to max, e.g. health. The snapshot will be restored to
     * the player after the default loadout has been saved.
     *
     * @param player The player performing the command.
     * @param group The group to write the defaults for.
     */
    public void setGroupDefault(Player player, Group group) {
        File file = new File(plugin.getDefaultFilesDirectory(), group.getName() + ".json");
        if (!file.exists()) {
            player.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "Default file for this group not found!");
            return;
        }

        File tmp = new File(getUserFolder(player.getUniqueId()), "tmp.json");
        try {
            tmp.getParentFile().mkdirs();
            tmp.createNewFile();
        } catch (IOException ex) {
            player.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY +  "Could not create temporary file! Aborting!");
            return;
        }
        Group tempGroup = new Group("tmp", null, null);
        writeData(tmp, playerSerializer.serialize(new PWIPlayer(plugin, player, tempGroup)));

        player.setFoodLevel(20);
        player.setHealth(player.getMaxHealth());
        player.setSaturation(20);
        player.setTotalExperience(0);
        player.setRemainingAir(player.getMaximumAir());
        player.setFireTicks(0);

        writeData(file, playerSerializer.serialize(new PWIPlayer(plugin, player, group)));

        getFromDatabase(tempGroup, GameMode.SURVIVAL, player);
        tmp.delete();
        player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY +  "Defaults for '" + group.getName() + "' set!");
    }
}
