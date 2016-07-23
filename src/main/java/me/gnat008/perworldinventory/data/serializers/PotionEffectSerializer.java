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
import org.bukkit.Color;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A class to help with the serialization of PotionEffects.
 */
public class PotionEffectSerializer {

    protected PotionEffectSerializer() {}

    /**
     * Serialize a Collection of PotionEffects into a JsonArray of JsonObjects. Each
     * JsonObject contains the type, amplifier, duration and color of a potion effect.
     * The color is saved in the RGB format.
     *
     * @param effects The PotionEffects to serialize
     * @return A JsonArray of JsonObjects of serialized PotionEffects.
     */
    public static JsonArray serialize(Collection<PotionEffect> effects) {
        JsonArray all = new JsonArray();

        for (PotionEffect effect : effects) {
            JsonObject pot = new JsonObject();
            pot.addProperty("type", effect.getType().getName());
            pot.addProperty("amp", effect.getAmplifier());
            pot.addProperty("duration", effect.getDuration());
            pot.addProperty("ambient", effect.isAmbient());
            pot.addProperty("particles", effect.hasParticles());
            if (effect.getColor() != null) {
                pot.addProperty("color", effect.getColor().asRGB());
            }

            all.add(pot);
        }

        return all;
    }

    /**
     * Get a Collection of PotionEffects from the given potion effect code
     *
     * @param serializedEffects The potion effect code to decode from
     * @return A Collection of PotionEffects from the given potion effect code
     *
     * @deprecated Uses deprecated methods that may be removed. Additionally, may not have all the data.
     */
    @Deprecated
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
     * Return a deserialized Collection of PotionEffects from a given JsonArray.
     *
     * @param serialized The serialized PotionEffects in a JsonArray.
     * @return The PotionEffects.
     */
    public static Collection<PotionEffect> deserialize(JsonArray serialized) {
        ArrayList<PotionEffect> effects = new ArrayList<>();
        for (int i = 0; i < serialized.size(); i++) {
            JsonObject s_effect = serialized.get(i).getAsJsonObject();
            PotionEffect effect;

            PotionEffectType type = PotionEffectType.getByName(s_effect.get("type").getAsString());
            int amplifier = s_effect.get("amp").getAsInt();
            int duration = s_effect.get("duration").getAsInt();
            boolean ambient = s_effect.get("ambient").getAsBoolean();
            boolean particles = s_effect.get("particles").getAsBoolean();

            if (s_effect.has("color") && particles) {
                Color color = Color.fromRGB(s_effect.get("color").getAsInt());
                effect = new PotionEffect(type, duration, amplifier, ambient, particles, color);
            } else {
                effect = new PotionEffect(type, duration, amplifier, ambient, particles);
            }

            effects.add(effect);
        }

        return effects;
    }

    /**
     * Add the given PotionEffects to a LivingEntity.
     *
     * @param code   The PotionEffects to add.
     * @param entity The entity to add the PotionEffects.
     *
     * @deprecated Magic numbers.
     */
    @Deprecated
    public static void addPotionEffects(String code, LivingEntity entity) {
        entity.addPotionEffects(deserialize(code));
    }

    /**
     * Deserialize the given PotionEffects and apply them to a LivingEntity.
     *
     * @param effects The PotionEffects to add.
     * @param entity The entity to apply the effects to.
     */
    public static void addPotionEffects(JsonArray effects, LivingEntity entity) {
        entity.addPotionEffects(deserialize(effects));
    }

    /**
     * Remove any current PotionEffects from a LivingEntity then add the given effects.
     *
     * @param code   The PotionEffects to add.
     * @param entity The entity to set the PotionEffects.
     *
     * @deprecated Magic numbers.
     */
    @Deprecated
    public static void setPotionEffects(String code, LivingEntity entity) {
        if (entity.getActivePotionEffects() != null && !entity.getActivePotionEffects().isEmpty()) {
            for (PotionEffect effect : entity.getActivePotionEffects()) {
                entity.removePotionEffect(effect.getType());
            }
        }

        addPotionEffects(code, entity);
    }

    /**
     * Remove any PotionEffects the entity currently has, then apply the new effects.
     *
     * @param effects The PotionEffects to apply.
     * @param entity The entity to apply the effects to.
     */
    public static void setPotionEffects(JsonArray effects, LivingEntity entity) {
        if (entity.getActivePotionEffects() != null && !entity.getActivePotionEffects().isEmpty()) {
            for (PotionEffect effect : entity.getActivePotionEffects()) {
                entity.removePotionEffect(effect.getType());
            }
        }

        addPotionEffects(effects, entity);
    }
}
