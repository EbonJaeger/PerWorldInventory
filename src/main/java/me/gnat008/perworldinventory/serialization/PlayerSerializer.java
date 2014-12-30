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

import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;

public class PlayerSerializer {
    
    protected PlayerSerializer() {}

    /**
     * Serialize a Player, including their EnderChest contents, inventory contents, and their stats.
     *
     * @param player The player to serialize
     * @return The JSON representation of the Player, or null
     */
    public static JSONObject serializePlayer(Player player) {
        try {
            JSONObject root = new JSONObject();
            
            root.put("ender-chest", InventorySerializer.serializeInventory(player.getEnderChest()));
            root.put("inventory", InventorySerializer.serializeInventory(player.getInventory()));
            root.put("stats", PlayerStatsSerializer.serializePlayerStats(player));
            
            return root;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Deserialize a Player, and apply inventories and stats.
     *
     * @param json The serialized Player
     * @param player The player to apply stuff to
     */
    public static void deserializePlayer(JSONObject json, Player player) {
        try {
            if (json.has("ender-chest"))
                InventorySerializer.setInventory(player.getEnderChest(), json.getJSONArray("ender-chest"));
            if (json.has("inventory"))
                InventorySerializer.setPlayerInventory(player, json.getJSONObject("inventory"));
            if (json.has("stats"))
                PlayerStatsSerializer.deserializePlayerStats(json.getJSONObject("stats"), player);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
}
