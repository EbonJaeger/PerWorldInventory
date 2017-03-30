/*
 * Copyright (C) 2014-2015  Erufael
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

package me.gnat008.perworldinventory.data.players;

import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.UUID;

/**
 * This class is used to represent a Player.
 * It contains all of the variables that can be saved, as well as a few things
 * for internal use.
 *
 * A PWIPlayer is meant to be used as a cache, in order to improve performance
 * both when using a MySQL database, and perhaps for flat-file storage as well.
 */
public class PWIPlayer {

    /* INVENTORIES */
    private ItemStack[] armor;
    private ItemStack[] enderChest;
    private ItemStack[] inventory;

    /* PLAYER STATS */
    private boolean canFly;
    private String displayName;
    private float exhaustion;
    private float experience;
    private boolean isFlying;
    private int foodLevel;
    private double maxHealth;
    private double health;
    private GameMode gamemode;
    private int level;
    private float saturationLevel;
    private Collection<PotionEffect> potionEffects;
    private float fallDistance;
    private int fireTicks;
    private int maxAir;
    private int remainingAir;

    /* ECONOMY */
    private double bankBalance;
    private double balance;

    private Location location;
    private UUID uuid;
    private String name;

    /* PERWORLDINVENTORY STUFF */
    private boolean saved;
    private Group group;

    PWIPlayer(Player player, Group group, double bankBalance, double balance) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.location = player.getLocation();

        this.group = group;
        this.saved = false;

        this.armor = player.getInventory().getArmorContents();
        this.enderChest = player.getEnderChest().getContents();
        this.inventory = player.getInventory().getContents();

        this.canFly = player.getAllowFlight();
        this.displayName = player.getDisplayName();
        this.exhaustion = player.getExhaustion();
        this.experience = player.getExp();
        this.isFlying = player.isFlying();
        this.foodLevel = player.getFoodLevel();
        this.maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        this.health = player.getHealth();
        this.gamemode = player.getGameMode();
        this.level = player.getLevel();
        this.saturationLevel = player.getSaturation();
        this.potionEffects = player.getActivePotionEffects();
        this.fallDistance = player.getFallDistance();
        this.fireTicks = player.getFireTicks();
        this.maxAir = player.getMaximumAir();
        this.remainingAir = player.getRemainingAir();

