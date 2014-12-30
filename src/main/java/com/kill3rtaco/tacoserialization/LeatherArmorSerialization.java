package com.kill3rtaco.tacoserialization;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to help with the serialization of dyed leather armor. The Red, Green, and Blue values are saved
 * appropriately.
 * @author KILL3RTACO
 * @since 1.0
 *
 */
public class LeatherArmorSerialization {
	
	protected LeatherArmorSerialization() {
	}
	
	/**
	 * Serialize LeatherArmorMeta, saving the Color's rgb value.
	 * @param meta The LeatherArmorMeta to serialize
	 * @return The serialized meta information
	 */
	public static JSONObject serializeArmor(LeatherArmorMeta meta) {
		try {
			JSONObject root = new JSONObject();
			root.put("color", ColorSerialization.serializeColor(meta.getColor()));
			return root;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serializes the LeatherArmorMeta using serializeArmor() and returns the String form.
	 * @param meta The LeatherArmorMeta to serialized
	 * @return The serialization string
	 */
	public static String serializeArmorAsString(LeatherArmorMeta meta) {
		return serializeArmorAsString(meta, false);
	}
	
	/**
	 * Serializes the LeatherArmorMeta using serializeArmor() and returns the String form.
	 * @param meta The LeatherArmorMeta to serialized
	 * @param pretty Whether the resulting String should be 'pretty' or not
	 * @return The serialization string
	 */
	public static String serializeArmorAsString(LeatherArmorMeta meta, boolean pretty) {
		return serializeArmorAsString(meta, pretty, 5);
	}
	
	/**
	 * Serializes the LeatherArmorMeta using serializeArmor() and returns the String form.
	 * @param meta The LeatherArmorMeta to serialized
	 * @param pretty Whether the resulting String should be 'pretty' or not
	 * @param indentFactor the amount of spaces in a tab
	 * @return The serialization string
	 */
	public static String serializeArmorAsString(LeatherArmorMeta meta, boolean pretty, int indentFactor) {
		try {
			if(pretty) {
				return serializeArmor(meta).toString(indentFactor);
			} else {
				return serializeArmor(meta).toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gets LeatherArmorMeta from the a JSONObject constructed from the given String
	 * @param json The String to use
	 * @return LeatherArmorMeta taken from a JSONObject constructed from the given String
	 */
	public static LeatherArmorMeta getLeatherArmorMeta(String json) {
		try {
			return getLeatherArmorMeta(new JSONObject(json));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gets LeatherArmorMeta from the given JSONObject
	 * @param json The JSONObject to decode
	 * @return LeatherArmorMeta taken from the given JSONObject as a reference
	 */
	public static LeatherArmorMeta getLeatherArmorMeta(JSONObject json) {
		try {
			ItemStack dummyItems = new ItemStack(Material.LEATHER_HELMET, 1);
			LeatherArmorMeta meta = (LeatherArmorMeta) dummyItems.getItemMeta();
			if(json.has("color")) {
				meta.setColor(ColorSerialization.getColor(json.getJSONObject("color")));
			}
			return meta;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
