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
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.defaults.ConfigValues;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import org.bukkit.entity.Player;

public class PlayerSerializer {

    protected PlayerSerializer() {}

    /**
     * Serialize a Player into a JsonObject. The player's EnderChest, inventory (including armor) and stats
     * such as experience and potion effects will be saved unless disabled.
     *
     * @param player The player to serialize
     * @return The serialized stats
     */
    public static String serialize(PerWorldInventory plugin, PWIPlayer player) {
        Gson gson = new Gson();
        JsonObject root = new JsonObject();

        // Formats: 0 == old serialization, 1 == new serialization
        root.addProperty("data-format", 1);
        root.add("ender-chest", InventorySerializer.serializeInventory(player.getEnderChest()));
        root.add("inventory", InventorySerializer.serializePlayerInventory(player));
        root.add("stats", StatSerializer.serialize(player));

        if (ConfigValues.ECONOMY.getBoolean())
            root.add("economy", EconomySerializer.serialize(player, plugin.getEconomy()));

        return gson.toJson(root);
    }

    /**
     * Set a player's meta information with desired stats
     *
     * @param data   The stats to set
     * @param player The affected player
     */
    public static void deserialize(JsonObject data, Player player, PerWorldInventory plugin) {
        // Formats: 0 == TacoSerialization, 1 == Base64 serialization
        int format = 0;
        if (data.has("data-format"))
            format = data.get("data-format").getAsInt();

        if (ConfigValues.ENDER_CHEST.getBoolean() && data.has("ender-chest"))
            player.getEnderChest().setContents(InventorySerializer.deserializeInventory(data.getAsJsonArray("ender-chest"),
                    player.getEnderChest().getSize(), format));
        if (ConfigValues.INVENTORY.getBoolean() && data.has("inventory"))
            InventorySerializer.setInventory(player, data.getAsJsonObject("inventory"), format);
        if (ConfigValues.STATS.getBoolean() && data.has("stats"))
            StatSerializer.deserialize(player, data.getAsJsonObject("stats"));
        if (ConfigValues.ECONOMY.getBoolean() && data.has("economy"))
            EconomySerializer.deserialize(plugin.getEconomy(), data.getAsJsonObject("economy"), player);
    }
}
