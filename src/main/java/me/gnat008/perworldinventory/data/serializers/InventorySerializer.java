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
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.inject.Inject;

public class InventorySerializer {

    @Inject
    private ItemSerializer itemSerializer;

    InventorySerializer() {}

    /**
     * Serialize a PlayerInventory. This will save the armor contents of the inventory as well
     *
     * @param player The player to serialize
     * @return A JsonObject representing the serialized Inventory.
     */
    public JsonObject serializePlayerInventory(PWIPlayer player) {
        JsonObject root = new JsonObject();
        JsonArray inventory = serializeInventory(player.getInventory());
        JsonArray armor = serializeInventory(player.getArmor());

        root.add("inventory", inventory);
        root.add("armor", armor);

        return root;
    }

    /**
     * Serialize an ItemStack array.
     *
     * @param contents The items in the inventory
     * @return A JsonArray representing the serialized ItemStack array
     */
    public JsonArray serializeInventory(ItemStack[] contents) {
        JsonArray inventory = new JsonArray();

        for (int i = 0; i < contents.length; i++) {
            JsonObject values = itemSerializer.serializeInventoryItem(contents[i], i);
            if (values != null)
                inventory.add(values);
        }

        return inventory;
    }

    /**
     * Sets the Inventory using an ItemStack array constructed from a JsonObject.
     *
     * @param player The InventoryHolder to which the Inventory will be set
     * @param inv    The reference JsonArray
     * @param format Data format being used; 0 is old, 1 is new
     */
    public void setInventory(Player player, JsonObject inv, int format) {
        PlayerInventory inventory = player.getInventory();
        
        ItemStack[] armor = deserializeInventory(inv.getAsJsonArray("armor"), 4, format);
        ItemStack[] inventoryContents = deserializeInventory(inv.getAsJsonArray("inventory"), inventory.getSize(), format);

        inventory.clear();
        if (armor != null) {
        	inventory.setArmorContents(armor);
        }
        
        if (inventoryContents != null) {
        	inventory.setContents(inventoryContents);
        }
       
    }

    /**
     * Gets an ItemStack array from a JsonObject.
     *
     * @param inv  The JsonObject to get from
     * @param size The expected size of the inventory, can be greater than expected
     * @param format Data format being used; 0 is old, 1 is new
     * @return An ItemStack array constructed from the given JsonArray
     */
    public ItemStack[] deserializeInventory(JsonArray inv, int size, int format) {
    	// Be tolerant if the expected JsonArray tag is missing
    	if (inv == null) {
    		return null;
    	}
    	
        ItemStack[] contents = new ItemStack[size];
        for (int i = 0; i < inv.size(); i++) {
        	// We don't want to risk failing to deserialize a players inventory. Try your best
        	// to deserialize as much as possible.
        	try {
	            JsonObject item = inv.get(i).getAsJsonObject();
	            int index = item.get("index").getAsInt();
	            
	            ItemStack is;
	            if (format >= 1)
	                is = itemSerializer.deserializeItem(item);
	            else
	                is = itemSerializer.getItem(item);
	
	            contents[index] = is;
        	}
        	catch (Exception ex) {
        		ex.printStackTrace();
        	}
        }

        return contents;
    }
}