        this.bankBalance = bankBalance;
        this.balance = balance;
    }

    /**
     * Get the armor contents of a player.
     *
     * @return Armor contents
     */
    public ItemStack[] getArmor() {
        return this.armor;
    }

    /**
     * Set the armor contents of a player.
     *
     * @param armor Armor to set
     */
    public void setArmor(ItemStack[] armor) {
        this.armor = armor;
    }

    /**
     * Get the EnderChest contents of a player.
     *
     * @return EnderChest contents
     */
    public ItemStack[] getEnderChest() {
        return this.enderChest;
    }

    /**
     * Set the EnderChest contents of a player.
     *
     * @param enderChest EnderChest contents to set
     */
    public void setEnderChest(ItemStack[] enderChest) {
        this.enderChest = enderChest;
    }

    /**
     * Get the inventory contents of a player.
     *
     * @return Inventory contents
     */
    public ItemStack[] getInventory() {
        return this.inventory;
    }

    /**
     * Set the inventory contents of a player.
     *
     * @param inventory Inventory contents to set
     */
    public void setInventory(ItemStack[] inventory) {
        this.inventory = inventory;
    }

    /**
     * Return whether a player can fly.
     *
     * @return Can fly
     */
    public boolean getCanFly() {
        return this.canFly;
    }

    /**
     * Set a player's ability to fly.
     *
     * @param canFly Can fly
     */
    public void setCanFly(boolean canFly) {
        this.canFly = canFly;
    }

    /**
     * Get a player's display name.
     *
     * @return Display name
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Set a player's display name.
     *
     * @param displayName Display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get a player's exhaustion level.
     *
     * @return Exhaustion
     */
    public float getExhaustion() {
        return this.exhaustion;
    }

    /**
     * Set a player's exhaustion level.
     *
     * @param exhaustion Exhaustion
     */
    public void setExhaustion(float exhaustion) {
        this.exhaustion = exhaustion;
    }

    /**
     * Get a player's experience amount.
     *
     * @return Experience
     */
    public float getExperience() {
        return this.experience;
    }

    /**
     * Set a player's experience amount.
     *
     * @param experience Experience
     */
    public void setExperience(float experience) {
        this.experience = experience;
    }

    /**
     * See if a player is currently flying.
     *
     * @return Flying
     */
    public boolean isFlying() {
        return this.isFlying;
    }

    /**
     * Set if a player is flying.
     *
     * @param flying Flying
     */
    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }

    /**
     * Get a player's food level.
     *
     * @return Food level
     */
    public int getFoodLevel() {
        return this.foodLevel;
    }

    /**
     * Set a player's food level.
     *
     * @param foodLevel Food level
     */
    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    /**
     * Get a player's maximum health.
     *
     * @return Maximum health
     */
    public double getMaxHealth() {
        return maxHealth;
    }

    /**
     * Set a player's maximum health.
     *
     * @param maxHealth Maximum health
     */
    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    /**
     * Get a player's health.
     *
     * @return Health level
     */
    public double getHealth() {
        return health;
    }

    /**
     * Set a player's health.
     *
     * @param health Health level
     */
    public void setHealth(double health) {
        this.health = health;
    }

    /**
     * Get a player's GameMode.
     *
     * @return GameMode
     */
    public GameMode getGamemode() {
        return this.gamemode;
    }

    /**
     * Set a player's GameMode.
     *
     * @param gamemode GameMode
     */
    public void setGamemode(GameMode gamemode) {
        this.gamemode = gamemode;
    }

    /**
     * Get a player's current experience level.
     *
     * @return Level
     */
    public int getLevel() {
        return this.level;
    }

    /**
     * Set a player's current experience level.
     *
     * @param level Level
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Get a player's saturation level.
     *
     * @return Saturation
     */
    public float getSaturationLevel() {
        return this.saturationLevel;
    }

    /**
     * Set a player's saturation level.
     *
     * @param saturationLevel Saturation
     */
    public void setSaturationLevel(float saturationLevel) {
        this.saturationLevel = saturationLevel;
    }

    /**
     * Get a player's active potion effects.
     *
     * @return Active potion effects
     */
    public Collection<PotionEffect> getPotionEffects() {
        return this.potionEffects;
    }

    /**
     * Set a player's active potion effects.
     *
     * @param potionEffects Potion effects
     */
    public void setPotionEffects(Collection<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    /**
     * Get a player's bank balance (economy).
     *
     * @return Bank balance
     */
    public double getBankBalance() {
        return this.bankBalance;
    }

    /**
     * Set a player's bank balance (economy).
     *
     * @param bankBalance Bank balance
     */
    public void setBankBalance(double bankBalance) {
        this.bankBalance = bankBalance;
    }

    /**
     * Get a player's balance (economy).
     *
     * @return Balance
     */
    public double getBalance() {
        return this.balance;
    }

    /**
     * Set a player's balance (economy).
     *
     * @param balance Balance
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * Get a player's UUID.
     *
     * @return UUID
     */
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * Get a player's name.
     *
     * @return Name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the {@link me.gnat008.perworldinventory.groups.Group} that this PWIPlayer
     * instance is storing information for.
     *
     * @return The Group for this PWIPlayer
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Check if the data for this player has been saved to a database or flatfile.
     *
     * @return True if saved
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * Set if this player has been saved to a database or flatfile.
     *
     * @param saved True if saved, false if not
     */
    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    /**
     * Get the location of the player.
     *
     * @return The player's location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Set the player's location
     *
     * @param location The new location
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    public float getFallDistance() {
        return fallDistance;
    }

    public void setFallDistance(float fallDistance) {
        this.fallDistance = fallDistance;
    }

    public int getFireTicks() {
        return fireTicks;
    }

    public void setFireTicks(int fireTicks) {
        this.fireTicks = fireTicks;
    }

    public int getMaxAir() {
        return maxAir;
    }

    public void setMaxAir(int maxAir) {
        this.maxAir = maxAir;
    }

    public int getRemainingAir() {
        return remainingAir;
    }

    public void setRemainingAir(int remainingAir) {
        this.remainingAir = remainingAir;
    }
}
