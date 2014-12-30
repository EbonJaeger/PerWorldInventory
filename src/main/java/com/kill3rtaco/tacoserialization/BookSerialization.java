package com.kill3rtaco.tacoserialization;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to help with the serialization of books. There are separate methods provided for the ItemMeta
 * of enchanted books and written books
 * @author KILL3RTACO
 * @since 1.0
 *
 */
public class BookSerialization {
	
	protected BookSerialization() {
	}
	
	/**
	 * Get BookMeta from a JSON string
	 * @param json The JSON string that a JSONObject will be constructed from
	 * @return The BookMeta constructed, or null if an error occurs
	 */
	public static BookMeta getBookMeta(String json) {
		try {
			return getBookMeta(new JSONObject(json));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get BookMeta from a JSONObject.
	 * @param json
	 * @return The BookMeta constructed, or null if an error occurs
	 */
	public static BookMeta getBookMeta(JSONObject json) {
		try {
			ItemStack dummyItems = new ItemStack(Material.WRITTEN_BOOK, 1);
			BookMeta meta = (BookMeta) dummyItems.getItemMeta();
			String title = null, author = null;
			JSONArray pages = null;
			if(json.has("title"))
				title = json.getString("title");
			if(json.has("author"))
				author = json.getString("author");
			if(json.has("pages"))
				pages = json.getJSONArray("pages");
			if(title != null)
				meta.setTitle(title);
			if(author != null)
				meta.setAuthor(author);
			if(pages != null) {
				String[] allPages = new String[pages.length()];
				for(int i = 0; i < pages.length(); i++) {
					String page = pages.getString(i);
					if(page.isEmpty() || page == null)
						page = "";
					allPages[i] = page;
				}
				meta.setPages(allPages);
			}
			return meta;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gets a JSONObject representation of a BookMeta. Book and Quills books will have a pages key, while
	 * finished, written, books will also have an author and title key.
	 * @param meta The BookMeta to serialize
	 * @return A JSON Representation of the give BookMeta
	 */
	public static JSONObject serializeBookMeta(BookMeta meta) {
		try {
			JSONObject root = new JSONObject();
			if(meta.hasTitle())
				root.put("title", meta.getTitle());
			if(meta.hasAuthor())
				root.put("author", meta.getAuthor());
			if(meta.hasPages()) {
				String[] pages = meta.getPages().toArray(new String[] {});
				root.put("pages", pages);
			}
			return root;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serialize BookMeta. This will produce the same result as serializeBookMeta(meta).toString()
	 * @param meta The BookMeta to serialize.
	 * @return The serialization string
	 */
	public static String serializeBookMetaAsString(BookMeta meta) {
		return serializeBookMetaAsString(meta, false);
	}
	
	/**
	 * Serialize BookMeta. 
	 * @param meta The BookMeta to serialize.
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @return the serialization string
	 */
	public static String serializeBookMetaAsString(BookMeta meta, boolean pretty) {
		return serializeBookMetaAsString(meta, pretty, 5);
	}
	
	/**
	 * Serialize BookMeta.
	 * @param meta The BookMeta to serialize.
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @param indentFactor The amount of spaces for each indentation
	 * @return the serialization string
	 */
	public static String serializeBookMetaAsString(BookMeta meta, boolean pretty, int indentFactor) {
		try {
			if(pretty) {
				return serializeBookMeta(meta).toString(indentFactor);
			} else {
				return serializeBookMeta(meta).toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get EnchantmentStorageMeta from a JSON string
	 * @param json The string to use
	 * @return The EnchantmentStorageMeta constructed, null if an error occurred
	 */
	public static EnchantmentStorageMeta getEnchantedBookMeta(String json) {
		try {
			return getEnchantedBookMeta(new JSONObject(json));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get EncantmentStorageMeta from a JSONObject.
	 * @param json The JSONObject to use
	 * @return The EnchantmentStorageMeta constructed, null if an error occurred
	 */
	public static EnchantmentStorageMeta getEnchantedBookMeta(JSONObject json) {
		try {
			ItemStack dummyItems = new ItemStack(Material.ENCHANTED_BOOK, 1);
			EnchantmentStorageMeta meta = (EnchantmentStorageMeta) dummyItems.getItemMeta();
			if(json.has("enchantments")) {
				Map<Enchantment, Integer> enchants = EnchantmentSerialization.getEnchantments(json.getString("enchantments"));
				for(Enchantment e : enchants.keySet()) {
					meta.addStoredEnchant(e, enchants.get(e), true);
				}
			}
			return meta;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serialize EnchantmentStorageMeta into JSONObject form.
	 * @param meta The EnchantmentStorageMeta to serialize
	 * @return The JSONObject form of the given EnchantmentStorageMeta
	 */
	public static JSONObject serializeEnchantedBookMeta(EnchantmentStorageMeta meta) {
		try {
			JSONObject root = new JSONObject();
			String enchants = EnchantmentSerialization.serializeEnchantments(meta.getStoredEnchants());
			root.put("enchantments", enchants);
			return root;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serialize EnchantmentStorageMeta into string form.
	 * @param meta The EnchantmentStorageMet to use
	 * @return The serialized string
	 */
	public static String serializeEnchantedBookMetaAsString(EnchantmentStorageMeta meta) {
		return serializeEnchantedBookMetaAsString(meta, false);
	}
	
	/**
	 * Serialize EnchantmentStorageMeta into string form.
	 * @param meta The EnchantmentStorageMet to use
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @return The serialized string
	 */
	public static String serializeEnchantedBookMetaAsString(EnchantmentStorageMeta meta, boolean pretty) {
		return serializeEnchantedBookMetaAsString(meta, pretty, 5);
	}
	
	/**
	 * Serialize EnchantmentStorageMeta into string form.
	 * @param meta The EnchantmentStorageMet to use
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @param indentFactor The amount of spaces to use for a tab
	 * @return The serialized string
	 */
	public static String serializeEnchantedBookMetaAsString(EnchantmentStorageMeta meta, boolean pretty, int indentFactor) {
		try {
			if(pretty) {
				return serializeEnchantedBookMeta(meta).toString(indentFactor);
			} else {
				return serializeEnchantedBookMeta(meta).toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
