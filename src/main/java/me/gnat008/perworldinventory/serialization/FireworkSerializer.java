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

import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FireworkSerializer {
    
    protected FireworkSerializer() {}

    /**
     * Serialize FireworkMeta into a JSON Object.
     *
     * @param meta The FireworkMeta to serialize
     * @return The JSON representation of the meta
     */
    public static JSONObject serializeFireworkMeta(FireworkMeta meta) {
        try {
            JSONObject root = new JSONObject();
            root.put("power", meta.getPower());

            JSONArray effects = new JSONArray();
            for (FireworkEffect effect : meta.getEffects()) {
                effects.put(FireworkEffectSerializer.serializeFireworkEffect((FireworkEffect) effect));
            }
            root.put("effects", effects);
            
            return root;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Deserialize FireworkMeta from a JSON Object
     *
     * @param json The JSON Object to deserialize
     * @return The FireworkMeta
     */
    public static FireworkMeta deserializeFireworkMeta(JSONObject json) {
        try {
            FireworkMeta dummy = (FireworkMeta) new ItemStack(Material.FIREWORK, 1);
            
            dummy.setPower(json.optInt("power"));
            
            JSONArray effects = json.getJSONArray("effects");
            for (int i = 0; i < effects.length(); i++) {
                JSONObject effectDto = effects.getJSONObject(i);
                FireworkEffect effect = FireworkEffectSerializer.deserializeFireworkEffect(effectDto);
                
                if (effect != null)
                    dummy.addEffect(effect);
            }
            
            return dummy;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
