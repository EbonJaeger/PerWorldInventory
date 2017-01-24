package me.gnat008.perworldinventory.config;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SectionComments;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import java.util.HashMap;
import java.util.Map;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Holds the properties of PerWorldInventory.
 */
public final class PwiProperties implements SettingsHolder {

    @Comment({
        "If true, the server will change player's gamemodes when entering a world",
        "The gamemode set is configured in the worlds.yml file"})
    public static final Property<Boolean> MANAGE_GAMEMODES =
            newProperty("manage-gamemodes", false);

    @Comment("If true, players will have different inventories for each gamemode")
    public static final Property<Boolean> SEPARATE_GAMEMODE_INVENTORIES =
            newProperty("separate-gamemode-inventories", true);

    @Comment("If true, any worlds that are not in the worlds.yml configuration file will share the same inventory")
    public static final Property<Boolean> SHARE_IF_UNCONFIGURED =
            newProperty("share-if-unconfigured", false);

    @Comment("Save and load players' economy balances. Requires Vault!")
    public static final Property<Boolean> USE_ECONOMY =
            newProperty("player.economy", false);

    @Comment("Load players' ender chests")
    public static final Property<Boolean> LOAD_ENDER_CHESTS =
            newProperty("player.ender-chest", true);

    @Comment("Load players' inventory")
    public static final Property<Boolean> LOAD_INVENTORY =
            newProperty("player.inventory", true);

    @Comment("Load if a player is able to fly")
    public static final Property<Boolean> LOAD_CAN_FLY =
            newProperty("player.stats.can-fly", true);

    @Comment("Load the player's display name")
    public static final Property<Boolean> LOAD_DISPLAY_NAME =
            newProperty("player.stats.display-name", false);

    @Comment("Load a player's exhaustion level")
    public static final Property<Boolean> LOAD_EXHAUSTION =
            newProperty("player.stats.exhaustion", true);

    @Comment("Load how much exp a player has")
    public static final Property<Boolean> LOAD_EXP =
            newProperty("player.stats.exp", true);

    @Comment("Load a player's hunger level")
    public static final Property<Boolean> LOAD_HUNGER =
            newProperty("player.stats.food", true);

    @Comment("Load if a player is flying")
    public static final Property<Boolean> LOAD_FLYING =
            newProperty("player.stats.flying", true);

    @Comment({
        "Load what gamemode a player is in. This is shadow-set to false if",
        "'manage-gamemodes' is true, to stop infinite loop"})
    public static final Property<Boolean> LOAD_GAMEMODE =
            newProperty("player.stats.gamemode", false);

    @Comment("Load how much health a player has")
    public static final Property<Boolean> LOAD_HEALTH =
            newProperty("player.stats.health", true);

    @Comment("Load what level the player is")
    public static final Property<Boolean> LOAD_LEVEL =
            newProperty("player.stats.level", true);


    @Comment("Load all the potion effects of the player")
    public static final Property<Boolean> LOAD_POTION_EFFECTS =
            newProperty("player.stats.potion-effects", true);

    @Comment("Load the saturation level of the player")
    public static final Property<Boolean> LOAD_SATURATION =
            newProperty("player.stats.saturation", true);

    @Comment("Load a player's fall distance")
    public static final Property<Boolean> LOAD_FALL_DISTANCE =
            newProperty("player.stats.fall-distance", true);

    @Comment("Load the fire ticks a player has")
    public static final Property<Boolean> LOAD_FIRE_TICKS =
            newProperty("player.stats.fire-ticks", true);

    @Comment("Load the maximum amount of air a player can have")
    public static final Property<Boolean> LOAD_MAX_AIR =
            newProperty("player.stats.max-air", true);

    @Comment("Load the current remaining air a player has")
    public static final Property<Boolean> LOAD_REMAINING_AIR =
            newProperty("player.stats.remaining-air", true);

    @Comment({
        "Configure the amount of time between saves, in seconds",
        "Default is 5 minutes (300 seconds)"})
    public static final Property<Integer> SAVE_INTERVAL =
            newProperty("save-interval", 300);

    @Comment({
        "Attempt to figure out which world a player last logged off in",
        "and save/load the correct data if that world is different.",
        "REQUIRES MC 1.9.2 OR NEWER"})
    public static final Property<Boolean> LOAD_DATA_ON_JOIN =
            newProperty("load-data-on-join", false);

    @Comment("Print out debug messages to the console for every event that happens in PWI")
    public static final Property<Boolean> DEBUG_MODE =
            newProperty("debug-mode", false);

    @Comment({
        "Disables bypass regardless of permission",
        "Defaults to false"})
    public static final Property<Boolean> DISABLE_BYPASS =
            newProperty("disable-bypass", false);

    private PwiProperties() {
    }

    @SectionComments
    public static Map<String, String[]> buildSectionComments() {
        Map<String, String[]> comments = new HashMap<>();
        comments.put("player", new String[]{"All settings for players are here:"});
        comments.put("player.stats", new String[]{"All options for player stats are here:"});
        return comments;
    }
}
