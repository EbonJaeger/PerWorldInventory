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
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.json.JSONException;
import org.json.JSONObject;

public class LeatherArmorSerializer {
    
    protected LeatherArmorSerializer() {}

    /**
     * Serializes LeatherArmorMeta, saving the RGB color values.
     *
     * @param meta The LeatherArmorMeta to serialize
     * @return The JSON representation of the meta
     */
    public static JSONObject serializeArmor(LeatherArmorMeta meta) {
        try {
            JSONObject root = new JSONObject();
            
            root.put("color", ColorSerializer.serializeColor(meta.getColor()));
            
            return root;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Deserialize LeatherArmorMeta from a JSON Object.
     *
     * @param json The JSON Object to deserialize
     * @return The LeatherArmorMeta
     */
    public static LeatherArmorMeta deserializeArmor(JSONObject json) {
        try {
            ItemStack dummy = new ItemStack(Material.LEATHER_HELMET, 1);
            LeatherArmorMeta meta = (LeatherArmorMeta) dummy.getItemMeta();
            
            if (json.has("color"))
                meta.setColor(ColorSerializer.deserializeColor(json.getJSONObject("color")));
            
            return meta;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
