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

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FireworkEffectSerializer {
    
    protected FireworkEffectSerializer() {}

    /**
     * Serialize a FireworkEffect into a JSON Object.
     *  
     * @param effect The effect to serialize
     * @return The JSON representation of the FireworkEffect
     */
    public static JSONObject serializeFireworkEffect(FireworkEffect effect) {
        try {
            JSONObject root = new JSONObject();
            
            // colors
            JSONArray colors = new JSONArray();
            for (Color c : effect.getColors()) {
                colors.put(ColorSerializer.serializeColor(c));
            }
            root.put("colors", colors);
            
            // fade colors
            JSONArray fadeColors = new JSONArray();
            for (Color c : effect.getFadeColors()) {
                fadeColors.put(ColorSerializer.serializeColor(c));
            }
            root.put("fade-colors", fadeColors);
            
            // hasFlicker
            root.put("flicker", effect.hasFlicker());
            
            // hasTrail
            root.put("trail", effect.hasTrail());
            
            // type
            root.put("type", effect.getType());
            
            return root;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get a FireworkEffect from a JSON Object.
     *
     * @param json The JSON Object to deserialize
     * @return The FireworkEffect
     */
    public static FireworkEffect deserializeFireworkEffect(JSONObject json) {
        try {
            FireworkEffect.Builder builder = FireworkEffect.builder();
            
            // colors
            JSONArray colors = json.getJSONArray("colors");
            for (int i = 0; i < colors.length(); i++) {
                builder.withColor(ColorSerializer.deserializeColor(colors.getJSONObject(i)));
            }
            
            // fade
            JSONArray fadeColors = json.getJSONArray("fade-colors");
            for (int i = 0; i < fadeColors.length(); i++) {
                builder.withFade(ColorSerializer.deserializeColor(fadeColors.getJSONObject(i)));
            }
            
            // flicker
            if (json.getBoolean("flicker"))
                builder.withFlicker();
            
            // trail
            if (json.getBoolean("trail"))
                builder.withTrail();
            
            // type
            builder.with(FireworkEffect.Type.valueOf(json.getString("type")));
            
            return builder.build();
        } catch (IllegalArgumentException | JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
