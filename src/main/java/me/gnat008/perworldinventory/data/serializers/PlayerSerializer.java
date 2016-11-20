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
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import javax.inject.Inject;

public class PlayerSerializer {

    @Inject
    private InventorySerializer inventorySerializer;
    @Inject
    private Settings settings;
    @Inject
    private StatSerializer statSerializer;
    @Inject
    private PerWorldInventory plugin;

    PlayerSerializer() {}

    /**
     * Serialize a Player into a JsonObject. The player's EnderChest, inventory (including armor) and stats
     * such as experience and potion effects will be saved unless disabled. A data format number is included
     * to tell which methods to use for some serializations/deserializations.
     * <p>
     *     Formats:
     *     0: Deserialize items with the old TacoSerialization methods
     *     1: Deserialize items with Base64
     *     2: Serialize/Deserialize PotionEffects as JsonObjects
     * </p>
     *
     * @param player The player to serialize.
     * @return The serialized stats.
     */
    public String serialize(PWIPlayer player) {
        Gson gson = new Gson();
        JsonObject root = new JsonObject();

        root.addProperty("data-format", 2);
        root.add("ender-chest", inventorySerializer.serializeInventory(player.getEnderChest()));
        root.add("inventory", inventorySerializer.serializePlayerInventory(player));
        root.add("stats", StatSerializer.serialize(player));

        if (settings.getProperty(PwiProperties.USE_ECONOMY))
            root.add("economy", EconomySerializer.serialize(player, plugin.getEconomy()));

        return gson.toJson(root);
    }

    /**
     * Deserialize all aspects of a player, and apply their data. See {@link PlayerSerializer#serialize(PWIPlayer)}
     * for an explanation of the data format number.
     *
     * @param data   The saved player information.
     * @param player The Player to apply the deserialized information to.
     */
    public void deserialize(final JsonObject data, final Player player) {
        int format = 0;
        if (data.has("data-format"))
            format = data.get("data-format").getAsInt();

        if (settings.getProperty(PwiProperties.LOAD_ENDER_CHESTS) && data.has("ender-chest"))
            player.getEnderChest().setContents(inventorySerializer.deserializeInventory(data.getAsJsonArray("ender-chest"),
                    player.getEnderChest().getSize(), format));
        if (settings.getProperty(PwiProperties.LOAD_INVENTORY) && data.has("inventory"))
            inventorySerializer.setInventory(player, data.getAsJsonObject("inventory"), format);
        if (data.has("stats"))
            statSerializer.deserialize(player, data.getAsJsonObject("stats"), format);
        if (settings.getProperty(PwiProperties.USE_ECONOMY)) {
            Economy econ = plugin.getEconomy();
            if (econ == null) {
                PwiLogger.warning("Economy saving is turned on, but no economy found!");
                return;
            }

            PwiLogger.debug("[ECON] Withdrawing " + econ.getBalance(player) + " from '" + player.getName() + "'!");
            econ.withdrawPlayer(player, econ.getBalance(player));
            econ.bankWithdraw(player.getName(), econ.bankBalance(player.getName()).amount);
            if (data.has("economy")) {
                EconomySerializer.deserialize(econ, data.getAsJsonObject("economy"), player);
            }
        }
    }
}
