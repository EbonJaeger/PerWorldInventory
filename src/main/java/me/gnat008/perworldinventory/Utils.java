package me.gnat008.perworldinventory;

import java.io.*;

/**
 * Class that holds utility methods.
 */
public final class Utils {

    public static boolean checkServerVersion(String version) {
        String versionNum = version.substring(version.indexOf(".") - 1, version.length() - 1).trim();
        String[] parts = versionNum.split("\\.");

        try {
            if ((Integer.parseInt(parts[0]) >= 1)) {
                if (Integer.parseInt(parts[1]) == 9) {
                    return !(parts.length < 3) && (Integer.parseInt(parts[2]) >= 2);
                } else if (Integer.parseInt(parts[1]) >= 10) {
                    return true;
                }
            }
        } catch (NumberFormatException ex) {
            return false;
        }

        return false;
    }

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
