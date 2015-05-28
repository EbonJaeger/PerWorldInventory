package com.kill3rtaco.tacoserialization;

import me.gnat008.perworldinventory.PerWorldInventory;
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
    public static JSONObject serializePlayerStats(Player player, PerWorldInventory plugin) {
        try {
            JSONObject root = new JSONObject();
            if (shouldSerialize("can-fly", plugin))
                root.put("can-fly", player.getAllowFlight());
            if (shouldSerialize("display-name", plugin))
                root.put("display-name", player.getDisplayName());
            if (shouldSerialize("exhaustion", plugin))
                root.put("exhaustion", player.getExhaustion());
            if (shouldSerialize("exp", plugin))
                root.put("exp", player.getExp());
            if (shouldSerialize("flying", plugin))
                root.put("flying", player.isFlying());
            if (shouldSerialize("food", plugin))
                root.put("food", player.getFoodLevel());
            if (shouldSerialize("gamemode", plugin))
                root.put("gamemode", player.getGameMode().toString());
            if (shouldSerialize("health", plugin))
                root.put("health", player.getHealthScale());
            if (shouldSerialize("level", plugin))
                root.put("level", player.getLevel());
            if (shouldSerialize("potion-effects", plugin))
                root.put("potion-effects", PotionEffectSerialization.serializeEffects(player.getActivePotionEffects()));
            if (shouldSerialize("saturation", plugin))
                root.put("saturation", player.getSaturation());
            return root;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Serialize a player's stats as a string
     *
     * @param player The player whose stats to serialize
     * @return The serialization string
     */
    public static String serializePlayerStatsAsString(Player player, PerWorldInventory plugin) {
        return serializePlayerStatsAsString(player, false, plugin);
    }

    /**
     * Serialize a player's stats as a string
     *
     * @param player The player whose stats to serialize
     * @param pretty Whether the resulting string should be 'pretty' or not
     * @return The serialization string
     */
    public static String serializePlayerStatsAsString(Player player, boolean pretty, PerWorldInventory plugin) {
        return serializePlayerStatsAsString(player, pretty, 5, plugin);
    }

    /**
     * Serialize a player's stats as a string
     *
     * @param player       The player whose stats to serialize
     * @param pretty       Whether the resulting string should be 'pretty' or not
     * @param indentFactor The amount of spaces in a tab
     * @return The serialization string
     */
    public static String serializePlayerStatsAsString(Player player, boolean pretty, int indentFactor, PerWorldInventory plugin) {
        try {
            if (pretty) {
                return serializePlayerStats(player, plugin).toString(indentFactor);
            } else {
                return serializePlayerStats(player, plugin).toString();
            }
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
    public static void applyPlayerStats(Player player, String stats) {
        try {
            applyPlayerStats(player, new JSONObject(stats));
        } catch (JSONException e) {
            e.printStackTrace();
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
            if (stats.has("can-fly"))
                player.setAllowFlight(stats.getBoolean("can-fly"));
            if (stats.has("display-name"))
                player.setDisplayName(stats.getString("display-name"));
            if (stats.has("exhaustion"))
                player.setExhaustion((float) stats.getDouble("exhaustion"));
            if (stats.has("exp"))
                player.setExp((float) stats.getDouble("exp"));
            if (stats.has("flying"))
                player.setFlying(stats.getBoolean("flying"));
            if (stats.has("food"))
                player.setFoodLevel(stats.getInt("food"));
            if (stats.has("health"))
                player.setHealth(stats.getDouble("health"));
            if (stats.has("gamemode")) {
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
            if (stats.has("level"))
                player.setLevel(stats.getInt("level"));
            if (stats.has("potion-effects"))
                PotionEffectSerialization.setPotionEffects(stats.getString("potion-effects"), player);
            if (stats.has("saturation"))
                player.setSaturation((float) stats.getDouble("saturation"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test if a certain key should be serialized
     *
     * @param key The key to test
     * @return Whether the key should be serilaized or not
     */
    public static boolean shouldSerialize(String key, PerWorldInventory plugin) {
        if (key.equalsIgnoreCase("gamemode") && plugin.getConfigManager().getConfig("config").getBoolean("separate-gamemode-inventories"))
            return false;
        return plugin.getConfigManager().getShouldSerialize("player-stats." + key);
    }

}
