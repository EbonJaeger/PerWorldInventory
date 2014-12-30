package com.kill3rtaco.tacoserialization;

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
 * @author KILL3RTACO
 * @since 1.0
 *
 */
public class PlayerSerialization {
	
	protected PlayerSerialization() {
	}
	
	/**
	 * Serialize a Player into a JSONObject. The player's EnderChest, inventory (including armor) and stats
	 * such as experience and potion effects will be saved unless disabled.
	 * @param player
	 * @return The serialized stats
	 */
	public static JSONObject serializePlayer(Player player) {
		try {
			JSONObject root = new JSONObject();
			if(SerializationConfig.getShouldSerialize("player-ender-chest"))
				root.put("ender-chest", InventorySerialization.serializeInventory(player.getEnderChest()));
			if(SerializationConfig.getShouldSerialize("player.inventory"))
				root.put("inventory", InventorySerialization.serializePlayerInventory(player.getInventory()));
			if(SerializationConfig.getShouldSerialize("player.stats"))
				root.put("stats", PlayerStatsSerialization.serializePlayerStats(player));
			return root;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serialize a player as a String
	 * @param player The player to serialize
	 * @return The serialization string
	 */
	public static String serializePlayerAsString(Player player) {
		return serializePlayerAsString(player, false);
	}
	
	/**
	 * Serialize a player as a String
	 * @param player The player to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @return The serialization string
	 */
	public static String serializePlayerAsString(Player player, boolean pretty) {
		return serializePlayerAsString(player, pretty, 5);
	}
	
	/**
	 * Serialize a player as a String
	 * @param player The player to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @param indentFactor The amount of spaces in a tab
	 * @return The serialization string
	 */
	public static String serializePlayerAsString(Player player, boolean pretty, int indentFactor) {
		try {
			if(pretty) {
				return serializePlayer(player).toString(indentFactor);
			} else {
				return serializePlayer(player).toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Set a player's meta information with desired stats
	 * @param meta The stats to set
	 * @param player The affected player
	 */
	public static void setPlayer(String meta, Player player) {
		try {
			setPlayer(new JSONObject(meta), player);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Set a player's meta information with desired stats
	 * @param meta The stats to set
	 * @param player The affected player
	 */
	public static void setPlayer(JSONObject meta, Player player) {
		try {
			if(meta.has("ender-chest"))
				InventorySerialization.setInventory(player.getEnderChest(), meta.getJSONArray("ender-chest"));
			if(meta.has("inventory"))
				InventorySerialization.setPlayerInventory(player, meta.getJSONObject("inventory"));
			if(meta.has("stats"))
				PlayerStatsSerialization.applyPlayerStats(player, meta.getJSONObject("stats"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test if a certain key should be serialized
	 * @param key The key to test
	 * @return Whether the key should be serilaized or not
	 */
	public static boolean shouldSerialize(String key) {
		return SerializationConfig.getShouldSerialize("player." + key);
	}
	
}
