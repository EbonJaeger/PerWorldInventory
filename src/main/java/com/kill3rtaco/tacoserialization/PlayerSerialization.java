package com.kill3rtaco.tacoserialization;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.defaults.ConfigValues;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to help with the serialization of Players.
 * <br/><br/>
 * This serialization class supports optional serialization.<br/>
 * TacoSerialization will create a folder in your server plugins directory (wherever that may be) called
 * 'TacoSerialization'. Inside the folder will be a config.yml file. Various values can be turned off to
 * prevent some keys from being generated.
 *
 * @author KILL3RTACO
 * @since 1.0
 */
public class PlayerSerialization {

    protected PlayerSerialization() {
    }

    /**
     * Serialize a Player into a JSONObject. The player's EnderChest, inventory (including armor) and stats
     * such as experience and potion effects will be saved unless disabled.
     *
     * @param player The player to serialize
     * @return The serialized stats
     */
    public static JSONObject serializePlayer(PWIPlayer player, PerWorldInventory plugin) {
        try {
            JSONObject root = new JSONObject();
            // Formats: 0 == old serialization, 1 == new serialization
            root.put("data-format", 1);
            if (ConfigValues.ENDER_CHEST.getBoolean())
                root.put("ender-chest", InventorySerialization.serializeInventory(player.getEnderChest()));
            if (ConfigValues.INVENTORY.getBoolean())
                root.put("inventory", InventorySerialization.serializePlayerInventory(player));
            if (ConfigValues.STATS.getBoolean())
                root.put("stats", PlayerStatsSerialization.serializePlayerStats(player));
            if (ConfigValues.ECONOMY.getBoolean())
                root.put("economy", EconomySerialization.serializeEconomy(player, plugin.getEconomy()));
            return root;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Set a player's meta information with desired stats
     *
     * @param meta   The stats to set
     * @param player The affected player
     */
    public static void setPlayer(JSONObject meta, Player player, PerWorldInventory plugin) {
        try {
            // Formats: 0 == TacoSerialization, 1 == Base64 serialization
            int format = 0;
            if (meta.has("data-format"))
                format = meta.getInt("data-format");

            if (ConfigValues.ENDER_CHEST.getBoolean() && meta.has("ender-chest"))
                InventorySerialization.setInventory(player.getEnderChest(), meta.getJSONArray("ender-chest"), format);
            if (ConfigValues.INVENTORY.getBoolean() && meta.has("inventory"))
                InventorySerialization.setPlayerInventory(player, meta.getJSONObject("inventory"), format);
            if (ConfigValues.STATS.getBoolean() && meta.has("stats"))
                PlayerStatsSerialization.applyPlayerStats(player, meta.getJSONObject("stats"));
            if (ConfigValues.ECONOMY.getBoolean() && meta.has("economy"))
                EconomySerialization.setEconomy(plugin.getEconomy(), meta.getJSONObject("economy"), player);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
