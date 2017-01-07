/*
 * Copyright (C) 2014-2016  EbonJaguar
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

package me.gnat008.perworldinventory.data.serializers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationSerializer {

    private LocationSerializer() {}

    /**
     * Serialize a Location into a JsonObject.
     *
     * @param location The {@link org.bukkit.Location}
     * @return The JsonObject in String form
     */
    public static JsonObject serialize(Location location) {
        JsonObject root = new JsonObject();

        root.addProperty("world", location.getWorld().getName());
        root.addProperty("x", location.getX());
        root.addProperty("y", location.getY());
        root.addProperty("z", location.getZ());
        root.addProperty("pitch", location.getPitch());
        root.addProperty("yaw", location.getYaw());
        return root;
    }
    /**
     * Serialize a Location into a JsonObject.
     *
     * @param location The {@link org.bukkit.Location}
     * @return The JsonObject in String form
     */
    public static String serializeAsString(Location location) {
        Gson gson = new Gson();
        return gson.toJson(serialize(location));
    }

    /**
     * Deserialize a location from a given JsonObject.
     *
     * @param loc The JsonObject to deserialize
     * @return The Location
     */
    public static Location deserialize(JsonObject loc) {
        String worldName = loc.get("world").getAsString();
        World world = Bukkit.getWorld(worldName);
        double x = loc.get("x").getAsDouble();
        double y = loc.get("y").getAsDouble();
        double z = loc.get("z").getAsDouble();
        float pitch = loc.get("pitch").getAsFloat();
        float yaw = loc.get("yaw").getAsFloat();

        return new Location(world, x, y, z, yaw, pitch);
    }
}
