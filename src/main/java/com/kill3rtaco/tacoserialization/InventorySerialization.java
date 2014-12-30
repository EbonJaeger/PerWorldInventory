package com.kill3rtaco.tacoserialization;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to help with the serializatiion of inventory. All data is saved in a JSON format.
 * 
 * Inventories are serialized perfectly. Books, whether they are enchanted or written (signed or not)
 * are saved with the enchantments or pages in their ItemMeta. Dyed armor is also perfectly saved, with
 * the Red, Green, and Blue value saved appropriately.
 * 
 * PlayerInventories will have a longer String length, depending on the contents or their armor.
 * @author KILL3RTACO
 * @since 1.0
 *
 */
public class InventorySerialization {
	
	protected InventorySerialization() {
	}
	
	/**
	 * Serialization an Inventory. Note that this does not save the armor contents for a PlayerInventory.
	 * @param inv The Inventory to serialize
	 * @return A JSONArray representing the serialized Inventory.
	 */
	public static JSONArray serializeInventory(Inventory inv) {
		JSONArray inventory = new JSONArray();
		for(int i = 0; i < inv.getSize(); i++) {
			JSONObject values = SingleItemSerialization.serializeItemInInventory(inv.getItem(i), i);
			if(values != null)
				inventory.put(values);
		}
		return inventory;
	}
	
