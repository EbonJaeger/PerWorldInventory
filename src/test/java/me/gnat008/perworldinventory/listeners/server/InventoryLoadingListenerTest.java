package me.gnat008.perworldinventory.listeners.server;

import me.gnat008.perworldinventory.data.serializers.DeserializeCause;
import me.gnat008.perworldinventory.events.InventoryLoadCompleteEvent;
import me.gnat008.perworldinventory.process.InventoryChangeProcess;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link InventoryLoadingListener}.
 */
@RunWith(MockitoJUnitRunner.class)
public class InventoryLoadingListenerTest {

    @InjectMocks
    private InventoryLoadingListener listener;

    @Mock
    private InventoryChangeProcess process;

    @Test
    public void shouldInteract() {
        // given
        Player player = mock(Player.class);
        InventoryLoadCompleteEvent event = new InventoryLoadCompleteEvent(player, DeserializeCause.WORLD_CHANGE);

        // when
        listener.onLoadComplete(event);

        // then
        verify(process).postProcessWorldChange(any(Player.class));
    }

    @Test
    public void shouldNotInteract() {
        // given
        Player player = mock(Player.class);
        InventoryLoadCompleteEvent event = new InventoryLoadCompleteEvent(player, DeserializeCause.GAMEMODE_CHANGE);

        // when
        listener.onLoadComplete(event);

        // then
        verifyZeroInteractions(process);
    }

    @Test
    public void shouldAlsoNotInteract() {
        // given
        Player player = mock(Player.class);
        InventoryLoadCompleteEvent event = new InventoryLoadCompleteEvent(player, DeserializeCause.CHANGED_DEFAULTS);

        // when
        listener.onLoadComplete(event);

        // then
        verifyZeroInteractions(process);
    }
}
