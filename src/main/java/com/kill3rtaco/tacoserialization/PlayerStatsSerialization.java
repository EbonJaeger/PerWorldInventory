package com.kill3rtaco.tacoserialization;

import me.gnat008.perworldinventory.config.defaults.ConfigValues;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to help with the serialization of player stats, like exp level and health.
 * <br/><br/>
 * This serialization class supports optional serialization.<br/>
 * TacoSerialization will create a folder in your server plugins directory (wherever that may be) called
 * 'TacoSerialization'. Inside the folder will be a config.yml file. Various values can be turned off to
 * prevent some keys from being generated.
 *
 * @author KILL3RTACO
 * @since 1.0
 */
public class PlayerStatsSerialization {

    protected PlayerStatsSerialization() {
    }

    /**
     * Serialize a player's stats
     *
     * @param player The player whose stats to serialize
     * @return The serialized stats
     */
    public static JSONObject serializePlayerStats(PWIPlayer player) {
        try {
            JSONObject root = new JSONObject();
            if (ConfigValues.CAN_FLY.getBoolean())
                root.put("can-fly", player.getCanFly());
            if (ConfigValues.DISPLAY_NAME.getBoolean())
                root.put("display-name", player.getDisplayName());
            if (ConfigValues.EXHAUSTION.getBoolean())
                root.put("exhaustion", player.getExhaustion());
            if (ConfigValues.EXP.getBoolean())
                root.put("exp", player.getExperience());
            if (ConfigValues.FLYING.getBoolean())
                root.put("flying", player.isFlying());
            if (ConfigValues.FOOD.getBoolean())
                root.put("food", player.getFoodLevel());
            if (ConfigValues.GAMEMODE.getBoolean())
                root.put("gamemode", player.getGamemode().toString());
            if (ConfigValues.HEALTH.getBoolean())
                root.put("health", player.getHealth());
            if (ConfigValues.LEVEL.getBoolean())
                root.put("level", player.getLevel());
            if (ConfigValues.POTION_EFFECTS.getBoolean())
                root.put("potion-effects", PotionEffectSerialization.serializeEffects(player.getPotionEffects()));
            if (ConfigValues.SATURATION.getBoolean())
                root.put("saturation", player.getSaturationLevel());
            return root;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Apply stats to a player
     *
     * @param player The player to affect
     * @param stats  The stats to apply
     */
    public static void applyPlayerStats(Player player, JSONObject stats) {
        try {
            if (ConfigValues.CAN_FLY.getBoolean() && stats.has("can-fly"))
                player.setAllowFlight(stats.getBoolean("can-fly"));
            if (ConfigValues.DISPLAY_NAME.getBoolean() && stats.has("display-name"))
                player.setDisplayName(stats.getString("display-name"));
            if (ConfigValues.EXHAUSTION.getBoolean() && stats.has("exhaustion"))
                player.setExhaustion((float) stats.getDouble("exhaustion"));
            if (ConfigValues.EXP.getBoolean() && stats.has("exp"))
                player.setExp((float) stats.getDouble("exp"));
            if (ConfigValues.FLYING.getBoolean() && stats.has("flying"))
                player.setFlying(stats.getBoolean("flying"));
            if (ConfigValues.FOOD.getBoolean() && stats.has("food"))
                player.setFoodLevel(stats.getInt("food"));
            if (ConfigValues.HEALTH.getBoolean() && stats.has("health"))
                player.setHealth(stats.getDouble("health"));
            if (ConfigValues.GAMEMODE.getBoolean() && (!ConfigValues.SEPARATE_GAMEMODE_INVENTORIES.getBoolean()) && stats.has("gamemode")) {
                if (stats.get("gamemode") instanceof String) {
                    player.setGameMode(GameMode.valueOf(stats.getString("gamemode")));
                } else {
                    int gm = stats.getInt("gamemode");
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
            if (ConfigValues.LEVEL.getBoolean() && stats.has("level"))
                player.setLevel(stats.getInt("level"));
            if (ConfigValues.POTION_EFFECTS.getBoolean() && stats.has("potion-effects"))
                PotionEffectSerialization.setPotionEffects(stats.getString("potion-effects"), player);
            if (ConfigValues.SATURATION.getBoolean() && stats.has("saturation"))
                player.setSaturation((float) stats.getDouble("saturation"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
