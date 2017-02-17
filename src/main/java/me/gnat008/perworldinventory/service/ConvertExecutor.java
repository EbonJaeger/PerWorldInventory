package me.gnat008.perworldinventory.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.ProfileTypes;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import me.gnat008.perworldinventory.DataFolder;
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.data.serializers.InventorySerializer;
import me.gnat008.perworldinventory.data.serializers.PotionEffectSerializer;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;

import javax.inject.Inject;
import java.io.File;
import java.util.*;

import static me.gnat008.perworldinventory.util.FileUtils.getFile;
import static me.gnat008.perworldinventory.util.FileUtils.writeData;

/**
 * Performs converting operations.
 */
public class ConvertExecutor {

    private final File FILE_PATH;

    private BukkitService bukkitService;
    private GroupManager groupManager;
    private PluginManager pluginManager;
    private InventorySerializer inventorySerializer;

    @Inject
    ConvertExecutor(BukkitService bukkitService,
                    GroupManager groupManager, PluginManager pluginManager,
                    InventorySerializer inventorySerializer, @DataFolder File dataFolder) {
        this.bukkitService = bukkitService;
        this.groupManager = groupManager;
        this.pluginManager = pluginManager;
        this.inventorySerializer = inventorySerializer;
        this.FILE_PATH = new File(dataFolder, "data");
    }

    /**
     * Converts data from the MultiVerse-Inventories format to the PWI format.
     *
     * @param offlinePlayers The players to convert.
     */
    public void executeConvert(Collection<OfflinePlayer> offlinePlayers) {
        MultiverseInventories mvinventories = (MultiverseInventories) pluginManager.getPlugin("Multiverse-Inventories");
        List<WorldGroupProfile> mvgroups = mvinventories.getGroupManager().getGroups();

        for (WorldGroupProfile mvgroup : mvgroups) {
            ProfileType[] MV_PROFILETYPES = { ProfileTypes.SURVIVAL, ProfileTypes.CREATIVE, ProfileTypes.ADVENTURE };

            for (ProfileType profileType : MV_PROFILETYPES) {
                GameMode gameMode = GameMode.valueOf(profileType.getName());

                for (OfflinePlayer player : offlinePlayers) {
                    try {
                        PlayerProfile playerData = mvgroup.getPlayerData(profileType, player);
                        if (playerData != null) {
                            String data = convertFormat(playerData);

                            File file = getFile(getUserFolder(player.getUniqueId()),
                                    gameMode, groupManager.getGroup(mvgroup.getName()));

                            if (!file.getParentFile().exists())
                                file.getParentFile().mkdir();
                            if (!file.exists())
                                file.createNewFile();

                            writeData(file, data);
                        }
                    } catch (Exception ex) {
                        PwiLogger.warning("Error importing inventory for player: " + player.getName() +
                                " For group: " + mvgroup.getName() + " For GameMode: " + gameMode.name(), ex);
                    }
                }
            }
        }
    }

    /**
     * Return the folder in which data is stored for the player.
     *
     * @param uuid The player's UUID
     * @return The data folder of the player
     */
    private File getUserFolder(UUID uuid) {
        return new File(FILE_PATH, uuid.toString());
    }

    private String convertFormat(PlayerProfile data) {
        Gson gson = new Gson();
        JsonObject root = new JsonObject();
        root.addProperty("data-format", 2);

        JsonObject inv = new JsonObject();
        if (data.get(Sharables.INVENTORY) != null) {
            JsonArray inventory = inventorySerializer.serializeInventory(data.get(Sharables.INVENTORY));
            inv.add("inventory", inventory);
        }
        if (data.get(Sharables.ARMOR) != null) {
            JsonArray armor = inventorySerializer.serializeInventory(data.get(Sharables.ARMOR));
            inv.add("armor", armor);
        }
        if (data.get(Sharables.ENDER_CHEST) != null) {
            JsonArray enderChest = inventorySerializer.serializeInventory(data.get(Sharables.ENDER_CHEST));
            root.add("ender-chest", enderChest);
        }

        JsonObject stats = new JsonObject();
        if (data.get(Sharables.EXHAUSTION) != null)
            stats.addProperty("exhaustion", data.get(Sharables.EXHAUSTION));
        if (data.get(Sharables.EXPERIENCE) != null)
            stats.addProperty("exp", data.get(Sharables.EXPERIENCE));
        if (data.get(Sharables.FOOD_LEVEL) != null)
            stats.addProperty("food", data.get(Sharables.FOOD_LEVEL));
        if (data.get(Sharables.HEALTH) != null)
            stats.addProperty("health", data.get(Sharables.HEALTH));
        if (data.get(Sharables.LEVEL) != null)
            stats.addProperty("level", data.get(Sharables.LEVEL));
        if (data.get(Sharables.POTIONS) != null) {
            PotionEffect[] effects = data.get(Sharables.POTIONS);
            Collection<PotionEffect> potionEffects = new LinkedList<>();
            Collections.addAll(potionEffects, effects);
            stats.add("potion-effects", PotionEffectSerializer.serialize(potionEffects));
        }
        if (data.get(Sharables.SATURATION) != null)
            stats.addProperty("saturation", data.get(Sharables.SATURATION));
        if (data.get(Sharables.FALL_DISTANCE) != null)
            stats.addProperty("fallDistance", data.get(Sharables.FALL_DISTANCE));
        if (data.get(Sharables.FIRE_TICKS) != null)
            stats.addProperty("fireTicks", data.get(Sharables.FIRE_TICKS));
        if (data.get(Sharables.MAXIMUM_AIR) != null)
            stats.addProperty("maxAir", data.get(Sharables.MAXIMUM_AIR));
        if (data.get(Sharables.REMAINING_AIR) != null)
            stats.addProperty("remainingAir", data.get(Sharables.REMAINING_AIR));

        root.add("inventory", inv);
        root.add("stats", stats);

        if (data.get(Sharables.ECONOMY) != null) {
            JsonObject econData = new JsonObject();
            econData.addProperty("balance", data.get(Sharables.ECONOMY));
            root.add("economy", econData);
        }

        return gson.toJson(root);
    }
}
