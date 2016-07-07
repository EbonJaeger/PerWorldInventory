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
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.data.serializers.LocationSerializer;
import me.gnat008.perworldinventory.data.serializers.PlayerSerializer;
import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.io.*;
import java.util.UUID;

public class FileSerializer extends DataSerializer {

    private final String FILE_PATH;

    @Inject
    FileSerializer(PerWorldInventory plugin) {
        super(plugin);

        this.FILE_PATH = plugin.getDataFolder() + File.separator + "data" + File.separator;
    }

    public void saveLogoutData(PWIPlayer player) {
        File file = new File(FILE_PATH + player.getUuid().toString(), "last-logout.json");

        try {
            if (!file.getParentFile().exists())
                file.getParentFile().mkdir();
            if (!file.exists())
                file.createNewFile();

            String data = LocationSerializer.serialize(player.getLocation());
            writeData(file, data);
        } catch (IOException ex) {
            plugin.getLogger().warning("Error creating file '" + file.getPath() + "': " + ex.getMessage());
        }
    }

    public void saveToDatabase(Group group, GameMode gamemode, PWIPlayer player) {
        File file = getFile(gamemode, group, player.getUuid());

        if (Settings.getBoolean("debug-mode"))
            PerWorldInventory.printDebug("Saving data for player '" + player.getName() + "' in file '" + file.getPath() + "'");

        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

            if (Settings.getBoolean("debug-mode"))
                PerWorldInventory.printDebug("Writing player data for player '" + player.getName() + "' to file");

            String data = PlayerSerializer.serialize(plugin, player);
            writeData(file, data);
        } catch (IOException ex) {
            plugin.getLogger().severe("Error creating file '" + FILE_PATH +
                    player.getUuid() + File.separator + group.getName() + ".json': " + ex.getMessage());
        }
    }

    public void writeData(final File file, final String data) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(data);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void getFromDatabase(Group group, GameMode gamemode, Player player) {
        File file = getFile(gamemode, group, player.getUniqueId());

        if (Settings.getBoolean("debug-mode"))
            PerWorldInventory.printDebug("Getting data for player '" + player.getName() + "' from file '" + file.getPath() + "'");

        try (JsonReader reader = new JsonReader(new FileReader(file))) {
            JsonParser parser = new JsonParser();
            JsonObject data = parser.parse(reader).getAsJsonObject();
            PlayerSerializer.deserialize(data, player, plugin);
        } catch (FileNotFoundException ex) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }

            if (Settings.getBoolean("debug-mode"))
                PerWorldInventory.printDebug("File not found for player '" + player.getName() + "' for group '" + group.getName() + "'. Getting data from default sources");

            getFromDefaults(group, player);
        } catch (IOException exIO) {
            plugin.getLogger().severe("Unable to read data for '" + player.getName() + "' for group '" + group.getName() +
                    "' in gamemode '" + gamemode.toString() + "' for reason: " + exIO.getMessage());
        }
    }

    public Location getLogoutData(Player player) {
        File file = new File(FILE_PATH + player.getUniqueId().toString(), "last-logout.json");

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
            plugin.getLogger().warning("Unable to get logout location data for '" + player.getName() + "': " + ioEx.getMessage());
            location = null;
        }

        return location;
    }

    public void getFromDefaults(Group group, Player player) {
        File file = new File(FILE_PATH + "defaults", group.getName() + ".json");

        try (JsonReader reader = new JsonReader(new FileReader(file))) {
            JsonParser parser = new JsonParser();
            JsonObject data = parser.parse(reader).getAsJsonObject();
            PlayerSerializer.deserialize(data, player, plugin);
        } catch (FileNotFoundException ex) {
            file = new File(FILE_PATH + "defaults", "__default.json");

            try (JsonReader reader = new JsonReader(new FileReader(file))) {
                JsonParser parser = new JsonParser();
                JsonObject data = parser.parse(reader).getAsJsonObject();
                PlayerSerializer.deserialize(data, player, plugin);
            } catch (FileNotFoundException ex2) {
                player.sendMessage(ChatColor.RED + "» " + ChatColor.GRAY + "Something went horribly wrong when loading your inventory! " +
                        "Please notify a server administrator!");
                plugin.getLogger().severe("Unable to find inventory data for player '" + player.getName() +
                        "' for group '" + group.getName() + "': " + ex2.getMessage());
            } catch (IOException exIO) {
                plugin.getLogger().severe("Unable to read data for '" + player.getName() + "' for group '" + group.getName() +
                        "' for reason: " + exIO.getMessage());
            }
        } catch (IOException exIO) {
            plugin.getLogger().severe("Unable to read data for '" + player.getName() + "' for group '" + group.getName() +
                    "' for reason: " + exIO.getMessage());
        }
    }

    /**
     * Get the data file for a player.
     *
     * @param gamemode The game mode for the group we are looking for
     * @param group The group we are looking for
     * @param uuid The UUID of the player
     * @return The data file
     */
    public File getFile(GameMode gamemode, Group group, UUID uuid) {
        File file;
        switch(gamemode) {
            case ADVENTURE:
                file = new File(FILE_PATH + uuid.toString(), group.getName() + "_adventure.json");
                break;
            case CREATIVE:
                file = new File(FILE_PATH + uuid.toString(), group.getName() + "_creative.json");
                break;
            case SPECTATOR:
                file = new File(FILE_PATH + uuid.toString(), group.getName() + "_creative.json");
                break;
            default:
                file = new File(FILE_PATH + uuid.toString(), group.getName() + ".json");
                break;
        }

        return file;
    }
}
