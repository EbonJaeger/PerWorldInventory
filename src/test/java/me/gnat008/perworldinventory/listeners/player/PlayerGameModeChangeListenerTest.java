package me.gnat008.perworldinventory.listeners.player;

import me.gnat008.perworldinventory.BukkitService;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static me.gnat008.perworldinventory.TestHelper.mockGroup;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link PlayerGameModeChangeListener}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PlayerGameModeChangeListenerTest {

    @InjectMocks
    private PlayerGameModeChangeListener listener;

    @Mock
    private BukkitService bukkitService;

    @Mock
    private GroupManager groupManager;

    @Mock
    private PWIPlayerManager playerManager;

    @Test
    public void shouldDoNothingEventCancelled() {
        // given
        PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(mock(Player.class), GameMode.CREATIVE);
        event.setCancelled(true);

        // when
        listener.onPlayerGameModeChange(event);

        // then
        verifyZeroInteractions(groupManager);
        verifyZeroInteractions(playerManager);
        verifyZeroInteractions(bukkitService);
    }

    @Test
    public void shouldDoEverything() {
        // given
        Player player = mock(Player.class);
        World world = mock(World.class);
        given(world.getName()).willReturn("world");
        given(player.getWorld()).willReturn(world);
        Group group = mockGroup("world");
        given(groupManager.getGroupFromWorld("world")).willReturn(group);

        PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(player, GameMode.CREATIVE);

        // when
        listener.onPlayerGameModeChange(event);

        // then
        verify(groupManager).getGroupFromWorld("world");
        verify(playerManager).addPlayer(player, group);
        verify(bukkitService).runTaskLater(any(Runnable.class), any(Long.class));
    }
}
