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

import com.kill3rtaco.tacoserialization.PlayerSerialization;
import com.kill3rtaco.tacoserialization.Serializer;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.util.Printer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class FileSerializer extends DataSerializer {

    private final String FILE_PATH;

    public FileSerializer(PerWorldInventory plugin) {
        super(plugin);

        this.FILE_PATH = plugin.getDataFolder() + File.separator + "data" + File.separator;
    }

    public void saveToDatabase(Group group, GameMode gamemode, PWIPlayer player) {
        File file = getFile(gamemode, group, player.getUuid());

        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

            JSONObject data = PlayerSerialization.serializePlayer(player, plugin);
            writeData(file, Serializer.toString(data));
        } catch (IOException ex) {
            Printer.getInstance(plugin).printToConsole("Error creating file '" + FILE_PATH +
                    player.getUuid() + File.separator + group.getName() + ".json': " + ex.getMessage(), true);
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

        try {
            JSONObject data = Serializer.getObjectFromFile(file);
            PlayerSerialization.setPlayer(data, player, plugin);
        } catch (FileNotFoundException | JSONException ex) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }

            getFromDefaults(group, player);
        }
    }

    public void getFromDefaults(Group group, Player player) {
        File file = new File(FILE_PATH + "defaults", group.getName() + ".json");

        try {
            JSONObject defaultGroupData = Serializer.getObjectFromFile(file);
            PlayerSerialization.setPlayer(defaultGroupData, player, plugin);
        } catch (FileNotFoundException ex) {
            file = new File(FILE_PATH + "defaults", "__default.json");

            try {
                JSONObject defaultGroupData = Serializer.getObjectFromFile(file);
                PlayerSerialization.setPlayer(defaultGroupData, player, plugin);
            } catch (FileNotFoundException ex2) {
                plugin.getPrinter().printToPlayer(player, "Something went horribly wrong when loading your inventory! " +
                        "Please notify a server administrator!", true);
                plugin.getLogger().severe("Unable to find inventory data for player '" + player.getName() +
                        "' for group '" + group.getName() + "': " + ex2.getMessage());
            }
        }
    }

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
