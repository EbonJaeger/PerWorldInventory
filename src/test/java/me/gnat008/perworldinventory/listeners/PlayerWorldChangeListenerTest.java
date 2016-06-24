package me.gnat008.perworldinventory.listeners;

import me.gnat008.perworldinventory.config.SettingsMocker;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.listeners.player.PlayerChangedWorldListener;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PlayerPermission;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test for {@link PlayerChangedWorldListener}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PlayerWorldChangeListenerTest {

    @InjectMocks
    private PlayerChangedWorldListener listener;

    @Mock
    private GroupManager groupManager;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private PWIPlayerManager playerManager;

    @Test
    public void shouldBypass() {
        // given
        World worldFrom = mock(World.class);
        given(worldFrom.getName()).willReturn("world");
        World worldTo = mock(World.class);
        given(worldTo.getName()).willReturn("new-world");

        Player player = mock(Player.class);
        given(player.getWorld()).willReturn(worldTo);

        PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(player, worldFrom);

        Group groupFrom = getTestGroup(worldFrom.getName(), worldFrom.getName());
        given(groupManager.getGroupFromWorld(worldFrom.getName())).willReturn(groupFrom);
        Group groupTo = getTestGroup(worldTo.getName(), worldTo.getName());
        given(groupManager.getGroupFromWorld(worldTo.getName())).willReturn(groupTo);

        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(true);

        SettingsMocker.create().set("separate-gamemode-inventories", true).set("disable-bypass", false).save();

        // when
        listener.onPlayerChangeWorld(event);

        // then
        verify(playerManager).addPlayer(player, groupFrom);
        verify(playerManager, never()).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class));
    }

    @Test
    public void shouldNotBypassBecauseNoPermission() {
        // given
        World worldFrom = mock(World.class);
        given(worldFrom.getName()).willReturn("world");
        World worldTo = mock(World.class);
        given(worldTo.getName()).willReturn("new-world");

        Player player = mock(Player.class);
        given(player.getWorld()).willReturn(worldTo);

        PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(player, worldFrom);

        Group groupFrom = getTestGroup(worldFrom.getName(), worldFrom.getName());
        given(groupManager.getGroupFromWorld(worldFrom.getName())).willReturn(groupFrom);
        Group groupTo = getTestGroup(worldTo.getName(), worldTo.getName());
        given(groupManager.getGroupFromWorld(worldTo.getName())).willReturn(groupTo);

        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(false);

        SettingsMocker.create().set("separate-gamemode-inventories", true).set("disable-bypass", false).save();

        // when
        listener.onPlayerChangeWorld(event);

        // then
        verify(playerManager).addPlayer(player, groupFrom);
        verify(playerManager).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class));
    }

    @Test
    public void shouldNotBypassBecauseBypassDisabled() {
        // given
        World worldFrom = mock(World.class);
        given(worldFrom.getName()).willReturn("world");
        World worldTo = mock(World.class);
        given(worldTo.getName()).willReturn("new-world");

        Player player = mock(Player.class);
        given(player.getWorld()).willReturn(worldTo);

        PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(player, worldFrom);

        Group groupFrom = getTestGroup(worldFrom.getName(), worldFrom.getName());
        given(groupManager.getGroupFromWorld(worldFrom.getName())).willReturn(groupFrom);
        Group groupTo = getTestGroup(worldTo.getName(), worldTo.getName());
        given(groupManager.getGroupFromWorld(worldTo.getName())).willReturn(groupTo);

        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(true);

        SettingsMocker.create().set("separate-gamemode-inventories", true).set("disable-bypass", true).save();

        // when
        listener.onPlayerChangeWorld(event);

        // then
        verify(playerManager).addPlayer(player, groupFrom);
        verify(playerManager).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class));
    }

    @Test
    public void shouldNotChangeInventoryIfWorldUnconfigured() {
        // given
        World worldFrom = mock(World.class);
        given(worldFrom.getName()).willReturn("world");
        World worldTo = mock(World.class);
        given(worldTo.getName()).willReturn("new-world");

        Player player = mock(Player.class);
        given(player.getWorld()).willReturn(worldTo);

        PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(player, worldFrom);

        Group groupFrom = getTestGroup(worldFrom.getName(), worldFrom.getName());
        given(groupManager.getGroupFromWorld(worldFrom.getName())).willReturn(groupFrom);
        Group groupTo = getTestGroup("__unconfigured__", worldTo.getName());
        given(groupManager.getGroupFromWorld(worldTo.getName())).willReturn(groupTo);

        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(false);

        SettingsMocker.create()
                .set("separate-gamemode-inventories", true)
                .set("disable-bypass", false)
                .set("share-if-unconfigured", true)
                .save();

        // when
        listener.onPlayerChangeWorld(event);

        // then
        verify(playerManager).addPlayer(player, groupFrom);
        verify(playerManager, never()).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class));
    }

    @Test
    public void shouldChangeInventoryIfWorldUnconfigured() {
        // given
        World worldFrom = mock(World.class);
        given(worldFrom.getName()).willReturn("world");
        World worldTo = mock(World.class);
        given(worldTo.getName()).willReturn("new-world");

        Player player = mock(Player.class);
        given(player.getWorld()).willReturn(worldTo);

        PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(player, worldFrom);

        Group groupFrom = getTestGroup(worldFrom.getName(), worldFrom.getName());
        given(groupManager.getGroupFromWorld(worldFrom.getName())).willReturn(groupFrom);
        Group groupTo = getTestGroup("__unconfigured__", worldTo.getName());
        given(groupManager.getGroupFromWorld(worldTo.getName())).willReturn(groupTo);

        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(false);

        SettingsMocker.create()
                .set("separate-gamemode-inventories", true)
                .set("disable-bypass", false)
                .set("share-if-unconfigured", false)
                .save();

        // when
        listener.onPlayerChangeWorld(event);

        // then
        verify(playerManager).addPlayer(player, groupFrom);
        verify(playerManager).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class));
    }

    @Test
    public void shouldChangeInventory() {
        // given
        World worldFrom = mock(World.class);
        given(worldFrom.getName()).willReturn("world");
        World worldTo = mock(World.class);
        given(worldTo.getName()).willReturn("new-world");

        Player player = mock(Player.class);
        given(player.getWorld()).willReturn(worldTo);

        PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(player, worldFrom);

        Group groupFrom = getTestGroup(worldFrom.getName(), worldFrom.getName());
        given(groupManager.getGroupFromWorld(worldFrom.getName())).willReturn(groupFrom);
        Group groupTo = getTestGroup(worldTo.getName(), worldTo.getName());
        given(groupManager.getGroupFromWorld(worldTo.getName())).willReturn(groupFrom);

        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(false);

        SettingsMocker.create()
                .set("separate-gamemode-inventories", true)
                .set("disable-bypass", false)
                .set("share-if-unconfigured", false)
                .save();

        // when
        listener.onPlayerChangeWorld(event);

        // then
        verify(playerManager).addPlayer(player, groupFrom);
        verify(playerManager).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class));
    }

    @Test
    public void shouldNotChangeInventory() {
        // given
        World worldFrom = mock(World.class);
        given(worldFrom.getName()).willReturn("world");
        World worldTo = mock(World.class);
        given(worldTo.getName()).willReturn("new-world");

        Player player = mock(Player.class);
        given(player.getWorld()).willReturn(worldTo);

        PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(player, worldFrom);

        Group groupFrom = getTestGroup(worldFrom.getName(), worldFrom.getName(), worldTo.getName());
        given(groupManager.getGroupFromWorld(worldFrom.getName())).willReturn(groupFrom);
        given(groupManager.getGroupFromWorld(worldTo.getName())).willReturn(groupFrom);

        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(false);

        SettingsMocker.create()
                .set("separate-gamemode-inventories", true)
                .set("disable-bypass", false)
                .set("share-if-unconfigured", false)
                .save();

        // when
        listener.onPlayerChangeWorld(event);

        // then
        verify(playerManager).addPlayer(player, groupFrom);
        verify(playerManager, never()).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class));
    }

    private static Group getTestGroup(String name, String... worldNames) {
        List<String> worlds = Arrays.asList(worldNames);
        return new Group(name, worlds, GameMode.SURVIVAL);
    }
}
