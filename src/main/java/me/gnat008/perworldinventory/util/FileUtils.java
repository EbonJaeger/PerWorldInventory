package me.gnat008.perworldinventory.util;

import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.GameMode;

import java.io.*;
import java.util.UUID;

/**
 * Utility methods for handling files.
 */
public class FileUtils {

    /**
     * Copy a file from one location to another. The file will not be deleted.
     *
     * @param from The original location of the file.
     * @param to The location the file will be copied to.
     * @throws IOException If an error is encountered while copying.
     */
    public static void copyFile(File from, File to) throws IOException {
        try (InputStream in = new FileInputStream(from);
             OutputStream out = new FileOutputStream(to)) {
            byte[] buff = new byte[1024];
            int len;
            while ((len = in.read(buff)) > 0) {
                out.write(buff, 0, len);
            }
        }
    }

    /**
     * Write a string of data to a file.
     *
     * @param file The file to write to.
     * @param data The data to write.
     */
    public static void writeData(final File file, final String data) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(data);
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
    public static File getFile(File directory, GameMode gamemode, Group group, UUID uuid) {
        File file;
        switch(gamemode) {
            case ADVENTURE:
                file = new File(directory, group.getName() + "_adventure.json");
                break;
            case CREATIVE:
            case SPECTATOR:
                file = new File(directory, group.getName() + "_creative.json");
                break;
            default:
                file = new File(directory, group.getName() + ".json");
                break;
        }

        return file;
    }
}
