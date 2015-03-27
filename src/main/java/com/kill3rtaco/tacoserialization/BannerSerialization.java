package com.kill3rtaco.tacoserialization;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to serialize Banners.
 */
public class BannerSerialization {

    protected BannerSerialization() {
    }

    /**
     * A method to serialize banner meta to a JSONObject. Saves the base color, each pattern color, and each
     * pattern type.
     *
     * @param banner The BannerMeta to serialize
     * @return The JSONObject representation of the BannerMeta, or null if an exception occurred
     */
    public static JSONObject serializeBanner(BannerMeta banner) {
        try {
            JSONObject root = new JSONObject();
            root.put("base-color", banner.getBaseColor().getDyeData());

            JSONArray colors = new JSONArray();
            JSONArray patternTypes = new JSONArray();
            for (Pattern pattern : banner.getPatterns()) {
                colors.put(pattern.getColor().getDyeData());
                patternTypes.put(pattern.getPattern().getIdentifier());
            }

            root.put("colors", colors);
            root.put("pattern-types", patternTypes);

            return root;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * A method to deserialize a BannerMeta object from a JSONObject. This method assumes that the JSONArrays containing
     * the colors and pattern types are the same length.
     *
     * @param json The JSONObject of the BannerMeta to deserialize
     * @return The BannerMeta or null if an exception occurs
     */
    public static BannerMeta getBannerMeta(JSONObject json) {
        try {
            BannerMeta dummy = (BannerMeta) new ItemStack(Material.BANNER).getItemMeta();
            dummy.setBaseColor(DyeColor.getByDyeData(Byte.parseByte("" + json.get("base-color"))));

            JSONArray colors = json.getJSONArray("colors");
            JSONArray patternTypes = json.getJSONArray("pattern-types");
            for (int i = 0; i < colors.length(); i++) {
                dummy.addPattern(new Pattern(DyeColor.getByDyeData(Byte.parseByte("" + colors.get(i))),
                        PatternType.getByIdentifier(patternTypes.getString(i))));
            }

            return dummy;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
