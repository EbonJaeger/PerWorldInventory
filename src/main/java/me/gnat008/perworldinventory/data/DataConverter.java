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

package me.gnat008.perworldinventory.data;

import com.kill3rtaco.tacoserialization.InventorySerialization;
import com.kill3rtaco.tacoserialization.PotionEffectSerialization;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.ProfileTypes;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import me.gnat008.perworldinventory.PerWorldInventory;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.potion.PotionEffect;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class DataConverter {

    private PerWorldInventory plugin;

    private static DataConverter converter = null;

    private DataConverter(PerWorldInventory plugin) {
        this.plugin = plugin;
    }

    public static DataConverter getInstance(PerWorldInventory plugin) {
        if (converter == null) {
            converter = new DataConverter(plugin);
        }

        return converter;
    }

    public static void disable() {
        converter = null;
    }

    public void convertMultiVerseData() {
        plugin.getPrinter().printToConsole("Beginning data conversion. This may take awhile...", false);
        MultiverseInventories mvinventories = (MultiverseInventories) plugin.getServer().getPluginManager().getPlugin("Multiverse-Inventories");
        List<WorldGroupProfile> mvgroups = mvinventories.getGroupManager().getGroups();

        for (WorldGroupProfile mvgroup : mvgroups) {
            for (OfflinePlayer player1 : Bukkit.getOfflinePlayers()) {
                try {
                    PlayerProfile playerData = mvgroup.getPlayerData(ProfileTypes.SURVIVAL, player1);
                    if (playerData != null) {
                        JSONObject writable = getAndSerializeToNewFormat(playerData);
                        plugin.getSerializer().writePlayerDataToFile(player1, writable, mvgroup.getName());
                    }
                } catch (Exception ex) {
                    plugin.getPrinter().printToConsole("Error importing inventory for player: " + player1.getName() +
                            " For group: " + mvgroup.getName(), true);
                    ex.printStackTrace();
                }
            }
        }

        plugin.getPrinter().printToConsole("Data conversion complete! Disabling Multiverse-Inventories...", false);
        plugin.getServer().getPluginManager().disablePlugin(mvinventories);
        plugin.getPrinter().printToConsole("Multiverse-Inventories disabled! Don't forget to remove the .jar!", false);
    }

    private JSONObject getAndSerializeToNewFormat(PlayerProfile data) {
        JSONObject root = new JSONObject();

        if (data.get(Sharables.INVENTORY) != null) {
            JSONArray inventory = InventorySerialization.serializeInventory(data.get(Sharables.INVENTORY));
            root.put("inventory", inventory);
        }
        if (data.get(Sharables.ARMOR) != null) {
            JSONArray armor = InventorySerialization.serializeInventory(data.get(Sharables.ARMOR));
            root.put("armor", armor);
        }

        JSONObject stats = new JSONObject();
        if (data.get(Sharables.EXHAUSTION) != null)
            stats.put("exhaustion", data.get(Sharables.EXHAUSTION));
        if (data.get(Sharables.EXPERIENCE) != null)
            stats.put("exp", data.get(Sharables.EXPERIENCE));
        if (data.get(Sharables.FOOD_LEVEL) != null)
            stats.put("food", data.get(Sharables.FOOD_LEVEL));
        if (data.get(Sharables.HEALTH) != null)
            stats.put("health", data.get(Sharables.HEALTH));
        if (data.get(Sharables.LEVEL) != null)
            stats.put("level", data.get(Sharables.LEVEL));
        if (data.get(Sharables.POTIONS) != null) {
            PotionEffect[] effects = data.get(Sharables.POTIONS);
            Collection<PotionEffect> potionEffects = new LinkedList<>();
            for (PotionEffect effect : effects) {
                potionEffects.add(effect);
            }
            stats.put("potion-effects", PotionEffectSerialization.serializeEffects(potionEffects));
        }
        if (data.get(Sharables.SATURATION) != null)
            stats.put("saturation", data.get(Sharables.SATURATION));

        root.put("stats", stats);

        return root;
    }
}
