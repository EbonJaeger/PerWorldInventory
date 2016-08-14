/*
 * Copyright (C) 2014-2016  EbonJaguar
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.gnat008.perworldinventory.data.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated For compatability only. Never ever use these!
 */
@Deprecated
public class DeprecatedMethodUtil {

    private DeprecatedMethodUtil() {}

    /**
     * Get a Map of Enchantments and their levels from an enchantment serialization string
     *
     * @param serializedEnchants The serialization string to decode
     * @return A Map of enchantments and their levels
     */
    public static Map<Enchantment, Integer> getEnchantments(String serializedEnchants) {
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        if (serializedEnchants.isEmpty())
            return enchantments;

        String[] enchants = serializedEnchants.split(";");
        for (int i = 0; i < enchants.length; i++) {
            String[] ench = enchants[i].split(":");
            if (ench.length < 2)
                throw new IllegalArgumentException(serializedEnchants + " - Enchantment " + i + " (" + enchants[i] + "): split must at least have a length of 2");
            if (isNum(ench[0]))
                throw new IllegalArgumentException(serializedEnchants + " - Enchantment " + i + " (" + enchants[i] + "): id is not an integer");
            if (isNum(ench[1]))
                throw new IllegalArgumentException(serializedEnchants + " - Enchantment " + i + " (" + enchants[i] + "): level is not an integer");
            int id = Integer.parseInt(ench[0]);
            int level = Integer.parseInt(ench[1]);
            Enchantment e = Enchantment.getById(id);
            if (e == null)
                throw new IllegalArgumentException(serializedEnchants + " - Enchantment " + i + " (" + enchants[i] + "): no Enchantment with id of " + id);
            enchantments.put(e, level);
        }

        return enchantments;
    }

    /**
     * A method to deserialize a BannerMeta object from a JSONObject. This method assumes that the JSONArrays containing
     * the colors and pattern types are the same length.
     *
     * @param json The JSONObject of the BannerMeta to deserialize
     * @return The BannerMeta
     */
    public static BannerMeta getBannerMeta(JsonObject json) {
        BannerMeta dummy = (BannerMeta) new ItemStack(Material.BANNER).getItemMeta();
        if (json.has("base-color"))
            dummy.setBaseColor(DyeColor.getByDyeData(Byte.parseByte("" + json.get("base-color"))));

        JsonArray colors = json.getAsJsonArray("colors");
        JsonArray patternTypes = json.getAsJsonArray("pattern-types");
        for (int i = 0; i < colors.size() - 1; i++) {
            dummy.addPattern(new Pattern(DyeColor.getByDyeData(Integer.valueOf(colors.get(i).getAsInt()).byteValue()),
                    PatternType.getByIdentifier(patternTypes.get(i).getAsString())));
        }

        return dummy;
    }

    /**
     * Get BookMeta from a JSONObject.
     *
     * @param json The JsonObject of the BannerMeta to deserialize
     * @return The BookMeta constructed
     */
    public static BookMeta getBookMeta(JsonObject json) {
        ItemStack dummyItems = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta meta = (BookMeta) dummyItems.getItemMeta();
        String title = null, author = null;
        JsonArray pages = null;
        if (json.has("title"))
            title = json.get("title").getAsString();
        if (json.has("author"))
            author = json.get("author").getAsString();
        if (json.has("pages"))
            pages = json.getAsJsonArray("pages");
        if (title != null)
            meta.setTitle(title);
        if (author != null)
            meta.setAuthor(author);
        if (pages != null) {
            String[] allPages = new String[pages.size()];
            for (int i = 0; i < pages.size() - 1; i++) {
                String page = pages.get(i).getAsString();
                if (page.isEmpty() || page == null)
                    page = "";
                allPages[i] = page;
            }
            meta.setPages(allPages);
        }
        return meta;
    }

