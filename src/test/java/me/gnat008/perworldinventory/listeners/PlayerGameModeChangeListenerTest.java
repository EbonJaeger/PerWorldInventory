package me.gnat008.perworldinventory.listeners;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PlayerGameModeChangeListenerTest {

    @InjectMocks
    private PlayerGameModeChangeListener listener;

    @Mock
    PerWorldInventory plugin;

    @Mock
    GroupManager groupManager;

    @Mock
    PWIPlayerManager playerManager;

    @Test
    public void shouldBypass() {
        // given
        Player player = mock(Player.class);
        PlayerGameModeChangeEvent event = mock(PlayerGameModeChangeEvent.class);
        given(player.hasPermission("perworldinventory.bypass.gamemode")).willReturn(true);

        // when
        listener.onPlayerGameModeChange(event);

        // then
        verify(playerManager, never()).getPlayerData(null, null, null);
    }
}