	/**
	 * Serialize a PlayerInventory. This will save the armor contents of the inventory as well
	 * @param inv The Inventory to serialize
	 * @return A JSONObject representing the serialized Inventory.
	 */
	public static JSONObject serializePlayerInventory(PlayerInventory inv) {
		try {
			JSONObject root = new JSONObject();
			JSONArray inventory = serializeInventory(inv);
			JSONArray armor = serializeInventory(inv.getArmorContents());
			root.put("inventory", inventory);
			root.put("armor", armor);
			return root;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the string form of the serialized PlayerInventory. This produces the exact same results as
	 * <code>serializePlayerInventory(inv).toString()</code>
	 * @param inv The Inventory to serialize
	 * @return The String form of the serialized PlayerInventory
	 */
	public static String serializePlayerInventoryAsString(PlayerInventory inv) {
		return serializePlayerInventoryAsString(inv, false);
	}
	
	/**
	 * Get the string form of the serialized PlayerInventory. If <code>pretty</code> is <code>true</code>
	 * then the resulting String will include whitespace and tabs, with each tab having a size of 5.
	 * @param inv The Inventory to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @return The String form of the serialized PlayerInventory
	 */
	public static String serializePlayerInventoryAsString(PlayerInventory inv, boolean pretty) {
		return serializePlayerInventoryAsString(inv, pretty, 5);
	}
	
	/**
	 * Get the string form of the serialized PlayerInventory. If <code>pretty</code> is <code>true</code>
	 * then the resulting String will include whitespace and tabs, with each tab having a size of 
	 * <code>indentFactor</code>.
	 * @param inv The Inventory to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @param indentFactor The size of the tabs
	 * @return The String form of the serialized PlayerInventory
	 */
	public static String serializePlayerInventoryAsString(PlayerInventory inv, boolean pretty, int indentFactor) {
		try {
			if(pretty) {
				return serializePlayerInventory(inv).toString(indentFactor);
			} else {
				return serializePlayerInventory(inv).toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the string form of the serialized Inventory. This produces the exact same results as
	 * <code>serializeInventory(inventory).toString()</code>
	 * @param inventory The Inventory to serialize
	 * @return The String form of the serialized Inventory
	 */
	public static String serializeInventoryAsString(Inventory inventory) {
		return serializeInventoryAsString(inventory, false);
	}
	
	/**
	 * Get the string form of the serialized Inventory. If <code>pretty</code> is <code>true</code>
	 * then the resulting String will include whitespace and tabs, with each tab having a size of 5.
	 * @param inventory The Inventory to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @return The String form of the serialized Inventory
	 */
	public static String serializeInventoryAsString(Inventory inventory, boolean pretty) {
		return serializeInventoryAsString(inventory, pretty, 5);
	}
	
	/**
	 * Get the string form of the serialized Inventory. If <code>pretty</code> is <code>true</code>
	 * then the resulting String will include whitespace and tabs, with each tab having a size of 
	 * <code>indentFactor</code>.
	 * @param inventory The Inventory to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @param indentFactor The size of the tabs
	 * @return The String form of the serialized Inventory
	 */
	public static String serializeInventoryAsString(Inventory inventory, boolean pretty, int indentFactor) {
		try {
			if(pretty) {
				return serializeInventory(inventory).toString(indentFactor);
			} else {
				return serializeInventory(inventory).toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the String form of the serialiazed ItemStack array. This produces the same result as
	 * <code>serializeInventory(contents).toString()</code>
	 * @param contents The Items to serialize
	 * @return The serialization string
	 */
	public static String serializeInventoryAsString(ItemStack[] contents) {
		return serializeInventoryAsString(contents, false);
	}
	
	/**
	 * Get the String form of the serialiazed ItemStack array. If <code>pretty</code> is <code>true</code>
	 * then the resulting String will include whitespace and tabs, with each tab having a size of 5.
	 * @param contents The Inventory to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @return The String form of the serialized Inventory
	 */
	public static String serializeInventoryAsString(ItemStack[] contents, boolean pretty) {
		return serializeInventoryAsString(contents, pretty, 5);
	}
	
	/**
	 * Get the String form of the serialiazed ItemStack array. If <code>pretty</code> is <code>true</code>
	 * then the resulting String will include whitespace and tabs, with each tab having a size of 
	 * <code>indentFactor</code>.
	 * @param contents The Inventory to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @param indentFactor The size of the tabs
	 * @return The String form of the serialized Inventory
	 */
	public static String serializeInventoryAsString(ItemStack[] contents, boolean pretty, int indentFactor) {
		try {
			if(pretty) {
				return serializeInventory(contents).toString(indentFactor);
			} else {
				return serializeInventory(contents).toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serialize an ItemStack array.
	 * @param contents
	 * @return A JSONArray representing the serialized ItemStack array
	 */
	public static JSONArray serializeInventory(ItemStack[] contents) {
		JSONArray inventory = new JSONArray();
		for(int i = 0; i < contents.length; i++) {
			JSONObject values = SingleItemSerialization.serializeItemInInventory(contents[i], i);
			if(values != null)
				inventory.put(values);
		}
		return inventory;
	}
	
	/**
	 * Get an ItemStack array from a JSON String.
	 * @param json The JSON String to use
	 * @param size The expected size of the inventory, can be greater than expected
	 * @return An ItemStack array constructed from a JSONArray constructed from the given String
	 */
	public static ItemStack[] getInventory(String json, int size) {
		try {
			return getInventory(new JSONArray(json), size);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gets an ItemStack array from a JSONObject.
	 * @param inv The JSONObject to get from
	 * @param size The expected size of the inventory, can be greater than expected
	 * @return An ItemStack array constructed from the given JSONArray
	 */
	public static ItemStack[] getInventory(JSONArray inv, int size) {
		try {
			ItemStack[] contents = new ItemStack[size];
			for(int i = 0; i < inv.length(); i++) {
				JSONObject item = inv.getJSONObject(i);
				int index = item.getInt("index");
				if(index > size)
					throw new IllegalArgumentException("index found is greator than expected size (" + index + ">" + size + ")");
				if(index > contents.length || index < 0)
					throw new IllegalArgumentException("Item " + i + " - Slot " + index + " does not exist in this inventory");
				ItemStack stuff = SingleItemSerialization.getItem(item);
				contents[index] = stuff;
			}
			return contents;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get an ItemStack array from a json file
	 * @param jsonFile The File to use
	 * @param size The expected size of the inventory, can be greater than expected
	 * @return An ItemStack array constructed from a JSONArray using the given file as a reference
	 */
	public static ItemStack[] getInventory(File jsonFile, int size) {
		String source = "";
		try {
			Scanner x = new Scanner(jsonFile);
			while (x.hasNextLine()) {
				source += x.nextLine() + "\n";
			}
			x.close();
			return getInventory(source, size);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Sets the holders Inventory using an ItemStack array constructed from a JSONArray using the given 
	 * String as a reference.
	 * @param holder The InventoryHolder to which the Inventory will be set
	 * @param inv The reference JSON string
	 */
	public static void setInventory(InventoryHolder holder, String inv) {
		setInventory(holder.getInventory(), inv);
	}
	
	/**
	 * Sets the holders Inventory using an ItemStack array constructed from a JSONArray.
	 * @param holder The InventoryHolder to which the Inventory will be set
	 * @param inv The reference JSONArray
	 */
	public static void setInventory(InventoryHolder holder, JSONArray inv) {
		setInventory(holder.getInventory(), inv);
	}
	
	/**
	 * Sets the Inventory using an ItemStack array constructed from a JSONArray using the given 
	 * String as a reference.
	 * @param inventory The InventoryHolder to which the Inventory will be set
	 * @param inv The reference JSON string
	 */
	public static void setInventory(Inventory inventory, String inv) {
		try {
			setInventory(inventory, new JSONArray(inv));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the Inventory using an ItemStack array constructed from a JSONArray.
	 * @param inventory The InventoryHolder to which the Inventory will be set
	 * @param inv The reference JSONArray
	 */
	public static void setInventory(Inventory inventory, JSONArray inv) {
		ItemStack[] items = getInventory(inv, inventory.getSize());
		inventory.clear();
		for(int i = 0; i < items.length; i++) {
			ItemStack item = items[i];
			if(item == null)
				continue;
			inventory.setItem(i, item);
		}
	}
	
	/**
	 * Sets the players Inventory using an ItemStack array constructed from a JSONObject using the given 
	 * String as a reference.
	 * @param player The InventoryHolder to which the Inventory will be set
	 * @param inv The reference JSON string
	 */
	public static void setPlayerInventory(Player player, String inv) {
		try {
			setPlayerInventory(player, new JSONObject(inv));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the Inventory using an ItemStack array constructed from a JSONObject.
	 * @param player The InventoryHolder to which the Inventory will be set
	 * @param inv The reference JSONArray
	 */
	public static void setPlayerInventory(Player player, JSONObject inv) {
		try {
			PlayerInventory inventory = player.getInventory();
			ItemStack[] armor = getInventory(inv.getJSONArray("armor"), 4);
			inventory.clear();
			inventory.setArmorContents(armor);
			setInventory(player, inv.getJSONArray("inventory"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
}
