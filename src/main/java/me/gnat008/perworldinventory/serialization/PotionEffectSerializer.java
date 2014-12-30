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

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

public class PotionEffectSerializer {
    
    protected PotionEffectSerializer() {}

    /**
     * Serialize a Collection of PotionEffects into a String with the regex:
     * 
     * <pre>
     * ([0-9]+:[0-9]+:[0-9]+;)+
     * </pre> 
     *  
     * @param effects The effects to serialize
     * @return The serialized String of effects
     */
    public static String serializePotionEffects(Collection<PotionEffect> effects) {
        String serialized = "";
         for (PotionEffect e : effects) {
             serialized += e.getType().getId() + ":" + e.getDuration() + ":" + e.getAmplifier() + ";";
         }
        
        return serialized;
    }

    /**
     * Get a List of PotionEffects from a serialized String.
     *
     * @param serializedEffects The String to deserialize
     * @return The deserialized PotionEffects
     */
    public static Collection<PotionEffect> deserializePotionEffects(String serializedEffects) {
        ArrayList<PotionEffect> effects = new ArrayList<>();
        if (serializedEffects.isEmpty())
            return effects;
        
        String[] effs = serializedEffects.split(";");
        for (int i = 0; i < effs.length; i++) {
            String[] effect = effs[i].split(":");
            if (effect.length < 3) {
                throw new IllegalArgumentException(serializedEffects + " - PotionEffect " + i + " (" + effs[i] +
                        "): split must at least have a length of 3");
            }
            
            if (!Util.isNumber(effect[0])) {
                throw new IllegalArgumentException(serializedEffects + " - PotionEffect " + i + " (" + effs[i] + 
                        "): id is not an integer");
            }
            if (!Util.isNumber(effect[1])) {
                throw new IllegalArgumentException(serializedEffects + " - PotionEffect " + i + " (" + effs[i] +
                        "): duration is not an integer");
            }
            if (!Util.isNumber(effect[2])) {
                throw new IllegalArgumentException(serializedEffects + " - PotionEffect " + i + " (" + effs[i] +
                        "): amplifier is not an integer");
            }
            
            int id = Integer.parseInt(effect[0]);
            int duration = Integer.parseInt(effect[1]);
            int amplifier = Integer.parseInt(effect[2]);
            
            PotionEffectType type = PotionEffectType.getById(id);
            if (type == null) {
                throw new IllegalArgumentException(serializedEffects + " - PotionEffect " + i + " (" + effs[i] + 
                        "): no PotionEffectType with id of " + id);
            }
            
            PotionEffect e = new PotionEffect(type, duration, amplifier);
            effects.add(e);
        }
        
        return effects;
    }

    /**
     * Add PotionEffects to an entity.
     *
     * @param json Serialized PotionEffects to set
     * @param entity Entity to add PotionEffects to
     */
    public static void addPotionEffects(String json, LivingEntity entity) {
        entity.addPotionEffects(deserializePotionEffects(json));        
    }

    /**
     * Set potion effects on an entity after removing all active effects.
     *  
     * @param json Serialized PotionEffects to set
     * @param entity Entity to set PotionEffects on
     */
    public static void setPotionEffects(String json, LivingEntity entity) {
        for (PotionEffect e : entity.getActivePotionEffects()) {
            entity.removePotionEffect(e.getType());
        }
        
        addPotionEffects(json, entity);
    }
}
