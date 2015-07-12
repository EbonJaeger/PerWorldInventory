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
import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.MultiInvAPI;
import uk.co.tggl.pluckerpluck.multiinv.api.MIAPIPlayer;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
        plugin.getLogger().info("Beginning data conversion. This may take awhile...");
        MultiverseInventories mvinventories = (MultiverseInventories) plugin.getServer().getPluginManager().getPlugin("Multiverse-Inventories");
        List<WorldGroupProfile> mvgroups = mvinventories.getGroupManager().getGroups();

        for (WorldGroupProfile mvgroup : mvgroups) {
            for (OfflinePlayer player1 : Bukkit.getOfflinePlayers()) {
                try {
                    for (GameMode gamemode : GameMode.values()) {
                        PlayerProfile playerData = null;
                        switch (gamemode) {
                            case SURVIVAL:
                                playerData = mvgroup.getPlayerData(ProfileTypes.SURVIVAL, player1);
                                break;
                            case ADVENTURE:
                                playerData = mvgroup.getPlayerData(ProfileTypes.ADVENTURE, player1);
                                break;
                            case SPECTATOR:
                            case CREATIVE:
                                playerData = mvgroup.getPlayerData(ProfileTypes.CREATIVE, player1);
                                break;
                        }

                        if (playerData != null) {
                            JSONObject writable = serializeMVIToNewFormat(playerData);
                            Group group = plugin.getGroupManager().getGroup(mvgroup.getName());
                            if (group == null) {
                                group = new Group(mvgroup.getName(), null, null);
                            }
                            plugin.getSerializer().writePlayerDataToFile(player1, writable, group, gamemode);
                        }
                    }
                } catch (Exception ex) {
                    plugin.getLogger().warning("Error importing inventory for player '" + player1.getName() +
                            "' for group '" + mvgroup.getName() + "': " + ex.getMessage());
                }
            }
        }

        plugin.getLogger().info("Data conversion complete! Disabling Multiverse-Inventories...");
        plugin.getServer().getPluginManager().disablePlugin(mvinventories);
        plugin.getLogger().info("Multiverse-Inventories disabled! Don't forget to remove the .jar!");
    }

    public void convertMultiInvData() {
        plugin.getLogger().info("Beginning data conversion. This may take awhile...");
        MultiInv multiinv = (MultiInv) plugin.getServer().getPluginManager().getPlugin("MultiInv");
        MultiInvAPI mvAPI = new MultiInvAPI(multiinv);

        for (String groupName : mvAPI.getGroups().values()) {
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                try {
                    for (GameMode gamemode : GameMode.values()) {
                        MIAPIPlayer player = null;
                        switch (gamemode) {
                            case SURVIVAL:
                                player = mvAPI.getPlayerInstance(offlinePlayer, groupName, gamemode);
                                break;
                            case ADVENTURE:
                                player = mvAPI.getPlayerInstance(offlinePlayer, groupName, gamemode);
                                break;
                            case SPECTATOR:
                            case CREATIVE:
                                player = mvAPI.getPlayerInstance(offlinePlayer, groupName, GameMode.CREATIVE);
                                break;
                        }

                        if (player != null) {
                            Group group = plugin.getGroupManager().getGroup(groupName);
                            if (group == null) {
                                group = new Group(groupName, null, null);
                            }

                            plugin.getSerializer().writePlayerDataToFile(
                                    offlinePlayer,
                                    serializeMIToNewFormat(player),
                                    group,
                                    gamemode);
                        }
                    }
                } catch (Exception ex) {
                    plugin.getLogger().warning("Error importing inventory for player '" + offlinePlayer.getName() +
                            "' for group '" + groupName + "': " + ex.getMessage());
                }
            }
        }

        plugin.getLogger().info("Data conversion complete! Disabling MultiInv...");
        plugin.getServer().getPluginManager().disablePlugin(multiinv);
        plugin.getLogger().info("MultiInv disabled! Don't forget to remove the .jar!");
    }

    private JSONObject serializeMVIToNewFormat(PlayerProfile data) {
        JSONObject root = new JSONObject();
        root.put("data-format", 1);

        JSONObject inv = new JSONObject();
        if (data.get(Sharables.ENDER_CHEST) != null) {
            JSONArray enderchest = InventorySerialization.serializeInventory(data.get(Sharables.ENDER_CHEST));
            root.put("ender-chest", enderchest);
        }
        if (data.get(Sharables.INVENTORY) != null) {
            JSONArray inventory = InventorySerialization.serializeInventory(data.get(Sharables.INVENTORY));
            inv.put("inventory", inventory);
        }
        if (data.get(Sharables.ARMOR) != null) {
            JSONArray armor = InventorySerialization.serializeInventory(data.get(Sharables.ARMOR));
            inv.put("armor", armor);
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

        if (data.get(Sharables.ECONOMY) != null) {
            JSONObject econ = new JSONObject();
            econ.put("balance", data.get(Sharables.ECONOMY));
            root.put("economy", econ);
        }

        root.put("inventory", inv);
        root.put("stats", stats);

        return root;
    }

    private JSONObject serializeMIToNewFormat(MIAPIPlayer player) {
        JSONObject root = new JSONObject();
        root.put("data-format", 1);

        if (player.getEnderchest() != null) {
            JSONArray enderChest = new JSONArray();
            List<ItemStack> items = new ArrayList<>();
            for (MIItemStack item : player.getEnderchest().getInventoryContents()) {
                if (item == null || item.getItemStack() == null) {
                    items.add(new ItemStack(Material.AIR));
                } else {
                    items.add(item.getItemStack());
                }
            }
            enderChest.put(InventorySerialization.serializeInventory((ItemStack[]) items.toArray()));
            root.put("ender-chest", enderChest);
        }

        JSONObject inventory = new JSONObject();

        List<ItemStack> items = new ArrayList<>();
        for (MIItemStack item : player.getInventory().getInventoryContents()) {
            if (item == null || item.getItemStack() == null) {
                items.add(new ItemStack(Material.AIR));
            } else {
                items.add(item.getItemStack());
            }
        }
        JSONArray inv = InventorySerialization.serializeInventory((ItemStack[]) items.toArray());
        inventory.put("inventory", inv);

        List<ItemStack> armorList = new ArrayList<>();
        for (MIItemStack item : player.getInventory().getArmorContents()) {
            if (item == null || item.getItemStack() == null) {
                items.add(new ItemStack(Material.AIR));
            } else {
                armorList.add(item.getItemStack());
            }
        }
        JSONArray armor = InventorySerialization.serializeInventory((ItemStack[]) armorList.toArray());
        inventory.put("armor", armor);

        JSONObject stats = new JSONObject();
        stats.put("exp", player.getXp());
        stats.put("food", player.getFoodlevel());
        stats.put("gamemode", player.getGm().toString());
        stats.put("health", player.getHealth());
        stats.put("level", player.getXpLevel());
        stats.put("saturation", player.getSaturation());

        root.put("inventory", inventory);
        root.put("stats", stats);

        return root;
    }
}
