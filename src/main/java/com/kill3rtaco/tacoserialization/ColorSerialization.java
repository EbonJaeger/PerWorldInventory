package com.kill3rtaco.tacoserialization;

import org.bukkit.Color;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to help with the serialilzation of Colors. Note that the Color class used is
 * Bukkit's org.bukkit.Color, to provide support to create a color using BGR <i>and</i> RGB formats.
 * 
 * You can convert a org.bukkit.Color to a java.awt.Color by doing the following:
 * 
 * <pre>
 * org.bukkit.Color testColor = new org.bukkit.Color.fromRGB(255, 255, 0); //yellow
 * jawa.awt.Color converted = new java.awt.Color(testColor.asRGB()); //asBGR() will not work, as java.awt.Color uses the RGB format.
 * </pre>
 * @author KILL3RTACO
 * @since 1.0
 *
 */
public class ColorSerialization {
	
	protected ColorSerialization() {
	}
	
	/**
	 * Serialize a Color into JSONObject form. Colors are serialized into a JSONObject with red, green, and blue
	 * as keys
	 * @param color The color to serialize.
	 * @return The JSONObject form a the given Color
	 */
	public static JSONObject serializeColor(Color color) {
		try {
			
			JSONObject root = new JSONObject();
			root.put("red", color.getRed());
			root.put("green", color.getGreen());
			root.put("blue", color.getBlue());
			return root;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get a Color from a JSON string.
	 * @param color The string to decode from.
	 * @return The color constructed, null if an error occurred.
	 */
	public static Color getColor(String color) {
		try {
			return getColor(new JSONObject(color));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get a Color from a JSONObject. If any one of the red, green, or blue keys are not found, 
	 * they are given a value of 0 by default. Therefore, if the red and green values found were both 0,
	 * and the blue key is not found, the resulting color is black (0, 0, 0).
	 * @param color The JSONObject to construct a Color from.
	 * @return
	 */
	public static Color getColor(JSONObject color) {
		try {
			int r = 0, g = 0, b = 0;
			if(color.has("red"))
				r = color.getInt("red");
			if(color.has("green"))
				g = color.getInt("green");
			if(color.has("blue"))
				b = color.getInt("blue");
			return Color.fromRGB(r, g, b);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serialize a Color into a String.
	 * @param color The Color to serialize
	 * @return The serialization string
	 */
	public static String serializeColorAsString(Color color) {
		return serializeColorAsString(color, false);
	}
	
	/**
	 * Serialize a Color into a String.
	 * @param color The Color to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @return The serialization string
	 */
	public static String serializeColorAsString(Color color, boolean pretty) {
		return serializeColorAsString(color, pretty, 5);
	}
	
	/**
	 * Serialize a Color into a String.
	 * @param color The Color to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @param indentFactor The amount of spaces to use for a tab
	 * @return The serialization string
	 */
	public static String serializeColorAsString(Color color, boolean pretty, int indentFactor) {
		try {
			if(pretty) {
				return serializeColor(color).toString(indentFactor);
			} else {
				return serializeColor(color).toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
