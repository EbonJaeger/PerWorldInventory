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

import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentSerializer {
    
    protected EnchantmentSerializer() {}

    /**
     * Serialize a Map of enchantments into a String that 
     * follows the regex
     * 
     * <pre>
     *     ([0-9]+:[0-9]+;)+ 
     * </pre> 
     *
     * @param enchantments The Map to serialize
     * @return The serialized enchantments
     */
    public static String serializeEnchantments(Map<Enchantment, Integer> enchantments) {
        String serialized = "";
        for (Enchantment e : enchantments.keySet()) {
            serialized += e.getId() + ":" + enchantments.get(e) + ";";
        }
        
        return serialized;
    }
    
    
    public static Map<Enchantment, Integer> deserializeEnchantments(String serialized) {
        HashMap<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
        
        if (serialized.isEmpty()) {
            return enchantments;
        }
        
        String[] enchants = serialized.split(";");
        for (int i = 0; i < enchants.length; i++) {
            String[] enchant = enchants[i].split(":");

            if (enchant.length < 2)
                throw new IllegalArgumentException(serialized + " - Enchantment " + i + " (" + enchants[i] + "): split must at least have a length of 2");
            if (!Util.isNumber(enchant[0]))
                throw new IllegalArgumentException(serialized + " - Enchantment " + i + " (" + enchants[i] + "): id is not an integer");
            if (!Util.isNumber(enchant[1]))
                throw new IllegalArgumentException(serialized + " - Enchantment " + i + " (" + enchants[i] + "): id is not an integer");
            
            int id = Integer.parseInt(enchant[0]);
            int level = Integer.parseInt(enchant[1]);
            Enchantment e = Enchantment.getById(id);
            
            if (e == null)
                throw new IllegalArgumentException(serialized + " - Enchantment " + i + " (" + enchants[i] + "): no Enchantment with id of " + id);
            
            enchantments.put(e, level);
        }
        
        return enchantments;
    }
}
