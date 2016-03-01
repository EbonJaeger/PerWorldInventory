/*
 * Copyright (C) 2014-2016  Gnat008
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
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.defaults.ConfigValues;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.database.Database;
import me.gnat008.perworldinventory.database.Operator;
import me.gnat008.perworldinventory.groups.Group;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.json.JSONArray;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SQLSerializer extends DataSerializer {

    private final Database database;
    private String[] tableNames;

    private final String PREFIX;

    public SQLSerializer(PerWorldInventory plugin) {
        super(plugin);

        this.database = plugin.getSQLDatabase();
        this.PREFIX = ConfigValues.PREFIX.getString();
        this.tableNames = new String[5];
        this.tableNames[0] = PREFIX + "ender_chests";
        this.tableNames[1] = PREFIX + "armor";
        this.tableNames[2] = PREFIX + "inventory";
        this.tableNames[3] = PREFIX + "player_stats";
        this.tableNames[4] = PREFIX + "econ";
    }

    /**
     * Save a player's data to a SQL database.
     * <p>
     * The method will check if data for the given player, group, and gamemode
     * already exists. If data does exist, it will delete the existing data,
     * and then re-insert it.
     * <p>
     * Everything is saved to the database, regardless of configuration settings.
     * All config checking will be done on data retrieval.
     *
     * @param group The {@link me.gnat008.perworldinventory.groups.Group} to save for
     * @param gamemode The {@link org.bukkit.GameMode} to save for
     * @param player The {@link me.gnat008.perworldinventory.data.players.PWIPlayer} we are saving
     */
    public void saveToDatabase(final Group group, final GameMode gamemode, final PWIPlayer player) {
        String findDataQuery = database.createQuery().select("data_uuid").from(PREFIX + "player_data").where("uuid", Operator.EQUAL)
                .and("data_group", Operator.EQUAL).and("gamemode", Operator.EQUAL).limit(1).buildQuery();
        PreparedStatement findData;
        try {
            findData = database.prepareStatement(findDataQuery);
            findData.setString(1, player.getUuid().toString().replace("-", ""));
            findData.setString(2, group.getName());
            findData.setString(3, gamemode.toString().toLowerCase());

            database.queryDb(findData, new Database.Callback<ResultSet>() {
                @Override
                public void onSuccess(ResultSet done) {
                    try {
                        if (done.next()) {
                            plugin.getLogger().info(done.getString("data_uuid"));
                            deleteExistingData(done.getString("data_uuid"));
                            createStatements(done.getString("data_uuid"), player, group, gamemode, false);
                        }
                        else {
                            plugin.getLogger().info("No data found!");
                            createStatements(player, group, gamemode, true);
                        }

                    } catch (SQLException ex) {
                        plugin.getLogger().severe("Error 100: Unable to check for existing data for a player: " + ex.getMessage());
                    }
                }

                @Override
                public void onFailure(Throwable cause) {
                    plugin.getLogger().severe("Error 101: Unable to update database: " + cause.getMessage());
                }
            });
        } catch (ClassNotFoundException | SQLException ex) {
            plugin.getLogger().severe("Error 102: Unable to check for existing data for a player: " + ex.getMessage());
        }
    }

    /**
     * Delete existing rows from the database.
     * <p>
     * This is called when data already exists, so we can just
     * re-insert the changed data afterwards.
     *
     * @param uuid The UUID of the data to delete
     */
    private void deleteExistingData(String uuid) {
        final PreparedStatement deleteEnderChest, deleteArmor, deleteInventory, deleteStats, deleteEcon;

        String deleteECQuery = database.createQuery().delete().from(PREFIX + "ender_chests").where("data_uuid", Operator.EQUAL).buildQuery();
        String deleteArmorQuery = database.createQuery().delete().from(PREFIX + "armor").where("data_uuid", Operator.EQUAL).buildQuery();
        String deleteInvQuery = database.createQuery().delete().from(PREFIX + "inventory").where("data_uuid", Operator.EQUAL).buildQuery();
        String deleteStatsQuery = database.createQuery().delete().from(PREFIX + "player_stats").where("data_uuid", Operator.EQUAL).buildQuery();
        String deleteEconQuery = database.createQuery().delete().from(PREFIX + "econ").where("data_uuid", Operator.EQUAL).buildQuery();

        try {
            deleteEnderChest = database.prepareStatement(deleteECQuery);
            deleteEnderChest.setString(1, uuid);
            deleteArmor = database.prepareStatement(deleteArmorQuery);
            deleteArmor.setString(1, uuid);
            deleteInventory = database.prepareStatement(deleteInvQuery);
            deleteInventory.setString(1, uuid);
            deleteStats = database.prepareStatement(deleteStatsQuery);
            deleteStats.setString(1, uuid);
            deleteEcon = database.prepareStatement(deleteEconQuery);
            deleteEcon.setString(1, uuid);
        } catch (ClassNotFoundException | SQLException ex) {
            plugin.getLogger().severe("Unable to delete database rows: " + ex.getMessage());
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    database.updateDb(deleteEnderChest);
                    database.updateDb(deleteArmor);
                    database.updateDb(deleteInventory);
                    database.updateDb(deleteStats);
                    database.updateDb(deleteEcon);
                } catch (ClassNotFoundException | SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the {@link java.sql.PreparedStatement}s to write a
     * player's data to the database. This method saves everything
     * regardless of the configuration options.
     * <p>
     * This method creates a new, random UUID for the data.
     *
     * @param player The {@link me.gnat008.perworldinventory.data.players.PWIPlayer} to grab data from
     * @param group The {@link me.gnat008.perworldinventory.groups.Group} the player is in
     * @param gamemode The {@link org.bukkit.GameMode} we are saving
     * @param createIndexEntry If an index entry needs to be created
     */
    private void createStatements(PWIPlayer player, Group group, GameMode gamemode, boolean createIndexEntry) {
        String dataUUID = UUID.randomUUID().toString().replace("-", "");

        createStatements(dataUUID, player, group, gamemode, createIndexEntry);
    }

    /**
     * Create the {@link PreparedStatement}s to write a
     * player's data to the database. This method saves everything
     * regardless of the configuration options.
     *
     * @param dataUUID The UUID to use to tie data together
     * @param player The {@link PWIPlayer} to grab data from
     * @param group The {@link Group} the player is in
     * @param gamemode The {@link GameMode} we are saving
     * @param createIndexEntry If an index entry needs to be created
     */
    private void createStatements(String dataUUID, PWIPlayer player, Group group, GameMode gamemode, boolean createIndexEntry) {
        Set<PreparedStatement> statements = new HashSet<>();
        if (createIndexEntry)
            statements.add(createIndexEntry(dataUUID, player.getUuid().toString(), group, gamemode));
        PreparedStatement insEnderChest, insArmor, insInv, insStats, insEcon;
        String insEnderChestQuery, insArmorQuery, insInvQuery, insStatsQuery, insEconQuery = null;
        insEnderChestQuery = database.createQuery().insertInto(PREFIX + "ender_chests").values(3).buildQuery();
        insArmorQuery = database.createQuery().insertInto(PREFIX + "armor").values(3).buildQuery();
        insInvQuery = database.createQuery().insertInto(PREFIX + "inventory").values(3).buildQuery();
        insStatsQuery = database.createQuery().insertInto(PREFIX + "player_stats").values(12).buildQuery();
        if (ConfigValues.ECONOMY.getBoolean())
            insEconQuery = database.createQuery().insertInto(PREFIX + "econ").values(4).buildQuery();

        try {
            insEnderChest = database.prepareStatement(insEnderChestQuery);
            insEnderChest.setNull(1, Types.INTEGER);
            insEnderChest.setString(2, dataUUID);
            Blob ec = new SerialBlob(InventorySerialization.serializeInventory(player.getEnderChest()).toString().getBytes());
            insEnderChest.setBlob(3, ec);
            statements.add(insEnderChest);

            insArmor = database.prepareStatement(insArmorQuery);
            insArmor.setNull(1, Types.INTEGER);
            insArmor.setString(2, dataUUID);
            Blob armor = new SerialBlob(InventorySerialization.serializeInventory(player.getArmor()).toString().getBytes());
            insArmor.setBlob(3, armor);
            statements.add(insArmor);

            insInv = database.prepareStatement(insInvQuery);
            insInv.setNull(1, Types.INTEGER);
            insInv.setString(2, dataUUID);
            Blob inv = new SerialBlob(InventorySerialization.serializeInventory(player.getInventory()).toString().getBytes());
            insInv.setBlob(3, inv);
            statements.add(insInv);

            insStats = database.prepareStatement(insStatsQuery);
            insStats.setNull(1, Types.INTEGER);
            insStats.setString(2, dataUUID);
            insStats.setBoolean(3, player.getCanFly());
            insStats.setString(4, player.getDisplayName());
            insStats.setFloat(5, player.getExhaustion());
            insStats.setFloat(6, player.getExperience());
            insStats.setBoolean(7, player.isFlying());
            insStats.setInt(8, player.getFoodLevel());
            insStats.setDouble(9, player.getHealth());
            insStats.setInt(10, player.getLevel());
            insStats.setBytes(11, PotionEffectSerialization.serializeEffects(player.getPotionEffects()).getBytes());
            insStats.setFloat(12, player.getSaturationLevel());
            statements.add(insStats);

            if (ConfigValues.ECONOMY.getBoolean() && insEconQuery != null) {
                insEcon = database.prepareStatement(insEconQuery);
                insEcon.setNull(1, Types.INTEGER);
                insEcon.setString(2, dataUUID);
                insEcon.setDouble(3, player.getBankBalance());
                insEcon.setDouble(4, player.getBalance());
                statements.add(insEcon);
            }

            updateDatabase(statements);
        } catch (ClassNotFoundException | SQLException ex) {
            plugin.getLogger().severe("There was an error inserting stats for '" + player.getName() + "': " + ex.getMessage());
        }
    }

    private PreparedStatement createIndexEntry(String dataUUID, String playerUUID, Group group, GameMode gamemode) {
        String query = database.createQuery().insertInto(PREFIX + "player_data").values(5).buildQuery();
        try {
            PreparedStatement statement = database.prepareStatement(query);
            statement.setNull(1, Types.INTEGER);
            statement.setString(2, playerUUID.replace("-", ""));
            statement.setString(3, group.getName());
            statement.setString(4, gamemode.toString().toLowerCase());
            statement.setString(5, dataUUID);
            return statement;
        } catch (ClassNotFoundException | SQLException ex) {
            plugin.getLogger().severe("There was an error creating a player index entry: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Executes a bunch of {@link java.sql.PreparedStatement}s asynchronously.
     *
     * @param statements The PreparedStatements to execute
     */
    private void updateDatabase(final Set<PreparedStatement> statements) {
        for (final PreparedStatement statement : statements) {
            try {
                database.updateDb(statement);
            } catch (ClassNotFoundException | SQLException ex) {
                plugin.getLogger().severe("Unable to update database: " + ex.getMessage());
            }
        }
    }

    public void getFromDatabase(final Group group, final GameMode gamemode, final Player player) {
        String dataUUID = getDataUUID(group, gamemode, player);
        if (dataUUID == null) {
            FileSerializer fs = new FileSerializer(plugin);
            fs.getFromDatabase(group, gamemode, player);
            return;
        }

        String query = database.createQuery().select("*").from(tableNames).where("data_uuid", Operator.EQUAL).buildQuery();
        PreparedStatement statement;
        try {
            statement = database.prepareStatement(query);
            statement.setString(1, dataUUID);

            final ConcurrentHashMap<String, Object> results = new ConcurrentHashMap<>();
            database.queryDb(statement, new Database.Callback<ResultSet>() {
                @Override
                public void onSuccess(ResultSet done) {
                    try {
                        if (done.isBeforeFirst()) {
                            while (done.next()) {
                                ResultSetMetaData meta = done.getMetaData();
                                int columnCount = meta.getColumnCount();
                                for (int i = 1; i < columnCount; i++) {
                                    results.put(meta.getColumnName(i), done.getObject(i));
                                }
                            }
                        } else {
                            FileSerializer fs = new FileSerializer(plugin);
                            fs.getFromDatabase(group, gamemode, player);
                        }
                    } catch (SQLException ex) {
                        plugin.getLogger().severe("Unable to read data for player '" + player.getName() + "': " + ex.getMessage());
                    }
                }

                @Override
                public void onFailure(Throwable cause) {
                    plugin.getLogger().severe("Unable to read data for player '" + player.getName() + "': " + cause.getMessage());
                }
            });

            setPlayer(results, player, gamemode);
        } catch (ClassNotFoundException | SQLException ex) {
            plugin.getLogger().severe("Unable to get existing data for player '" + player.getName() + "': " + ex.getMessage());
        }
    }

    private String getDataUUID(Group group, GameMode gamemode, final Player player) {
        String dataUUID = database.createQuery().select("data_uuid").from(PREFIX + "player_data").where("uuid", Operator.EQUAL)
                .and("data_group", Operator.EQUAL).and("gamemode", Operator.EQUAL).limit(1).buildQuery();
        final PreparedStatement findData;
        try {
            findData = database.prepareStatement(dataUUID);
            findData.setString(1, player.getUniqueId().toString().replace("-", ""));
            findData.setString(2, group.getName());
            findData.setString(3, gamemode.toString().toLowerCase());
            plugin.getLogger().info(findData.toString());

            final ConcurrentHashMap<String, String> result = new ConcurrentHashMap<>();
            database.queryDb(findData, new Database.Callback<ResultSet>() {
                @Override
                public void onSuccess(ResultSet done) {
                    try {
                        if (done.next())
                            result.put("data_uuid", done.getString("data_uuid"));
                    } catch (SQLException ex) {
                        plugin.getLogger().severe("Unable to read data: " + ex.getMessage());
                    }
                }

                @Override
                public void onFailure(Throwable cause) {
                    plugin.getLogger().severe("Unable to get existing data for player '" + player.getName() + "': " + cause.getMessage());
                }
            });

            if (result.containsKey("data_uuid")) {
                return result.get("data_uuid");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            plugin.getLogger().severe("Unable to get existing data for player '" + player.getName() + "': " + ex.getMessage());
        }

        return null;
    }

    private void setPlayer(ConcurrentHashMap data, Player player, GameMode gameMode) {
        if (ConfigValues.ENDER_CHEST.getBoolean()) {
            String ecBlob = (String) data.get("ender_chest");
            player.getEnderChest().setContents(InventorySerialization.getInventory(new JSONArray(ecBlob), 27, 1));
        }
        if (ConfigValues.INVENTORY.getBoolean()) {
            String invBlob = (String) data.get("items");
            player.getInventory().setContents(InventorySerialization.getInventory(new JSONArray(invBlob), 27, 1));

            String armorBlob = (String) data.get("armor_items");
            player.getInventory().setArmorContents(InventorySerialization.getInventory(new JSONArray(armorBlob), 4, 1));
        }
        if (ConfigValues.STATS.getBoolean()) {
            if (ConfigValues.CAN_FLY.getBoolean()) {
                boolean canFly = (byte) data.get("can_fly") == 1;
                player.setAllowFlight(canFly);
            }
            if (ConfigValues.DISPLAY_NAME.getBoolean())
                player.setDisplayName((String) data.get("display_name"));
            if (ConfigValues.EXHAUSTION.getBoolean())
                player.setExhaustion((float) data.get("exhaustion"));
            if (ConfigValues.EXP.getBoolean())
                player.setExp((float) data.get("exp"));
            if (ConfigValues.FLYING.getBoolean())
                player.setFlying((byte) data.get("flying") == 1);
            if (ConfigValues.FOOD.getBoolean())
                player.setFoodLevel((int) data.get("food"));
            if (ConfigValues.HEALTH.getBoolean()) {
                double health = (double) data.get("health");
                if (health < player.getMaxHealth())
                    player.setHealth(health);
                else
                    player.setHealth(player.getMaxHealth());
            }
            if (ConfigValues.GAMEMODE.getBoolean() && (!ConfigValues.SEPARATE_GAMEMODE_INVENTORIES.getBoolean()))
                player.setGameMode(gameMode);
            if (ConfigValues.LEVEL.getBoolean())
                player.setLevel((int) data.get("level"));
            if (ConfigValues.POTION_EFFECTS.getBoolean()) {
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                String blob = (String) data.get("potion_effects");
                Collection<PotionEffect> effects = PotionEffectSerialization.getPotionEffects(blob);
                player.addPotionEffects(effects);
            }
            if (ConfigValues.SATURATION.getBoolean())
                player.setSaturation((int) data.get("saturation"));
        }
        if (ConfigValues.ECONOMY.getBoolean()) {
            Economy econ = plugin.getEconomy();
            econ.bankWithdraw(player.getName(), econ.bankBalance(player.getName()).balance);
            econ.bankDeposit(player.getName(), (double) data.get("bank_balance"));

            econ.withdrawPlayer(player, econ.getBalance(player));
            econ.depositPlayer(player, (double) data.get("balance"));
        }
    }
}
