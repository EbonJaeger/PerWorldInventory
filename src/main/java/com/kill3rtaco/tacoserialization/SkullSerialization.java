package com.kill3rtaco.tacoserialization;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to help with the serialization of Skulls.
 * @author KILL3RTACO
 * @since 1.1
 *
 */
public class SkullSerialization {
	
	protected SkullSerialization() {
	}
	
	/**
	 * Serialize a skull into a JSONObject.
	 * @param meta The SkullMeta to serialize
	 * @return The serialized SkullMeta
	 */
	public static JSONObject serializeSkull(SkullMeta meta) {
		//only one value is saved, I know. But just in case Mojang/Bukkit decides
		//to do anything further with skulls, causing it to add to the meta.
		try {
			JSONObject root = new JSONObject();
			if(meta.hasOwner())
				root.put("owner", meta.getOwner());
			return root;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serialize a skull as a String.
	 * @param meta The SkullMeta to serialize
	 * @return The serialization string
	 */
	public static String serializeSkullAsString(SkullMeta meta) {
		return serializeSkullAsString(meta, false);
	}
	
	/**
	 * Serialize a skull as a String.
	 * @param meta The SkullMeta to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @return The serialization string
	 */
	public static String serializeSkullAsString(SkullMeta meta, boolean pretty) {
		return serializeSkullAsString(meta, pretty, 5);
	}
	
	/**
	 * Serialize a skull as a String.
	 * @param meta The SkullMeta to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @param indentFactor The amount of spaces in a tab
	 * @return The serialization string
	 */
	public static String serializeSkullAsString(SkullMeta meta, boolean pretty, int indentFactor) {
		try {
			if(pretty) {
				return serializeSkull(meta).toString(indentFactor);
			} else {
				return serializeSkull(meta).toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get SkullMeta from a String that will be constructed into a JSONObject.
	 * @param meta The meta information of the skull
	 * @return The SkullMeta
	 */
	public static SkullMeta getSkullMeta(String meta) {
		try {
			return getSkullMeta(new JSONObject(meta));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get SkullMeta from a JSONObject.
	 * @param meta The meta information of the skull
	 * @return The SkullMeta
	 */
	public static SkullMeta getSkullMeta(JSONObject meta) {
		try {
			ItemStack dummyItems = new ItemStack(Material.SKULL_ITEM);
			SkullMeta dummyMeta = (SkullMeta) dummyItems.getItemMeta();
			if(meta.has("owner"))
				dummyMeta.setOwner(meta.getString("owner"));
			return dummyMeta;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
