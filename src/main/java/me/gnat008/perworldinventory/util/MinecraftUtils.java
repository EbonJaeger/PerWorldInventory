/**
 * NexusInventory is a multi-world inventory plugin.
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

package me.gnat008.perworldinventory.Util;

import org.bukkit.Bukkit;

public class MinecraftUtils {
    public static String getMinecraftVersion() {
        // Get the raw version
        final String rawVersion = Bukkit.getVersion();

        // Get the start of the raw string
        int start = rawVersion.indexOf("MC:");
        if(start == -1)
            return rawVersion;

        // Exclude the 'MC:'
        start += 4;

        // Get the end of the string
        int end = rawVersion.indexOf(')', start);

        // Get and return the Minecraft version number
        return rawVersion.substring(start, end);
    }
}
