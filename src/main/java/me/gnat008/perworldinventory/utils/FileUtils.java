package me.gnat008.perworldinventory.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.GameMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;

/**
 * Class with methods to handle file I/O.
 */
public final class FileUtils {

    /**
     * Read Json data from a file.
     *
     * @param file The file to read from.
     * @return The Json data in the file as a {@link JsonObject}.
     * @throws IOException If the file is not found, or another exception occurs.
     */
    public static JsonObject readData(File file) throws IOException {
        JsonObject data;
        try (JsonReader reader = new JsonReader(new FileReader(file))) {
            JsonParser parser = new JsonParser();
            data = parser.parse(reader).getAsJsonObject();
        }

        return data;
    }

    /**
     * Write a string of data to a file.
     *
     * @param file The file to write to.
     * @param data The data to write.
     */
    public static void writeData(final File file, final String data) {
        try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
            writer.write(data);
        } catch (IOException ex) {
            PwiLogger.severe("Could not write data to file '" + file + "':", ex);
        }
    }

    /**
     * Get the data file for a player.
     *
     * @param dataFolder The location where all player data folders reside.
     * @param gamemode The game mode for the group we are looking for.
     * @param group The group we are looking for.
     * @param uuid The UUID of the player.
     *
     * @return The data file to read from or write to.
     */
    public static File getFile(File dataFolder, GameMode gamemode, Group group, UUID uuid) {
        File userFolder = new File(dataFolder, uuid.toString());
        File file;
        switch(gamemode) {
            case ADVENTURE:
                file = new File(userFolder, group.getName() + "_adventure.json");
                break;
            case CREATIVE:
            case SPECTATOR:
                file = new File(userFolder, group.getName() + "_creative.json");
                break;
            default:
                file = new File(userFolder, group.getName() + ".json");
                break;
        }

        return file;
    }
}
