/*
 * Copyright (C) 2014-2015  Gnat008
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

package me.gnat008.perworldinventory.serialization;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class SingleItemSerializer {
    
    protected SingleItemSerializer() {}

    /**
     * Serialize an ItemStack in an Inventory. An index will be added
     * to preserve its location in the Inventory.
     *
     * @param item The item to serialize
     * @param index The position of the item in the inventory
     * @return The serialized item
     */
    public static JSONObject serializeInventoryItem(ItemStack item, int index) {
        return serializeItemStack(item, true, index);
    }

    private static JSONObject serializeItemStack(ItemStack item, boolean useIndex, int index) {
        try {
            JSONObject values = new JSONObject();
            if (item == null) {
                return null;
            }
            
            int id = item.getTypeId();
            int amount = item.getAmount();
            int data = item.getDurability();
            Material material = item.getType();
            String name = null, enchants = null;
            String[] lore = null;
            
            JSONObject bookMeta = null, armorMeta = null, skullMeta = null, fireworkMeta = null;
            
            if (material == Material.BOOK_AND_QUILL || material == Material.WRITTEN_BOOK) {
                bookMeta = BookSerializer.serializeBookMeta((BookMeta) item.getItemMeta());
            } else if (material == Material.ENCHANTED_BOOK) {
                bookMeta = BookSerializer.serializeEnchantedBookMeta((EnchantmentStorageMeta) item.getItemMeta());
            } else if (Util.isLeatherArmor(material)) {
                armorMeta = LeatherArmorSerializer.serializeArmor((LeatherArmorMeta) item.getItemMeta());
            } else if (material == Material.SKULL_ITEM) {
                skullMeta = SkullSerializer.serializeSkull((SkullMeta) item.getItemMeta());
            } else if (material == Material.FIREWORK) {
                fireworkMeta = FireworkSerializer.serializeFireworkMeta((FireworkMeta) item.getItemMeta());
            }
            
            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                
                if (meta.hasDisplayName())
                    name = meta.getDisplayName();
                if (meta.hasLore())
                    lore = meta.getLore().toArray(new String[meta.getLore().size()]);
                if (meta.hasEnchants())
                    enchants = EnchantmentSerializer.serializeEnchantments(meta.getEnchants());
            }
            
            values.put("id", id);
            values.put("amount", amount);
            values.put("data", data);
            if (useIndex)
                values.put("index", index);
            if (name != null)
                values.put("name", name);
            if (enchants != null)
                values.put("enchantments", enchants);
            if (lore != null)
                values.put("lore", lore);
            if (bookMeta != null && bookMeta.length() > 0)
                values.put("book-meta", bookMeta);
            if (armorMeta != null && armorMeta.length() > 0)
                values.put("armor-meta", armorMeta);
            if (skullMeta != null && skullMeta.length() > 0)
                values.put("skull-meta", skullMeta);
            if (fireworkMeta != null && fireworkMeta.length() > 0)
                values.put("firework-meta", fireworkMeta);
            
            return values;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Deserialize an ItemStack from a JSON Object.
     *
     * @param json The JSON Object to deserialize
     * @return The deserialized ItemStack
     */
    public static ItemStack deserializeItemStack(JSONObject json) {
        return deserializeItemStack(json, 0);        
    }

    /**
     * Deserialize an ItemStack. Index is only used for debugging.
     *
     * @param json
     * @param index
     * @return
     */
    public static ItemStack deserializeItemStack(JSONObject json, int index) {
        try {
            int id = json.getInt("id");
            int amount = json.optInt("amount", 1);
            int data = json.optInt("data", 0);
            String name = null;
            Map<Enchantment, Integer> enchants = null;
            ArrayList<String> lore = null;
            
            if (json.has("name"))
                name = json.getString("name");
            if (json.has("enchantments"))
                enchants = EnchantmentSerializer.deserializeEnchantments(json.getString("enchantments"));
            if (json.has("lore")) {
                JSONArray l = json.getJSONArray("lore");
                lore = new ArrayList<>();
                for (int i = 0; i < l.length(); i++) {
                    lore.add(l.getString(i));
                }
            }
            
            if (Material.getMaterial(id) == null)
                throw new IllegalArgumentException("Item " + index + " - No Material found with id: " + id);
            Material material = Material.getMaterial(id);
            ItemStack item = new ItemStack(material, amount, (short) data);
            
            if ((material == Material.BOOK_AND_QUILL || material == Material.WRITTEN_BOOK) && json.has("book-meta")) {
                BookMeta meta = BookSerializer.deserializeBookMeta(json.getJSONObject("book-meta"));
                item.setItemMeta(meta);
            } else if (material == Material.ENCHANTED_BOOK && json.has("book-meta")) {
                EnchantmentStorageMeta meta = BookSerializer.deserializeEnchantedBookMeta(json.getJSONObject("book-meta"));
                item.setItemMeta(meta);
            } else if (Util.isLeatherArmor(material) && json.has("armor-meta")) {
                LeatherArmorMeta meta = LeatherArmorSerializer.deserializeArmor(json.getJSONObject("armor-meta"));
                item.setItemMeta(meta);
            } else if (material == Material.SKULL_ITEM && json.has("skull-meta")) {
                SkullMeta meta = SkullSerializer.deserializeSkullMeta(json.getJSONObject("skull-meta"));
                item.setItemMeta(meta);
            } else if (material == Material.FIREWORK && json.has("firework-meta")) {
                FireworkMeta meta = FireworkSerializer.deserializeFireworkMeta(json.getJSONObject("firework-meta"));
                item.setItemMeta(meta);
            }
            
            ItemMeta meta = item.getItemMeta();
            if (name != null)
                meta.setDisplayName(name);
            if (lore != null)
                meta.setLore(lore);
            item.setItemMeta(meta);
            
            if (enchants != null)
                item.addUnsafeEnchantments(enchants);
            
            return item;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
