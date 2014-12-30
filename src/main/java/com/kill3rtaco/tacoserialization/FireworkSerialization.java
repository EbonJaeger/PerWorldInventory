package com.kill3rtaco.tacoserialization;

import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FireworkSerialization {
	
	protected FireworkSerialization() {
	}
	
	public static FireworkMeta getFireworkMeta(String json) {
		return getFireworkMeta(json);
	}
	
	public static FireworkMeta getFireworkMeta(JSONObject json) {
		try {
			FireworkMeta dummy = (FireworkMeta) new ItemStack(Material.FIREWORK).getItemMeta();
			dummy.setPower(json.optInt("power", 1));
			JSONArray effects = json.getJSONArray("effects");
			for(int i = 0; i < effects.length(); i++) {
				JSONObject effectDto = effects.getJSONObject(i);
				FireworkEffect effect = FireworkEffectSerialization.getFireworkEffect(effectDto);
				if(effect != null)
					dummy.addEffect(effect);
			}
			return dummy;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static JSONObject serializeFireworkMeta(FireworkMeta meta) {
		try {
			JSONObject root = new JSONObject();
			root.put("power", meta.getPower());
			JSONArray effects = new JSONArray();
			for(FireworkEffect e : meta.getEffects()) {
				effects.put(FireworkEffectSerialization.serializeFireworkEffect(e));
			}
			root.put("effects", effects);
			return root;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String serializeFireworkMetaAsString(FireworkMeta meta) {
		return serializeFireworkMetaAsString(meta, false);
	}
	
	public static String serializeFireworkMetaAsString(FireworkMeta meta, boolean pretty) {
		return serializeFireworkMetaAsString(meta, false, 5);
	}
	
	public static String serializeFireworkMetaAsString(FireworkMeta meta, boolean pretty, int indentFactor) {
		return Serializer.toString(serializeFireworkMeta(meta), pretty, indentFactor);
	}
	
}
