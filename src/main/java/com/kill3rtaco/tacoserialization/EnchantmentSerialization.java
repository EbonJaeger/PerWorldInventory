package com.kill3rtaco.tacoserialization;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * A class to help with the serialization of Enchantments. Because of the ability to add unsafe enchantments
 * (item and enchantment level are ignored), enchantments are serialized in a different way than like ChestShop.
 * ChestShop takes the enchantment id, converts it into a String, and appends the level. It then iterates this
 * process for each enchantment. Afterwards, it has a very long number, which it then conveters to a base-32 String.
 * <br/><br/>
 * This class serializes it much differently, and it is in fact more human readable. The process can be explained
 * as such:
 * <pre>
 * String serializationString = "";
 * for(Enchantment e : enchantments){
 *     serializationString += e.getId() + ":" + e.getLevel() + ";";
 * }
 * </pre>
 * 
 * So that the result would be:<br/>
 * <pre>id:level;...</pre>
 * 
 * This allows for much easier readability as well as the possibility to unsafely add enchantments
 * @author KILL3RTACO
 * @since 1.0
 *
 */
public class EnchantmentSerialization {
	
	protected EnchantmentSerialization() {
	}
	
	/**
	 * Serialize a Map of Enchantments and their levels into a string that follows the regex
	 * <pre>([0-9]+:[0-9]+;)+</pre>
	 * @param enchantments The Enchantment Map to serialize
	 * @return
	 */
	public static String serializeEnchantments(Map<Enchantment, Integer> enchantments) {
		String serialized = "";
		for(Enchantment e : enchantments.keySet()) {
			serialized += e.getId() + ":" + enchantments.get(e) + ";";
		}
		return serialized;
	}
	
	/**
	 * Get a Map of Enchantments and their levels from an enchantment serialization string
	 * @param serializedEnchants The serialization string to decode
	 * @return A Map of enchantments and their levels
	 */
	public static Map<Enchantment, Integer> getEnchantments(String serializedEnchants) {
		HashMap<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
		if(serializedEnchants.isEmpty())
			return enchantments;
		String[] enchants = serializedEnchants.split(";");
		for(int i = 0; i < enchants.length; i++) {
			String[] ench = enchants[i].split(":");
			if(ench.length < 2)
				throw new IllegalArgumentException(serializedEnchants + " - Enchantment " + i + " (" + enchants[i] + "): split must at least have a length of 2");
			if(!Util.isNum(ench[0]))
				throw new IllegalArgumentException(serializedEnchants + " - Enchantment " + i + " (" + enchants[i] + "): id is not an integer");
			if(!Util.isNum(ench[1]))
				throw new IllegalArgumentException(serializedEnchants + " - Enchantment " + i + " (" + enchants[i] + "): level is not an integer");
			int id = Integer.parseInt(ench[0]);
			int level = Integer.parseInt(ench[1]);
			Enchantment e = Enchantment.getById(id);
			if(e == null)
				throw new IllegalArgumentException(serializedEnchants + " - Enchantment " + i + " (" + enchants[i] + "): no Enchantment with id of " + id);
			enchantments.put(e, level);
		}
		return enchantments;
	}
	
	/**
	 * Get a Map of Enchantments and their levels from an old enchantment code
	 * @param oldFormat The old (ChestShop compatible) enchantment code.
	 * @return A map of enchantments and their levels using the old (ChestShop compatible) enchantment code.
	 */
	public static Map<Enchantment, Integer> getEnchantsFromOldFormat(String oldFormat) {
		HashMap<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
		if(oldFormat.length() == 0) {
			return enchants;
		}
		String nums = Long.parseLong(oldFormat, 32) + "";
		System.out.println(nums);
		for(int i = 0; i < nums.length(); i += 3) {
			int enchantId = Integer.parseInt(nums.substring(i, i + 2));
			int enchantLevel = Integer.parseInt(nums.charAt(i + 2) + "");
			Enchantment ench = Enchantment.getById(enchantId);
			enchants.put(ench, enchantLevel);
		}
		return enchants;
	}
	
	/**
	 * Convert to the old (ChestShop compatible) enchantment code to a more friendly format
	 * @param oldFormat
	 * @return The converted String
	 */
	public static String convert(String oldFormat) {
		Map<Enchantment, Integer> enchants = getEnchantsFromOldFormat(oldFormat);
		return serializeEnchantments(enchants);
	}
	
	/**
	 * Convert an old base-32 string into a map of enchantments and return the enchantments.
	 * @param oldFormat The old (ChestShop compatible) enchantment code to use
	 * @return A Map of enchantments and their levels using
	 */
	public static Map<Enchantment, Integer> convertAndGetEnchantments(String oldFormat) {
		String newFormat = convert(oldFormat);
		return getEnchantments(newFormat);
	}
	
	/**
	 * Apply Enchantments to an ItemStack using a Enchantment serialization string
	 * @param code The enchantment code to use
	 * @param items The items to apply the enchantments to
	 */
	public static void addEnchantments(String code, ItemStack items) {
		items.addUnsafeEnchantments(getEnchantments(code));
	}
	
}
