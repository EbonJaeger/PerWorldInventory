package me.gnat008.perworldinventory.listeners;

import me.gnat008.perworldinventory.config.SettingsMocker;
import me.gnat008.perworldinventory.data.DataWriter;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.listeners.player.PlayerSpawnLocationListener;
import me.gnat008.perworldinventory.process.InventoryChangeProcess;
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
    private DataWriter dataWriter;

    @Mock
    private GroupManager groupManager;

    @Mock
    private InventoryChangeProcess process;

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
        SettingsMocker.create().set("load-data-on-join", true).save();
        given(dataWriter.getLogoutData(player)).willReturn(null);

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
        SettingsMocker.create().set("load-data-on-join", true).save();

        World oldWorld = mock(World.class);
        given(oldWorld.getName()).willReturn("world");
        Location lastLocation = new Location(oldWorld, 4, 5, 6);
        given(dataWriter.getLogoutData(player)).willReturn(lastLocation);

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
        Location spawnLocation = new Location(world, 1, 2, 3);
        PlayerSpawnLocationEvent event = new PlayerSpawnLocationEvent(player, spawnLocation);
        SettingsMocker.create().set("load-data-on-join", true).save();

        World oldWorld = mock(World.class);
        given(oldWorld.getName()).willReturn("other_world");
        Location lastLocation = new Location(oldWorld, 4, 5, 6);
        given(dataWriter.getLogoutData(player)).willReturn(lastLocation);

        // when
        listener.onPlayerSpawn(event);

        // then
        verify(process, only()).processWorldChangeOnSpawn(any(Player.class), any(Group.class), any(Group.class));
    }
}
