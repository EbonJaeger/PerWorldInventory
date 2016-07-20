package me.gnat008.perworldinventory.listeners;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.SettingsMocker;
import me.gnat008.perworldinventory.data.DataWriter;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.listeners.player.PlayerSpawnLocationListener;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PlayerPermission;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link PlayerSpawnLocationListener}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PlayerSpawnLocationListenerTest {

    @InjectMocks
    private PlayerSpawnLocationListener listener;

    @Mock
    private PerWorldInventory plugin;

    @Mock
    private DataWriter dataWriter;

    @Mock
    private GroupManager groupManager;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private PWIPlayerManager playerManager;

    @Test
    public void shouldNotCheckDisabled() {
        // given
        PlayerSpawnLocationEvent event = mock(PlayerSpawnLocationEvent.class);
        SettingsMocker.create().set("load-data-on-join", false).save();

        // when
        listener.onPlayerSpawn(event);

        // then
        verifyZeroInteractions(dataWriter);
        verifyZeroInteractions(groupManager);
        verifyZeroInteractions(permissionManager);
        verifyZeroInteractions(playerManager);
    }

    @Test
    public void shouldDoNothingNoDataFound() {
        // given
        Player player = mock(Player.class);
        World world = mock(World.class);
        given(world.getName()).willReturn("world");
        Location spawnLocation = new Location(world, 1, 2, 3);
        PlayerSpawnLocationEvent event = new PlayerSpawnLocationEvent(player, spawnLocation);
        SettingsMocker.create().set("load-data-on-join", true).save();
        given(dataWriter.getLogoutData(player)).willReturn(null);

        // when
        listener.onPlayerSpawn(event);

        // then
        verifyZeroInteractions(groupManager);
        verifyZeroInteractions(permissionManager);
        verifyZeroInteractions(playerManager);
    }

    @Test
    public void shouldDoNothingSameWorld() {
        // given
        Player player = mock(Player.class);
        World world = mock(World.class);
        given(world.getName()).willReturn("world");
        Location spawnLocation = new Location(world, 1, 2, 3);
        PlayerSpawnLocationEvent event = new PlayerSpawnLocationEvent(player, spawnLocation);
        SettingsMocker.create().set("load-data-on-join", true).save();

        World oldWorld = mock(World.class);
        given(oldWorld.getName()).willReturn("world");
        Location lastLocation = new Location(oldWorld, 4, 5, 6);
        given(dataWriter.getLogoutData(player)).willReturn(lastLocation);

        // when
        listener.onPlayerSpawn(event);

        // then
        verifyZeroInteractions(groupManager);
        verifyZeroInteractions(permissionManager);
        verifyZeroInteractions(playerManager);
    }

    @Test
    public void shouldDoNothingSameGroup() {
        // given
        Player player = mock(Player.class);
        World world = mock(World.class);
        given(world.getName()).willReturn("world");
        Location spawnLocation = new Location(world, 1, 2, 3);
        PlayerSpawnLocationEvent event = new PlayerSpawnLocationEvent(player, spawnLocation);
        SettingsMocker.create().set("load-data-on-join", true).save();

        World oldWorld = mock(World.class);
        given(oldWorld.getName()).willReturn("world_nether");
        Location lastLocation = new Location(oldWorld, 4, 5, 6);
        given(dataWriter.getLogoutData(player)).willReturn(lastLocation);

        Group group = new Group("default", null, GameMode.SURVIVAL);
        given(groupManager.getGroupFromWorld("world")).willReturn(group);
        given(groupManager.getGroupFromWorld("world_nether")).willReturn(group);

        // when
        listener.onPlayerSpawn(event);

        // then
        verifyZeroInteractions(permissionManager);
        verifyZeroInteractions(playerManager);
    }

    @Test
    public void shouldNotLoadDataBecauseBypass() {
        // given
        Player player = mock(Player.class);
        World world = mock(World.class);
        given(world.getName()).willReturn("world");
        Location spawnLocation = new Location(world, 1, 2, 3);
        PlayerSpawnLocationEvent event = new PlayerSpawnLocationEvent(player, spawnLocation);
        SettingsMocker.create().set("load-data-on-join", true).set("disable-bypass", false).save();

        World oldWorld = mock(World.class);
        given(oldWorld.getName()).willReturn("world2");
        Location lastLocation = new Location(oldWorld, 4, 5, 6);
        given(dataWriter.getLogoutData(player)).willReturn(lastLocation);

        Group group1 = new Group("default", null, GameMode.SURVIVAL);
        given(groupManager.getGroupFromWorld("world")).willReturn(group1);
        Group group2 = new Group("second_group", null, GameMode.SURVIVAL);
        given(groupManager.getGroupFromWorld("world2")).willReturn(group2);

        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(true);

        // when
        listener.onPlayerSpawn(event);

        // then
        verify(playerManager).addPlayer(player, group2);
        verifyNoMoreInteractions(playerManager);
    }

    @Test
    public void shouldLoadDataSeparateGamemode() {
        // given
        Player player = mock(Player.class);
        World world = mock(World.class);
        given(world.getName()).willReturn("world");
        Location spawnLocation = new Location(world, 1, 2, 3);
        PlayerSpawnLocationEvent event = new PlayerSpawnLocationEvent(player, spawnLocation);
        SettingsMocker.create()
                .set("load-data-on-join", true)
                .set("disable-bypass", false)
                .set("separate-gamemode-inventories", true)
                .save();

        World oldWorld = mock(World.class);
        given(oldWorld.getName()).willReturn("world2");
        Location lastLocation = new Location(oldWorld, 4, 5, 6);
        given(dataWriter.getLogoutData(player)).willReturn(lastLocation);

        Group group1 = new Group("default", null, GameMode.SURVIVAL);
        given(groupManager.getGroupFromWorld("world")).willReturn(group1);
        Group group2 = new Group("second_group", null, GameMode.SURVIVAL);
        given(groupManager.getGroupFromWorld("world2")).willReturn(group2);

        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(false);

        given(player.getGameMode()).willReturn(GameMode.CREATIVE);

        // when
        listener.onPlayerSpawn(event);

        // then
        verify(playerManager).addPlayer(player, group2);
        verify(playerManager).getPlayerData(group1, player.getGameMode(), player);
    }

    @Test
    public void shouldLoadDataNoSeparateGamemode() {
        // given
        Player player = mock(Player.class);
        World world = mock(World.class);
        given(world.getName()).willReturn("world");
        Location spawnLocation = new Location(world, 1, 2, 3);
        PlayerSpawnLocationEvent event = new PlayerSpawnLocationEvent(player, spawnLocation);
        SettingsMocker.create()
                .set("load-data-on-join", true)
                .set("disable-bypass", false)
                .set("separate-gamemode-inventories", false)
                .save();

        World oldWorld = mock(World.class);
        given(oldWorld.getName()).willReturn("world2");
        Location lastLocation = new Location(oldWorld, 4, 5, 6);
        given(dataWriter.getLogoutData(player)).willReturn(lastLocation);

        Group group1 = new Group("default", null, GameMode.SURVIVAL);
        given(groupManager.getGroupFromWorld("world")).willReturn(group1);
        Group group2 = new Group("second_group", null, GameMode.SURVIVAL);
        given(groupManager.getGroupFromWorld("world2")).willReturn(group2);

        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(false);

        given(player.getGameMode()).willReturn(GameMode.CREATIVE);

        // when
        listener.onPlayerSpawn(event);

        // then
        verify(playerManager).addPlayer(player, group2);
        verify(playerManager).getPlayerData(group1, GameMode.SURVIVAL, player);
    }
}
