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

package me.gnat008.perworldinventory.data;

import com.kill3rtaco.tacoserialization.PlayerSerialization;
import com.kill3rtaco.tacoserialization.Serializer;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.util.Printer;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class DataSerializer {

    private PerWorldInventory plugin;

    private final String FILE_PATH;

    private static DataSerializer instance = null;

    private DataSerializer(PerWorldInventory plugin) {
        this.plugin = plugin;
        FILE_PATH = plugin.getDataFolder() + File.separator + "data";
    }

    public static DataSerializer getInstance(PerWorldInventory plugin) {
        if (instance == null) {
            instance = new DataSerializer(plugin);
        }

        return instance;
    }

    public static void disable() {
        instance = null;
    }

    public void writePlayerDataToFile(Player player, JSONObject data, String world) {
        File file = new File(FILE_PATH + File.separator + player.getUniqueId().toString(),
                world + ".json");

        FileWriter writer = null;
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

            writer = new FileWriter(file);
            writer.write(Serializer.toString(data));
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

    public void getPlayerDataFromFile(Player player, String world) {
        File file = new File(FILE_PATH + File.separator + player.getUniqueId().toString(),
                world + ".json");
        try {
            JSONObject data = Serializer.getObjectFromFile(file);
            PlayerSerialization.setPlayer(data, player);
        } catch (FileNotFoundException ex) {
            try {
                file.createNewFile();
            } catch (IOException exIO) {
                Printer.getInstance(plugin).printToConsole("Error creating file '" + FILE_PATH + File.separator +
                        player.getUniqueId().toString() + world + ".json", true);
            }
        }
    }
}
