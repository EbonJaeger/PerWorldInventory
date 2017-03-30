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

import com.google.gson.JsonObject;
import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import javax.inject.Inject;

public class StatSerializer {

    @Inject
    private Settings settings;

    private StatSerializer() {}

    /**
     * Serialize a player's stats
     *
     * @param player The player whose stats to serialize
     * @return The serialized stats
     */
    public static JsonObject serialize(PWIPlayer player) {
        JsonObject root = new JsonObject();

        root.addProperty("can-fly", player.getCanFly());
        root.addProperty("display-name", player.getDisplayName());
        root.addProperty("exhaustion", player.getExhaustion());
        root.addProperty("exp", player.getExperience());
        root.addProperty("flying", player.isFlying());
        root.addProperty("food", player.getFoodLevel());
        root.addProperty("gamemode", player.getGamemode().toString());
        root.addProperty("max-health", player.getMaxHealth());
        root.addProperty("health", player.getHealth());
        root.addProperty("level", player.getLevel());
        root.add("potion-effects", PotionEffectSerializer.serialize(player.getPotionEffects()));
        root.addProperty("saturation", player.getSaturationLevel());
        root.addProperty("fallDistance", player.getFallDistance());
        root.addProperty("fireTicks", player.getFireTicks());
        root.addProperty("maxAir", player.getMaxAir());
        root.addProperty("remainingAir", player.getRemainingAir());

        return root;
    }

    /**
     * Apply stats to a player.
     *
     * @param player The Player to apply the stats to.
     * @param stats  The stats to apply.
     * @param dataFormat See {@link PlayerSerializer#serialize(PWIPlayer)}.
     */
    public void deserialize(Player player,JsonObject stats, int dataFormat) {
        if (settings.getProperty(PwiProperties.LOAD_CAN_FLY) && stats.has("can-fly"))
            player.setAllowFlight(stats.get("can-fly").getAsBoolean());
        if (settings.getProperty(PwiProperties.LOAD_DISPLAY_NAME) && stats.has("display-name"))
            player.setDisplayName(stats.get("display-name").getAsString());
        if (settings.getProperty(PwiProperties.LOAD_EXHAUSTION) && stats.has("exhaustion"))
            player.setExhaustion((float) stats.get("exhaustion").getAsDouble());
        if (settings.getProperty(PwiProperties.LOAD_EXP) && stats.has("exp"))
            player.setExp((float) stats.get("exp").getAsDouble());
        if (settings.getProperty(PwiProperties.LOAD_FLYING) && stats.has("flying"))
            player.setFlying(stats.get("flying").getAsBoolean());
        if (settings.getProperty(PwiProperties.LOAD_HUNGER) && stats.has("food"))
            player.setFoodLevel(stats.get("food").getAsInt());
        if (settings.getProperty(PwiProperties.LOAD_MAX_HEALTH) && stats.has("max-health"))
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(stats.get("max-health").getAsDouble());
        if (settings.getProperty(PwiProperties.LOAD_HEALTH) && stats.has("health")) {
            double health = stats.get("health").getAsDouble();
            if (health <= player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
                player.setHealth(health);
            } else if (health <= 0) {
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            } else {
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            }
        }
        if (settings.getProperty(PwiProperties.LOAD_GAMEMODE) && (!settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)) && stats.has("gamemode")) {
            if (stats.get("gamemode").getAsString().length() > 1) {
                player.setGameMode(GameMode.valueOf(stats.get("gamemode").getAsString()));
            } else {
                int gm = stats.get("gamemode").getAsInt();
                switch (gm) {
                    case 0:
                        player.setGameMode(GameMode.CREATIVE);
                        break;
                    case 1:
                        player.setGameMode(GameMode.SURVIVAL);
                        break;
                    case 2:
                        player.setGameMode(GameMode.ADVENTURE);
                        break;
                    case 3:
                        player.setGameMode(GameMode.SPECTATOR);
                        break;
                }
            }
        }
        if (settings.getProperty(PwiProperties.LOAD_LEVEL) && stats.has("level"))
            player.setLevel(stats.get("level").getAsInt());
        if (settings.getProperty(PwiProperties.LOAD_POTION_EFFECTS) && stats.has("potion-effects")) {
            if (dataFormat < 2) {
                PotionEffectSerializer.setPotionEffects(stats.get("potion-effects").getAsString(), player);
            } else {
                PotionEffectSerializer.setPotionEffects(stats.getAsJsonArray("potion-effects"), player);
            }
        }
        if (settings.getProperty(PwiProperties.LOAD_SATURATION) && stats.has("saturation"))
            player.setSaturation((float) stats.get("saturation").getAsDouble());
        if (settings.getProperty(PwiProperties.LOAD_FALL_DISTANCE) && stats.has("fallDistance"))
            player.setFallDistance(stats.get("fallDistance").getAsFloat());
        if (settings.getProperty(PwiProperties.LOAD_FIRE_TICKS) && stats.has("fireTicks"))
            player.setFireTicks(stats.get("fireTicks").getAsInt());
        if (settings.getProperty(PwiProperties.LOAD_MAX_AIR) && stats.has("maxAir"))
            player.setMaximumAir(stats.get("maxAir").getAsInt());
        if (settings.getProperty(PwiProperties.LOAD_REMAINING_AIR) && stats.has("remainingAir"))
            player.setRemainingAir(stats.get("remainingAir").getAsInt());
    }
}
