package me.gnat008.perworldinventory.commands;

import com.google.gson.JsonObject;
import me.gnat008.perworldinventory.DataFolder;
import me.gnat008.perworldinventory.PwiLogger;
import me.gnat008.perworldinventory.data.players.PWIPlayerFactory;
import me.gnat008.perworldinventory.data.serializers.PlayerSerializer;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.permission.AdminPermission;
import me.gnat008.perworldinventory.permission.PermissionNode;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static me.gnat008.perworldinventory.utils.FileUtils.readData;
import static me.gnat008.perworldinventory.utils.FileUtils.writeData;

public class SetWorldDefaultCommand implements ExecutableCommand {

    @Inject
    private GroupManager groupManager;
    @Inject
    private PlayerSerializer playerSerializer;
    @Inject
    private PWIPlayerFactory playerFactory;
    @Inject
    @DataFolder
    private File dataFolder;

    @Override
    public void executeCommand(CommandSender sender, List<String> args) {
        // Check if player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "This command may only be run from ingame.");
            return;
        }

        Player player = (Player) sender;

        // Check args
        Group group;
        if (args.size() == 1) {
            String name = args.get(0);
            group = name.equalsIgnoreCase("serverDefault") ? new Group("__default", null, null) : groupManager.getGroup(name);
        } else if (args.isEmpty()) {
            try {
                group = groupManager.getGroupFromWorld(player.getWorld().getName());
            } catch (IllegalArgumentException ex) {
                player.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "You are not standing in a valid world!");
                group = null;
            }
        } else {
            group = null;
            player.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "Incorrect number of arguments! See "
                    + ChatColor.WHITE + "/pwi help" + ChatColor.GRAY + " for usage.");
        }

        if (group != null) {
            setGroupDefault(player, group);
        }
    }

    /**
     * Set the default inventory loadout for a group. This is the inventory that will
     * be given to a player the first time they enter a world in the group.
     * <p>
     * A snapshot of the player will be taken and saved to a temp file to be deleted after.
     * This is so some stats are set to max, e.g. health. The snapshot will be restored to
     * the player after the default loadout has been saved.
     *
     * @param player The player performing the command.
     * @param group The group to write the defaults for.
     */
    private void setGroupDefault(Player player, Group group) {
        File file = new File(dataFolder + File.separator + "defaults", group.getName() + ".json");
        if (!file.exists()) {
            player.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY + "Default file for this group not found!");
            return;
        }

        File tmp = new File(getUserFolder(player.getUniqueId()), "tmp.json");
        try {
            tmp.getParentFile().mkdirs();
            tmp.createNewFile();
        } catch (IOException ex) {
            player.sendMessage(ChatColor.DARK_RED + "» " + ChatColor.GRAY +  "Could not create temporary file! Aborting!");
            return;
        }
        Group tempGroup = new Group("tmp", null, null);
        writeData(tmp, playerSerializer.serialize(playerFactory.create(player, tempGroup)));

        player.setFoodLevel(20);
        player.setHealth(player.getMaxHealth());
        player.setSaturation(20);
        player.setTotalExperience(0);
        player.setRemainingAir(player.getMaximumAir());
        player.setFireTicks(0);

        writeData(file, playerSerializer.serialize(playerFactory.create(player, group)));

        JsonObject data;
        try {
            data = readData(tmp);
            playerSerializer.deserialize(data, player);
            tmp.delete();
            player.sendMessage(ChatColor.BLUE + "» " + ChatColor.GRAY +  "Defaults for '" + group.getName() + "' set!");
        } catch (IOException ex) {
            player.sendMessage(ChatColor.RED + "» " + ChatColor.GRAY +  "Something went wrong while restoring your inventory! Check the console.");
            PwiLogger.severe("Unable to restore inventory for '" + player.getName() + "':", ex);
        }
    }

    /**
     * Return the folder in which data is stored for the player.
     *
     * @param uuid The player's UUID
     * @return The data folder of the player
     */
    private File getUserFolder(UUID uuid) {
        return new File(dataFolder, uuid.toString());
    }

    @Override
    public PermissionNode getRequiredPermission() {
        return AdminPermission.SETDEFAULTS;
    }
}
