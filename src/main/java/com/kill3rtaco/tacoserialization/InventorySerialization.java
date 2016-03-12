package com.kill3rtaco.tacoserialization;

import me.gnat008.perworldinventory.data.players.PWIPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * A class to help with the serializatiion of inventory. All data is saved in a JSON format.
 * <p/>
 * Inventories are serialized perfectly. Books, whether they are enchanted or written (signed or not)
 * are saved with the enchantments or pages in their ItemMeta. Dyed armor is also perfectly saved, with
 * the Red, Green, and Blue value saved appropriately.
 * <p/>
 * PlayerInventories will have a longer String length, depending on the contents or their armor.
 *
 * @author KILL3RTACO
 * @since 1.0
 */
public class InventorySerialization {

    protected InventorySerialization() {
    }

    /**
     * Serialize a PlayerInventory. This will save the armor contents of the inventory as well
     *
     * @param player The player to serialize
     * @return A JSONObject representing the serialized Inventory.
     */
    public static JSONObject serializePlayerInventory(PWIPlayer player) {
        try {
            JSONObject root = new JSONObject();
            JSONArray inventory = serializeInventory(player.getInventory());
            JSONArray armor = serializeInventory(player.getArmor());
            root.put("inventory", inventory);
            root.put("armor", armor);
            return root;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Serialize an ItemStack array.
     *
     * @param contents The items in the inventory
     * @return A JSONArray representing the serialized ItemStack array
     */
    public static JSONArray serializeInventory(ItemStack[] contents) {
        JSONArray inventory = new JSONArray();
        for (int i = 0; i < contents.length; i++) {
            JSONObject values = SingleItemSerialization.serializeItemInInventory(contents[i], i);
            if (values != null)
                inventory.put(values);
        }
        return inventory;
    }

    /**
     * Gets an ItemStack array from a JSONObject.
     *
     * @param inv  The JSONObject to get from
     * @param size The expected size of the inventory, can be greater than expected
     * @param format Data format being used; 0 is old, 1 is new
     * @return An ItemStack array constructed from the given JSONArray
     */
    public static ItemStack[] getInventory(JSONArray inv, int size, int format) {
        try {
            ItemStack[] contents = new ItemStack[size];
            for (int i = 0; i < inv.length(); i++) {
                JSONObject item = inv.getJSONObject(i);
                int index = item.getInt("index");
                if (index > size)
                    throw new IllegalArgumentException("Index found is greater than expected size (" + index + " > " + size + ")");
                if (index > contents.length || index < 0)
                    throw new IllegalArgumentException("Item " + i + " - Slot " + index + " does not exist in this inventory");

                ItemStack stuff;
                if (format == 1) {
                    stuff = SingleItemSerialization.deserializeItem(item);
                } else {
                    stuff = SingleItemSerialization.getItem(item);
                }
                contents[index] = stuff;
            }
            return contents;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sets the holders Inventory using an ItemStack array constructed from a JSONArray.
     *
     * @param holder The InventoryHolder to which the Inventory will be set
     * @param inv    The reference JSONArray
     * @param format Data format being used; 0 is old, 1 is new
     */
    public static void setInventory(InventoryHolder holder, JSONArray inv, int format) {
        setInventory(holder.getInventory(), inv, format);
    }

    /**
     * Sets the Inventory using an ItemStack array constructed from a JSONArray.
     *
     * @param inventory The InventoryHolder to which the Inventory will be set
     * @param inv       The reference JSONArray
     * @param format Data format being used; 0 is old, 1 is new
     */
    public static void setInventory(Inventory inventory, JSONArray inv, int format) {
        ItemStack[] items = getInventory(inv, inventory.getSize(), format);
        inventory.clear();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null)
                continue;
            inventory.setItem(i, item);
        }
    }

    /**
     * Sets the Inventory using an ItemStack array constructed from a JSONObject.
     *
     * @param player The InventoryHolder to which the Inventory will be set
     * @param inv    The reference JSONArray
     * @param format Data format being used; 0 is old, 1 is new
     */
    public static void setPlayerInventory(Player player, JSONObject inv, int format) {
        try {
            PlayerInventory inventory = player.getInventory();
            ItemStack[] armor = getInventory(inv.getJSONArray("armor"), 4, format);
            inventory.clear();
            inventory.setArmorContents(armor);
            setInventory(player, inv.getJSONArray("inventory"), format);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
