package com.kill3rtaco.tacoserialization;

import org.bukkit.Location;
import org.bukkit.entity.Horse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to help with the serialization of Horses.
 * <br/><br/>
 * This serialization class supports optional serialization.<br/>
 * TacoSerialization will create a folder in your server plugins directory (wherever that may be) called
 * 'TacoSerialization'. Inside the folder will be a config.yml file. Various values can be turned off to
 * prevent some keys from being generated.
 * @author KILL3RTACO
 * @since 1.0
 *
 */
public class HorseSerialization {
	
	protected HorseSerialization() {
	}
	
	/**
	 * Serialize a Horse into a JSONObject.
	 * @param horse The Horse to serialize
	 * @return The serialized Horse
	 */
	public static JSONObject serializeHorse(Horse horse) {
		try {
			JSONObject root = LivingEntitySerialization.serializeEntity(horse);
			if(shouldSerialize("color"))
				root.put("color", horse.getColor().name());
			if(shouldSerialize("inventory"))
				root.put("inventory", InventorySerialization.serializeInventory(horse.getInventory()));
			if(shouldSerialize("jump-strength"))
				root.put("jump-strength", horse.getJumpStrength());
			if(shouldSerialize("style"))
				root.put("style", horse.getStyle());
			if(shouldSerialize("variant"))
				root.put("variant", horse.getVariant());
			return root;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serialize a Horse as a String.
	 * @param horse The Horse to serialize
	 * @return The serialization String.
	 */
	public static String serializeHorseAsString(Horse horse) {
		return serializeHorseAsString(horse, false);
	}
	
	/**
	 * Serialize a Horse as a String.
	 * @param horse The Horse to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not.
	 * @return The serialization String.
	 */
	public static String serializeHorseAsString(Horse horse, boolean pretty) {
		return serializeHorseAsString(horse, pretty, 5);
	}
	
	/**
	 * Serialize a Horse as a String.
	 * @param horse The Horse to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not.
	 * @param indentFactor The amount of spaces in a tab
	 * @return The serialization String.
	 */
	public static String serializeHorseAsString(Horse horse, boolean pretty, int indentFactor) {
		try {
			if(pretty) {
				return serializeHorse(horse).toString(indentFactor);
			} else {
				return serializeHorse(horse).toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Spawn a Horse in a given Location and apply stats specified in a JSONObject constructed
	 * with the given String.
	 * @param location Where to spawn the Horse
	 * @param stats The stats to apply
	 * @return The Horse spawned
	 */
	public static Horse spawnHorse(Location location, String stats) {
		try {
			return spawnHorse(location, new JSONObject(stats));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Spawn a Horse in a given Location and apply stats specified in the given JSONObject.
	 * @param location Where to spawn the Horse
	 * @param stats The stats to apply
	 * @return The Horse spawned
	 */
	public static Horse spawnHorse(Location location, JSONObject stats) {
		try {
			Horse horse = (Horse) LivingEntitySerialization.spawnEntity(location, stats);
			if(stats.has("color"))
				horse.setColor(Horse.Color.valueOf(stats.getString("color")));
			if(stats.has("jump-strength"))
				horse.setCustomName(stats.getString("name"));
			if(stats.has("style"))
				horse.setStyle(Horse.Style.valueOf(stats.getString("style")));
			if(stats.has("inventory"))
				PotionEffectSerialization.addPotionEffects(stats.getString("potion-effects"), horse);
			if(stats.has("variant"))
				horse.setVariant(Horse.Variant.valueOf(stats.getString("variant")));
			return horse;
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
		return SerializationConfig.getShouldSerialize("horse." + key);
	}
	
}
