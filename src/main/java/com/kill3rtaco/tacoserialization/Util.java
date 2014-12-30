package com.kill3rtaco.tacoserialization;

import org.bukkit.Material;

public class Util {

	protected Util() {}
	
	/**
	 * Method used to test whether a string is an Integer or not
	 * @param s The string to test
	 * @return Whether the given string is an Integer
	 */
	public static boolean isNum(String s){
		try{
			Integer.parseInt(s);
			return true;
		} catch(NumberFormatException e){
			return false;
		}
	}
	
	/**
	 * Test 
	 * @param material
	 * @return True if the given material is Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
	 * Material.LEATHER_LEGGINGS, or  Material.LEATHER_BOOTS;
	 */
	public static boolean isLeatherArmor(Material material){
		return material == Material.LEATHER_HELMET || material == Material.LEATHER_CHESTPLATE || 
				material == Material.LEATHER_LEGGINGS || material == Material.LEATHER_BOOTS;
	}
	
	public static boolean keyFound(String[] array, String key){
		for(String s : array){
			if(s.equalsIgnoreCase(key));
		}
		return false;
	}

}
