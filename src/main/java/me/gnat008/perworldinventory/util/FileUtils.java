package me.gnat008.perworldinventory.util;

import me.gnat008.perworldinventory.ConsoleLogger;

import java.io.*;
import java.nio.file.Files;

/**
 * Utility methods for handling files.
 */
public final class FileUtils {

    private FileUtils() {
    }

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
     * Writes the given data to the provided file.
     *
     * @param file The file to write to.
     * @param data The data to write.
     */
    public static void writeData(File file, String data) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(data);
        } catch (IOException ex) {
            ConsoleLogger.severe("Could not write data to file '" + file + "':", ex);
        }
    }

    /**
     * Creates the given file if it doesn't exist.
     *
     * @param file The file to create if necessary.
     * @return The given file (allows inline use).
     * @throws IOException if file could not be created
     */
    public static File createFileIfNotExists(File file) throws IOException {
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                Files.createDirectories(file.getParentFile().toPath());
            }

            Files.createFile(file.toPath());
        }
        return file;
    }
}
