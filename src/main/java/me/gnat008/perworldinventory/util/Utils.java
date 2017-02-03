package me.gnat008.perworldinventory.util;

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

}
