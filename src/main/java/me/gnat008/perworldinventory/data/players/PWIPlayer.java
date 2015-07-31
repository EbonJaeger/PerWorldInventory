package me.gnat008.perworldinventory.data.players;

import me.gnat008.perworldinventory.PerWorldInventory;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

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
    private GameMode gamemode;
    private int level;
    private float saturationLevel;
    private Collection<PotionEffect> potionEffects;

    /* ECONOMY */
    private double bankBalance;
    private double balance;

    private UUID uuid;
    private String name;

    /* PERWORLDINVENTORY STUFF */
    private File dataFileDirectory;

    public PWIPlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.dataFileDirectory = new File(PerWorldInventory.getInstance().getDataFolder() + File.separator + "data" + File.separator + uuid.toString());
        if (!dataFileDirectory.exists()) {
            try {
                dataFileDirectory.createNewFile();
            } catch (IOException ex) {
                PerWorldInventory.getInstance().getLogger().warning("Unable to create data directory for '" + name + "'!");
            }
        }

        this.armor = player.getInventory().getArmorContents();
        this.enderChest = player.getEnderChest().getContents();
        this.inventory = player.getInventory().getContents();

        this.canFly = player.getAllowFlight();
        this.displayName = player.getDisplayName();
        this.exhaustion = player.getExhaustion();
        this.experience = player.getTotalExperience();
        this.isFlying = player.isFlying();
        this.foodLevel = player.getFoodLevel();
        this.gamemode = player.getGameMode();
        this.level = player.getLevel();
        this.saturationLevel = player.getSaturation();
        this.potionEffects = player.getActivePotionEffects();

        if (PerWorldInventory.getInstance().getEconomy() != null) {
            this.bankBalance = PerWorldInventory.getInstance().getEconomy().bankBalance(name).balance;
            this.balance = PerWorldInventory.getInstance().getEconomy().getBalance(player);
        }
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
}