    /**
     * Get EncantmentStorageMeta from a JSONObject.
     *
     * @param json The JSONObject to use
     * @return The EnchantmentStorageMeta constructed
     */
    public static EnchantmentStorageMeta getEnchantedBookMeta(JsonObject json) {
        ItemStack dummyItems = new ItemStack(Material.ENCHANTED_BOOK, 1);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) dummyItems.getItemMeta();
        if (json.has("enchantments")) {
            Map<Enchantment, Integer> enchants = getEnchantments(json.get("enchantments").getAsString());
            for (Enchantment e : enchants.keySet()) {
                meta.addStoredEnchant(e, enchants.get(e), true);
            }
        }
        return meta;
    }

    /**
     * Gets LeatherArmorMeta from the given JSONObject
     *
     * @param json The JSONObject to decode
     * @return LeatherArmorMeta taken from the given JSONObject as a reference
     */
    public static LeatherArmorMeta getLeatherArmorMeta(JsonObject json) {
        ItemStack dummyItems = new ItemStack(Material.LEATHER_HELMET, 1);
        LeatherArmorMeta meta = (LeatherArmorMeta) dummyItems.getItemMeta();
        if (json.has("color")) {
            meta.setColor(getColor(json.getAsJsonObject("color")));
        }
        return meta;
    }

    /**
     * Get SkullMeta from a JSONObject.
     *
     * @param json The meta information of the skull
     * @return The SkullMeta
     */
    public static SkullMeta getSkullMeta(JsonObject json) {
        ItemStack dummyItems = new ItemStack(Material.SKULL_ITEM);
        SkullMeta dummyMeta = (SkullMeta) dummyItems.getItemMeta();
        if (json.has("owner"))
            dummyMeta.setOwner(json.get("owner").getAsString());
        return dummyMeta;
    }

    public static FireworkMeta getFireworkMeta(JsonObject json) {
        FireworkMeta dummy = (FireworkMeta) new ItemStack(Material.FIREWORK).getItemMeta();

        if (json.has("power"))
            dummy.setPower(json.get("power").getAsInt());
        else
            dummy.setPower(1);

        JsonArray effects = json.getAsJsonArray("effects");
        for (int i = 0; i < effects.size() - 1; i++) {
            JsonObject effectDto = effects.get(i).getAsJsonObject();
            FireworkEffect effect = getFireworkEffect(effectDto);
            if (effect != null)
                dummy.addEffect(effect);
        }
        return dummy;
    }

    private static FireworkEffect getFireworkEffect(JsonObject json) {
        FireworkEffect.Builder builder = FireworkEffect.builder();

        //colors
        JsonArray colors = json.getAsJsonArray("colors");
        for (int j = 0; j < colors.size() - 1; j++) {
            builder.withColor(getColor(colors.get(j).getAsJsonObject()));
        }

        //fade colors
        JsonArray fadeColors = json.getAsJsonArray("fade-colors");
        for (int j = 0; j < fadeColors.size() - 1; j++) {
            builder.withFade(getColor(colors.get(j).getAsJsonObject()));
        }

        //hasFlicker
        if (json.get("flicker").getAsBoolean())
            builder.withFlicker();

        //trail
        if (json.get("trail").getAsBoolean())
            builder.withTrail();

        //type
        builder.with(FireworkEffect.Type.valueOf(json.get("type").getAsString()));

        return builder.build();
    }

    /**
     * Get a Color from a JSONObject. If any one of the red, green, or blue keys are not found,
     * they are given a value of 0 by default. Therefore, if the red and green values found were both 0,
     * and the blue key is not found, the resulting color is black (0, 0, 0).
     *
     * @param color The JSONObject to construct a Color from.
     * @return The decoded Color
     */
    private static Color getColor(JsonObject color) {
        int r = 0, g = 0, b = 0;
        if (color.has("red"))
            r = color.get("red").getAsInt();
        if (color.has("green"))
            g = color.get("green").getAsInt();
        if (color.has("blue"))
            b = color.get("blue").getAsInt();
        return Color.fromRGB(r, g, b);
    }

    /**
     * Method used to test whether a string is an Integer or not
     *
     * @param s The string to test
     * @return Whether the given string is an Integer
     */
    public static boolean isNum(String s) {
        try {
            Integer.parseInt(s);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * Test
     *
     * @param material
     * @return True if the given material is Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
     * Material.LEATHER_LEGGINGS, or  Material.LEATHER_BOOTS;
     */
    public static boolean isLeatherArmor(Material material) {
        return material == Material.LEATHER_HELMET || material == Material.LEATHER_CHESTPLATE ||
                material == Material.LEATHER_LEGGINGS || material == Material.LEATHER_BOOTS;
    }
}
