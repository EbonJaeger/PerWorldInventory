package me.gnat008.perworldinventory.listeners.player;

import me.gnat008.perworldinventory.TestHelper;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Tests for {@link PlayerTeleportListener}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PlayerTeleportListenerTest {

    @InjectMocks
    private PlayerTeleportListener listener;

    @Mock
    private GroupManager groupManager;

    @Mock
    private PWIPlayerManager playerManager;

    @Test
    public void noInteractionsEventCancelled() {
        // given
        PlayerTeleportEvent event = mock(PlayerTeleportEvent.class);
        given(event.isCancelled()).willReturn(true);

        // when
        listener.onPlayerTeleport(event);

        // then
        verifyZeroInteractions(groupManager);
        verifyZeroInteractions(playerManager);
    }

    @Test
    public void noInteractionsSameWorld() {
        // given
        Player player = mock(Player.class);

        World worldFrom = mock(World.class);
        Location from = new Location(worldFrom, 1, 2, 3);
        Location to = new Location(worldFrom, 4, 5, 6);

        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from, to);

        // when
        listener.onPlayerTeleport(event);

        // then
        verifyZeroInteractions(groupManager);
        verifyZeroInteractions(playerManager);
    }

    @Test
    public void noPlayerInteractionsSameGroup() {
        // given
        Player player = mock(Player.class);

        World worldFrom = mock(World.class);
        given(worldFrom.getName()).willReturn("world");
        Location from = new Location(worldFrom, 1, 2, 3);
        World worldTo = mock(World.class);
        given(worldTo.getName()).willReturn("world_nether");
        Location to = new Location(worldTo, 4, 5, 6);

        Group group = TestHelper.mockGroup("world");
        given(groupManager.getGroupFromWorld("world")).willReturn(group);
        given(groupManager.getGroupFromWorld("world_nether")).willReturn(group);

        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from, to);

        // when
        listener.onPlayerTeleport(event);

        // then
        verifyZeroInteractions(playerManager);
    }

    @Test
    public void shouldAddPlayer() {
        // given
        Player player = mock(Player.class);

        World worldFrom = mock(World.class);
        given(worldFrom.getName()).willReturn("world");
        Location from = new Location(worldFrom, 1, 2, 3);
        World worldTo = mock(World.class);
        given(worldTo.getName()).willReturn("world2");
        Location to = new Location(worldTo, 4, 5, 6);

        Group groupFrom = TestHelper.mockGroup("world");
        Group groupTo = TestHelper.mockGroup("world2");
        given(groupManager.getGroupFromWorld("world")).willReturn(groupFrom);
        given(groupManager.getGroupFromWorld("world2")).willReturn(groupTo);

        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from, to);

        // when
        listener.onPlayerTeleport(event);

        // then
        verify(playerManager).addPlayer(player, groupFrom);
    }
}
