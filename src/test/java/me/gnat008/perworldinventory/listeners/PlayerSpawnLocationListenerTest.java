package me.gnat008.perworldinventory.listeners;

import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.DataSource;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.listeners.player.PlayerSpawnLocationListener;
import me.gnat008.perworldinventory.process.InventoryChangeProcess;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static me.gnat008.perworldinventory.TestHelper.mockGroup;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Tests for {@link PlayerSpawnLocationListener}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PlayerSpawnLocationListenerTest {

    @InjectMocks
    private PlayerSpawnLocationListener listener;

    @Mock
    private DataSource dataSource;

    @Mock
    private GroupManager groupManager;

    @Mock
    private InventoryChangeProcess process;

    @Mock
    private Settings settings;

    @Test
    public void shouldNotCheckDisabled() {
        // given
        PlayerSpawnLocationEvent event = mock(PlayerSpawnLocationEvent.class);
        given(settings.getProperty(PwiProperties.LOAD_DATA_ON_JOIN)).willReturn(false);

        // when
        listener.onPlayerSpawn(event);

        // then
        verifyZeroInteractions(dataSource);
        verifyZeroInteractions(groupManager);
        verifyZeroInteractions(process);
    }

    @Test
    public void shouldDoNothingNoDataFound() {
        // given
        Player player = mock(Player.class);
        World world = mock(World.class);
        given(world.getName()).willReturn("world");
        Location spawnLocation = new Location(world, 1, 2, 3);
        PlayerSpawnLocationEvent event = new PlayerSpawnLocationEvent(player, spawnLocation);
        given(settings.getProperty(PwiProperties.LOAD_DATA_ON_JOIN)).willReturn(true);
        given(dataSource.getLogoutData(player)).willReturn(null);

        // when
        listener.onPlayerSpawn(event);

        // then
        verifyZeroInteractions(groupManager);
        verifyZeroInteractions(process);
    }

    @Test
    public void shouldDoNothingSameWorld() {
        // given
        Player player = mock(Player.class);
        World world = mock(World.class);
        given(world.getName()).willReturn("world");
        Location spawnLocation = new Location(world, 1, 2, 3);
        PlayerSpawnLocationEvent event = new PlayerSpawnLocationEvent(player, spawnLocation);
        given(settings.getProperty(PwiProperties.LOAD_DATA_ON_JOIN)).willReturn(true);

        World oldWorld = mock(World.class);
        given(oldWorld.getName()).willReturn("world");
        Location lastLocation = new Location(oldWorld, 4, 5, 6);
        given(dataSource.getLogoutData(player)).willReturn(lastLocation);

        // when
        listener.onPlayerSpawn(event);

        // then
        verifyZeroInteractions(groupManager);
        verifyZeroInteractions(process);
    }

    @Test
    public void shouldProcessChange() {
        // given
        Player player = mock(Player.class);
        World world = mock(World.class);
        given(world.getName()).willReturn("world");

        Set<String> worlds = new HashSet<>();
        Group spawnWorldGroup = mockGroup("spawn", Arrays.asList("otherWorld", world.getName()), GameMode.SURVIVAL);

        given(groupManager.getGroupFromWorld(world.getName())).willReturn(spawnWorldGroup);

        Location spawnLocation = new Location(world, 1, 2, 3);
        PlayerSpawnLocationEvent event = new PlayerSpawnLocationEvent(player, spawnLocation);
        given(settings.getProperty(PwiProperties.LOAD_DATA_ON_JOIN)).willReturn(true);

        World oldWorld = mock(World.class);
        given(oldWorld.getName()).willReturn("other_world");
        Location lastLocation = new Location(oldWorld, 4, 5, 6);
        given(dataSource.getLogoutData(player)).willReturn(lastLocation);
        Group oldWorldGroup = mockGroup("oldWorldGroup", Collections.singletonList(oldWorld.getName()), GameMode.SURVIVAL);
        given(groupManager.getGroupFromWorld(oldWorld.getName())).willReturn(oldWorldGroup);

        // when
        listener.onPlayerSpawn(event);

        // then
        verify(process, only()).processWorldChangeOnSpawn(player, oldWorldGroup, spawnWorldGroup);
    }
}
