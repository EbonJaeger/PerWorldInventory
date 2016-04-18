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

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A class to help with the serialization of PotionEffects. The serialization technique is similar to that
 * of serializing enchantments. The process can be as explained as such:
 * <br/><br/>
 * <pre>
 * String serializedEffects = "";
 * for(PotionEffect e : effects){
 *     serializedEffects += e.getType().getId() + ":" + e.getDuration() + ":" + e.getAmplifier() + ";";
 * }
 * </pre>
 * <br/>
 * So that it would follow this pattern:<br/>
 * <pre>id:duration:amplifier;...</pre>
 */
public class PotionEffectSerializer {

    protected PotionEffectSerializer() {}

    /**
     * Serialize a Collection of PotionEffects into a string that follows the regex
     * <pre>([0-9]+:[0-9]+:[0-9]+;)+</pre>
     *
     * @param effects The PotionEffects to serialize
     * @return The serialized PotionEffects
     */
    public static String serialize(Collection<PotionEffect> effects) {
        String serialized = "";
        for (PotionEffect e : effects) {
            serialized += e.getType().getId() + ":" + e.getDuration() + ":" + e.getAmplifier() + ";";
        }
        return serialized;
    }

    /**
     * Get a Collection of PotionEffects from the given potion effect code
     *
     * @param serializedEffects The potion effect code to decode from
     * @return A Collection of PotionEffects from the given potion effect code
     */
    public static Collection<PotionEffect> deserialize(String serializedEffects) {
        ArrayList<PotionEffect> effects = new ArrayList<>();
        if (serializedEffects.isEmpty())
            return effects;
        String[] effs = serializedEffects.split(";");
        for (int i = 0; i < effs.length; i++) {
            String[] effect = effs[i].split(":");
            if (effect.length < 3)
                throw new IllegalArgumentException(serializedEffects + " - PotionEffect " + i + " (" + effs[i] + "): split must at least have a length of 3");
            if (DeprecatedMethodUtil.isNum(effect[0]))
                throw new IllegalArgumentException(serializedEffects + " - PotionEffect " + i + " (" + effs[i] + "): id is not an integer");
            if (DeprecatedMethodUtil.isNum(effect[1]))
                throw new IllegalArgumentException(serializedEffects + " - PotionEffect " + i + " (" + effs[i] + "): duration is not an integer");
            if (DeprecatedMethodUtil.isNum(effect[2]))
                throw new IllegalArgumentException(serializedEffects + " - PotionEffect " + i + " (" + effs[i] + "): amplifier is not an integer");
            int id = Integer.parseInt(effect[0]);
            int duration = Integer.parseInt(effect[1]);
            int amplifier = Integer.parseInt(effect[2]);
            PotionEffectType effectType = PotionEffectType.getById(id);
            if (effectType == null)
                throw new IllegalArgumentException(serializedEffects + " - PotionEffect " + i + " (" + effs[i] + "): no PotionEffectType with id of " + id);
            PotionEffect e = new PotionEffect(effectType, duration, amplifier);
            effects.add(e);
        }
        return effects;
    }

    /**
     * Add the given PotionEffects to a LivingEntity
     *
     * @param code   The PotionEffects to add
     * @param entity The entity to add the PotionEffects
     */
    public static void addPotionEffects(String code, LivingEntity entity) {
        entity.addPotionEffects(deserialize(code));
    }

    /**
     * Remove any current PotionEffects from a LivingEntity then add the given effects
     *
     * @param code   The PotionEffects to add
     * @param entity The entity to set the PotionEffects
     */
    public static void setPotionEffects(String code, LivingEntity entity) {
        for (PotionEffect effect : entity.getActivePotionEffects()) {
            entity.removePotionEffect(effect.getType());
        }
        addPotionEffects(code, entity);
    }
}
