/*
 * Copyright (C) 2014-2015  Gnat008
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

package me.gnat008.perworldinventory.serialization;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.JSONException;
import org.json.JSONObject;

public class SkullSerializer {
    
    protected SkullSerializer() {}

    /**
     * Serialize SkullMeta into a JSON Object.
     *
     * @param meta The SkullMeta to serialize
     * @return The JSON representation of the SkullMeta
     */
    public static JSONObject serializeSkull(SkullMeta meta) {
        try {
            JSONObject root = new JSONObject();
            
            if (meta.hasOwner())
                root.put("owner", meta.getOwner());
            
            return root;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get SkullMeta from a JSON Object.
     *
     * @param json The JSON Object to deserialize
     * @return The SkullMeta
     */
    public static SkullMeta deserializeSkullMeta(JSONObject json) {
        try {
            ItemStack dummy = new ItemStack(Material.SKULL_ITEM, 1);
            SkullMeta meta = (SkullMeta) dummy.getItemMeta();
            
            if (json.has("owner"))
                meta.setOwner(json.getString("owner"));
            
            return meta;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
