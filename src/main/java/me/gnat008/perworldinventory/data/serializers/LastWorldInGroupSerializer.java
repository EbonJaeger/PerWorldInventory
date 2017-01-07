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

import java.util.Map;
import java.util.HashMap;

public class LastWorldInGroupSerializer {

    private LastWorldInGroupSerializer() {}

    /**
     * Serialize a Location into a JsonObject.
     *
     * @param location The {@link org.bukkit.Location}
     * @return The JsonObject
     */
    public static JsonObject serialize(Map<String, String> worldInGroup) {
        JsonObject root = new JsonObject();
        for(Map.Entry<String, String> entry : worldInGroup.entrySet()) {
            root.addProperty(entry.getKey(), entry.getValue());
        }
        return root;
    }

    /**
     * Deserialize a location from a given JsonObject.
     *
     * @param loc The JsonObject to deserialize
     * @return The Location
     */
    public static Map<String, String> deserialize(JsonObject worldInGroup) {
        Map<String, String> map = new HashMap<String, String>();
        for(Map.Entry<String, JsonElement> entry : worldInGroup.entrySet()) {
            map.put(entry.getKey(),entry.getValue().getAsString());
        }
        return map;
    }
}
