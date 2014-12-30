package com.kill3rtaco.tacoserialization;

import org.bukkit.Location;
import org.bukkit.entity.Ocelot;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to help with the serialization of Ocelots. 
 * <br/><br/>
 * This serialization class supports optional serialization.<br/>
 * TacoSerialization will create a folder in your server plugins directory (wherever that may be) called
 * 'TacoSerialization'. Inside the folder will be a config.yml file. Various values can be turned off to
 * prevent some keys from being generated.
 * @author KILL3RTACO
 * @since 1.0
 *
 */
public class OcelotSerialization {
	
	protected OcelotSerialization() {
	}
	
	/**
	 * Serialize an Ocelot into a JSONObject
	 * @param ocelot The ocelot to serialize
	 * @return The serialize Ocelot, in the form of a JSONObject
	 */
	public static JSONObject serializeOcelot(Ocelot ocelot) {
		try {
			JSONObject root = LivingEntitySerialization.serializeEntity(ocelot);
			if(shouldSerialize("type"))
				root.put("type", ocelot.getCatType().name());
			return root;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serialize an Ocelot as a String
	 * @param ocelot The Ocelot to serialize
	 * @return The serialization string
	 */
	public static String serializeOcelotAsString(Ocelot ocelot) {
		return serializeOcelotAsString(ocelot, false);
	}
	
	/**
	 * Serialize an Ocelot as a String
	 * @param ocelot The Ocelot to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @return The serialization string
	 */
	public static String serializeOcelotAsString(Ocelot ocelot, boolean pretty) {
		return serializeOcelotAsString(ocelot, pretty, 5);
	}
	
	/**
	 * Serialize an Ocelot as a String
	 * @param ocelot The Ocelot to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @param indentFactor The amount of spaces in a tab
	 * @return The serialization string
	 */
	public static String serializeOcelotAsString(Ocelot ocelot, boolean pretty, int indentFactor) {
		try {
			if(pretty) {
				return serializeOcelot(ocelot).toString(indentFactor);
			} else {
				return serializeOcelot(ocelot).toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Spawn a ocelot in a desired location desired stats
	 * @param location Where to spawn the ocelot
	 * @param stats The desired stats
	 * @return The spawned Ocelot
	 */
	public static Ocelot spawnOcelot(Location location, String stats) {
		try {
			return spawnOcelot(location, new JSONObject(stats));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Spawn a ocelot in a desired location desired stats
	 * @param location Where to spawn the ocelot
	 * @param stats The desired stats
	 * @return The spawned Ocelot
	 */
	public static Ocelot spawnOcelot(Location location, JSONObject stats) {
		try {
			Ocelot ocelot = (Ocelot) LivingEntitySerialization.spawnEntity(location, stats);
			if(stats.has("type"))
				ocelot.setCatType(Ocelot.Type.valueOf(stats.getString("type")));
			return ocelot;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Test if a certain key should be serialized
	 * @param key The key to test
	 * @return Whether the key should be serilaized or not
	 */
	public static boolean shouldSerialize(String key) {
		return SerializationConfig.getShouldSerialize("ocelot." + key);
	}
	
}
