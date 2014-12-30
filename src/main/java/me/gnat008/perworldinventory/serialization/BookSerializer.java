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
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class BookSerializer {
    
    protected BookSerializer() {}

    /**
     * Gets a JSONObject representation of a BookMeta. Book and Quills books
     * will have a pages key, while finished, written, books will also have an
     * author and title key.
     *
     * @param meta The BookMeta to serialize
     * @return The JSON representation of the BookMeta
     */
    public static JSONObject serializeBookMeta(BookMeta meta) {
        try {
            JSONObject root = new JSONObject();
            
            if (meta.hasTitle())
                root.put("title", meta.getTitle());
            if (meta.hasAuthor())
                root.put("author", meta.getAuthor());
            if (meta.hasPages()) {
                String[] pages = meta.getPages().toArray(new String[meta.getPages().size()]);
                root.put("pages", pages);
            }
            
            return root;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static EnchantmentStorageMeta deserializeEnchantedBookMeta(JSONObject json) {
        try {
            ItemStack dummy = new ItemStack(Material.ENCHANTED_BOOK, 1);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) dummy.getItemMeta();
            
            if (json.has("enchantments")) {
                Map<Enchantment, Integer> enchants = EnchantmentSerializer.deserializeEnchantments(json.getString("enchantments"));
                for (Enchantment e : enchants.keySet()) {
                    meta.addStoredEnchant(e, enchants.get(e), true);
                }
            }
            
            return meta;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Serialize EnchantmentStorageMeta into JSON form.
     *
     * @param meta The enchantment meta to serialize
     * @return The JSON representation of the meta, or null
     */
    public static JSONObject serializeEnchantedBookMeta(EnchantmentStorageMeta meta) {
        try {
            JSONObject root = new JSONObject();
            
            String enchants = EnchantmentSerializer.serializeEnchantments(meta.getStoredEnchants());
            root.put("enchantments", enchants);
            
            return root;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Gets book meta from a JSON object.
     *
     * @param json The JSON object
     * @return The book meta
     */
    public static BookMeta deserializeBookMeta(JSONObject json) {
        try {
            ItemStack dummy = new ItemStack(Material.WRITTEN_BOOK, 1);
            BookMeta meta = (BookMeta) dummy.getItemMeta();
            String title = null, author = null;
            JSONArray pages = null;

            if (json.has("title"))
                title = json.getString("title");
            if (json.has("author"))
                author = json.getString("author");
            if (json.has("pages"))
                pages = json.getJSONArray("pages");

            if (title != null)
                meta.setTitle(title);
            if (author != null)
                meta.setAuthor(author);
            if (pages != null) {
                String[] totalPages = new String[pages.length()];
                for (int i = 0; i < pages.length(); i++) {
                    String page = pages.getString(i);
                    if (page.isEmpty() || page == null)
                        page = "";

                    totalPages[i] = page;
                }

                meta.setPages(totalPages);
            }

            return meta;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
