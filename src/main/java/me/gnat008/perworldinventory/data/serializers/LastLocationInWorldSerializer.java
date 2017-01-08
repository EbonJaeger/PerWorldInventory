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
import com.google.gson.JsonElement;
import org.bukkit.Location;

import java.util.Map;
import java.util.HashMap;

public class LastLocationInWorldSerializer {

    private LastLocationInWorldSerializer() {}

    /**
     * Serialize the last location of a player in worlds into a JsonObject.
     *
     * @param locationInWorld The map of the players locations in world.
     * @return The JsonObject
     */
    public static JsonObject serialize(Map<String, Location> locationInWorld) {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, Location> entry : locationInWorld.entrySet()) {
            root.add(entry.getKey(), LocationSerializer.serialize(entry.getValue()));
        }
        return root;
    }

    /**
     * Serialize the last location of a player in worlds into a json-string.
     *
     * @param locationInWorld The map of the players locations in world.
     * @return The string representation of the json object.
     */
    public static String serializeAsString(Map<String, Location> locationInWorld) {
        Gson gson = new Gson();
        return gson.toJson(serialize(locationInWorld));
    }

    /**
     * Deserialize the last location of a player in worlds from a given JsonObject.
     *
     * @param locationInWorld The JsonObject to deserialize
     * @return A map representing the last location of the player in worlds.
     */
    public static Map<String, Location> deserialize(JsonObject locationInWorld) {
        Map<String, Location> map = new HashMap<String, Location>();
        for (Map.Entry<String, JsonElement> entry : locationInWorld.entrySet()) {
            map.put(entry.getKey(), LocationSerializer.deserialize(entry.getValue().getAsJsonObject()));
        }
        return map;
    }
}
