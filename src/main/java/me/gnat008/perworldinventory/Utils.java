package me.gnat008.perworldinventory;

import java.io.*;

/**
 * Class that holds utility methods.
 */
public final class Utils {

    /**
     * Check if a server's version is the same as a given version
     * or higher.
     *
     * @param version The server's version.
     * @param major The major version number.
     * @param minor The minor version number.
     * @param patch The patch version number.
     *
     * @return True if the server is running the same version or newer.
     */
    public static boolean checkServerVersion(String version, int major, int minor, int patch) {
        String versionNum = version.substring(version.indexOf(".") - 1, version.length() - 1).trim();
        String[] parts = versionNum.split("\\.");

        try {
            if ((Integer.parseInt(parts[0]) >= major)) {
                if (Integer.parseInt(parts[1]) == minor) {
                    if (parts.length == 2) {
                        return true;
                    } else {
                        return Integer.parseInt(parts[2]) >= patch;
                    }
                } else {
                    return Integer.parseInt(parts[1]) > minor;
                }
            }
        } catch (NumberFormatException ex) {
            return false;
        }

        return false;
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
     * Get the available core count of the JVM.
     *
     * @return Number of cores.
     */
    public static int getCoreCount() {
        return Runtime.getRuntime().availableProcessors();
    }
}
