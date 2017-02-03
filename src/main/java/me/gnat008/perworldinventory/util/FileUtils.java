package me.gnat008.perworldinventory.util;

import java.io.*;

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
}
