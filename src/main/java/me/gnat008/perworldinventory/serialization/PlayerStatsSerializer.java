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

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;

public class PlayerStatsSerializer {
    
    protected PlayerStatsSerializer() {}

    /**
     * Serialize a Player's stats into a JSON Object.
     *
     * @param player The Player whose stats to serialize
     * @return The serialized stats
     */
    public static JSONObject serializePlayerStats(Player player) {
        try {
            JSONObject root = new JSONObject();
            
            root.put("can-fly", player.getAllowFlight());
            root.put("display-name", player.getDisplayName());
            root.put("exhaustion", player.getExhaustion());
            root.put("exp", player.getExp());
            root.put("flying", player.isFlying());
            root.put("food", player.getFoodLevel());
            root.put("gamemode", player.getGameMode().ordinal());
            root.put("level", player.getLevel());
            root.put("potion-effects", PotionEffectSerializer.serializePotionEffects(player.getActivePotionEffects()));
            root.put("saturation", player.getSaturation());
            
            return root;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Deserialize a Player's stats and apply them.
     *
     * @param json The serialized stats
     * @param player The player to apply the stats to
     */
    public static void deserializePlayerStats(JSONObject json, Player player) {
        try {
            if (json.has("can-fly"))
                player.setAllowFlight(json.getBoolean("can-fly"));
            if (json.has("display-name"))
                player.setDisplayName(json.getString("display-name"));
            if (json.has("exhaustion"))
                player.setExhaustion((float) json.getDouble("exhaustion"));
            if (json.has("exp"))
                player.setExp((float) json.getDouble("exp"));
            if (json.has("flying"))
                player.setFlying(json.getBoolean("flying"));
            if (json.has("food"))
                player.setFoodLevel(json.getInt("food"));
            if (json.has("gamemode"))
                player.setGameMode(GameMode.getByValue(json.getInt("gamemode")));
            if (json.has("level"))
                player.setLevel(json.getInt("level"));
            if (json.has("potion-effects"))
                PotionEffectSerializer.setPotionEffects(json.getString("potion-effects"), player);
            if (json.has("saturation"))
                player.setSaturation((float) json.getDouble("saturation"));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
}
