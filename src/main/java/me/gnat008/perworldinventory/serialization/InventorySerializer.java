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

public class InventorySerializer {
    
    protected InventorySerializer() {}

    /**
     * Serialize an Inventory. This does not save the armor contents for a 
     * PlayerInventory.
     *  
     * @param inv The Inventory to serialize
     * @return The JSON Array representation of the Inventory
     */
    public static JSONArray serializeInventory(Inventory inv) {
        JSONArray inventory = new JSONArray();
        
        for (int i = 0; i < inv.getSize(); i++) {
            JSONObject values = SingleItemSerializer.serializeInventoryItem(inv.getItem(i), i);
            if (values != null)
                inventory.put(values);
        }
        
        return inventory;
    }

    /**
     * Serialize an ItemStack array.
     *
     * @param contents The ItemStack array to serialize
     * @return The JSON Array representation of the ItemStack array
     */
    public static JSONArray serializeInventory(ItemStack[] contents) {
        JSONArray inventory = new JSONArray();
        for (int i = 0; i < contents.length; i++) {
            JSONObject values = SingleItemSerializer.serializeInventoryItem(contents[i], i);
            if (values != null)
                inventory.put(values);
        }
        
        return inventory;
    }

    /**
     * Serialize a PlayerInventory. This will save the armor contents
     * of the inventory. 
     *
     * @param inv The Player Inventory to serialize
     * @return The JSON representation of the Player Inventory
     */
    public static JSONObject serializePlayerInventory(PlayerInventory inv) {
        try {
            JSONObject root = new JSONObject();
            JSONArray inventory = serializeInventory(inv);
            JSONArray armor = serializeInventory(inv.getArmorContents());
            
            root.put("inventory", inventory);
            root.put("armor", armor);
            
            return root;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get an ItemStack array from a JSON file.
     *
     * @param jsonFile The JSON file to deserialize
     * @param size The expected size of the inventory
     * @return An ItemStack array constructed from a JSON Array
     */
    public static ItemStack[] deserializeInventory(File jsonFile, int size) {
        String source = "";
        try {
            Scanner in = new Scanner(jsonFile);
            while (in.hasNextLine()) {
                source += in.nextLine() + "\n";
            }
            
            in.close();
            return deserializeInventory(source, size);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get an ItemStack[] from a JSON String.
     *  
     * @param json The JSON String to deserialize
     * @param size The expected size of the inventory
     * @return An ItemStack array constructed from a JSON Array
     */
    private static ItemStack[] deserializeInventory(String json, int size) {
        try {
            return deserializeInventory(new JSONArray(json), size);
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get an ItemStack[] from a JSON Array.
     *
     * @param json The JSON Array to deserialize
     * @param size The expected size of the inventory
     * @return The deserialized ItemStack array
     */
    private static ItemStack[] deserializeInventory(JSONArray json, int size) {
        try {
            ItemStack[] contents = new ItemStack[size];
            for (int i = 0; i < json.length(); i++) {
                JSONObject item = json.getJSONObject(i);
                
                int index = item.getInt("index");
                if (index > size)
                    throw new IllegalArgumentException("Index found is greator than expected size (" + index + ">" + size + ")");
                if (index > contents.length || index < 0)
                    throw new IllegalArgumentException("Item " + i + " - Slot " + index + " does not exist in this inventory");
                
                ItemStack is = SingleItemSerializer.deserializeItemStack(item);
                contents[index] = is;
            }
            
            return contents;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Set an Inventory from a JSON Array.
     *
     * @param holder The person whose Inventory will be set
     * @param json The JSON Array to deserialize
     */
    public static void setInventory(InventoryHolder holder, JSONArray json) {
        setInventory(holder.getInventory(), json);        
    }

    /**
     * Sets the Inventory using a JSON Array
     *
     * @param inv The Inventory to set
     * @param json The JSON Array to deserialize
     */
    public static void setInventory(Inventory inv, JSONArray json) {
        ItemStack[] items = deserializeInventory(json, inv.getSize());
        inv.clear();
        
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null)
                continue;
            
            inv.setItem(i, item);
        }
    }

    /**
     * Set an inventory using an ItemStack array from a JSON Object.
     *
     * @param player The Player to update
     * @param json The JSON Array of the inventory
     */
    public static void setPlayerInventory(Player player, JSONObject json) {
        try {
            PlayerInventory inventory = player.getInventory();
            ItemStack[] armor = deserializeInventory(json.getJSONArray("armor"), 4);
            
            inventory.clear();
            inventory.setArmorContents(armor);
            setInventory(player, json.getJSONArray("inventory"));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
}
