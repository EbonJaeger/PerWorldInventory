package com.kill3rtaco.tacoserialization;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FireworkEffectSerialization {
	
	protected FireworkEffectSerialization() {
	}
	
	public static FireworkEffect getFireworkEffect(String json) {
		return getFireworkEffect(json);
	}
	
	public static FireworkEffect getFireworkEffect(JSONObject json) {
		try {
			FireworkEffect.Builder builder = FireworkEffect.builder();
			
			//colors
			JSONArray colors = json.getJSONArray("colors");
			for(int j = 0; j < colors.length(); j++) {
				builder.withColor(ColorSerialization.getColor(colors.getJSONObject(j)));
			}
			
			//fade colors
			JSONArray fadeColors = json.getJSONArray("fade-colors");
			for(int j = 0; j < fadeColors.length(); j++) {
				builder.withFade(ColorSerialization.getColor(colors.getJSONObject(j)));
			}
			
			//hasFlicker
			if(json.getBoolean("flicker"))
				builder.withFlicker();
			
			//trail
			if(json.getBoolean("trail"))
				builder.withTrail();
			
			//type
			builder.with(FireworkEffect.Type.valueOf(json.getString("type")));
			
			return builder.build();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static JSONObject serializeFireworkEffect(FireworkEffect effect) {
		try {
			JSONObject root = new JSONObject();
			
			//colors
			JSONArray colors = new JSONArray();
			for(Color c : effect.getColors()) {
				colors.put(ColorSerialization.serializeColor(c));
			}
			root.put("colors", colors);
			
			//fade colors
			JSONArray fadeColors = new JSONArray();
			for(Color c : effect.getFadeColors()) {
				fadeColors.put(ColorSerialization.serializeColor(c));
			}
			root.put("fade-colors", fadeColors);
			
			//hasFlicker
			root.put("flicker", effect.hasFlicker());
			
			//trail
			root.put("trail", effect.hasTrail());
			
			//type
			root.put("type", effect.getType().name());
			
			return root;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String serializeFireworkEffectAsString(FireworkEffect effect) {
		return serializeFireworkEffectAsString(effect, false);
	}
	
	public static String serializeFireworkEffectAsString(FireworkEffect effect, boolean pretty) {
		return serializeFireworkEffectAsString(effect, false, 5);
	}
	
	public static String serializeFireworkEffectAsString(FireworkEffect effect, boolean pretty, int indentFactor) {
		return Serializer.toString(serializeFireworkEffect(effect), pretty, indentFactor);
	}
	
}
