/**
 * PerWorldInventory is a multi-world inventory plugin.
 * Copyright (C) 2014 - 2015 Gnat008
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.gnat008.perworldinventory.Updater;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.Util.ChatColor;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class SpigotUpdater
{
    private PerWorldInventory plugin;
    final int resource = 4482;
    private static String latestVersion = "";
    private static boolean updateAvailable = false;

    public SpigotUpdater(PerWorldInventory var1)
    {
        this.plugin = var1;
    }

    private String getSpigotVersion()
    {
        try
        {
            HttpURLConnection var1 = (HttpURLConnection)new URL("http://www.spigotmc.org/api/general.php").openConnection();
            var1.setDoOutput(true);
            var1.setRequestMethod("POST");
            var1.getOutputStream().write("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=4482".getBytes("UTF-8"));
            String var2 = new BufferedReader(new InputStreamReader(var1.getInputStream())).readLine();
            if (var2.length() <= 7) {
                return var2;
            }
        }
        catch (Exception var3)
        {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "----------------------------");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "      PerWorldInventory Updater");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + " ");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " Could not connect to spigotmc.org ");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + " to check for updates! ");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + " ");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "----------------------------");
        }
        return null;
    }

    private boolean checkHigher(String var1, String var2)
    {
        String var3 = toReadable(var1);
        String var4 = toReadable(var2);
        return var3.compareTo(var4) < 0;
    }

    public boolean checkUpdates()
    {
        if (getHighest() != "") {
            return true;
        }
        String var1 = getSpigotVersion();
        if ((var1 != null) && (checkHigher(this.plugin.getDescription().getVersion(), var1)))
        {
            latestVersion = var1;
            updateAvailable = true;
            return true;
        }
        return false;
    }

    public static boolean updateAvailable()
    {
        return updateAvailable;
    }

    public static String getHighest()
    {
        return latestVersion;
    }

    private String toReadable(String var1)
    {
        String[] var2 = Pattern.compile(".", 16).split(var1.replace("v", ""));
        var1 = "";
        String[] var6 = var2;
        int var5 = var2.length;
        for (int var4 = 0; var4 < var5; var4++)
        {
            String var3 = var6[var4];
            var1 = var1 + String.format("%4s", new Object[] { var3 });
        }
        return var1;
    }
}
