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
import org.json.JSONException;
import org.json.JSONObject;

public class ColorSerializer {
    
    protected ColorSerializer() {}

    /**
     * Serialize colors into a JSON Object form.
     * Colors are serialized as red, green, blue 
     *  
     * @param color The color to serialize
     * @return The serialized color
     */
    public static JSONObject serializeColor(Color color) {
        try {
            JSONObject root = new JSONObject();
            root.put("red", color.getRed());
            root.put("green", color.getGreen());
            root.put("blue", color.getBlue());
            
            return root;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get a color from a JSON object. If any of the red, green, or blue keys
     * are not found, they will default to 0.
     *  
     * @param json The JSON to get the color from
     * @return The color
     */
    public static Color deserializeColor(JSONObject json) {
        try {
            int red = 0, green = 0, blue = 0;
            
            if (json.has("red"))
                red = json.getInt("red");
            if (json.has("green"))
                green = json.getInt("green");
            if (json.has("blue"))
                blue = json.getInt("blue");
            
            return Color.fromRGB(red, green, blue);
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
