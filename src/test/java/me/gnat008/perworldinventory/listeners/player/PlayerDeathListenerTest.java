package me.gnat008.perworldinventory.listeners.player;

import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static me.gnat008.perworldinventory.TestHelper.mockGroup;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link PlayerDeathListener}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PlayerDeathListenerTest {

    @InjectMocks
    private PlayerDeathListener listener;

    @Mock
    private GroupManager groupManager;

    @Mock
    private PWIPlayerManager playerManager;

    @Test
    public void shouldRemoveInventory() {
        // given
        Player player = mock(Player.class);
        given(player.getInventory()).willReturn(mock(PlayerInventory.class));
        given(player.getActivePotionEffects()).willReturn(new ArrayList<>());
        Location loc = mock(Location.class);
        World world = mock(World.class);

        given(player.getLocation()).willReturn(loc);
        given(loc.getWorld()).willReturn(world);
        given(world.getName()).willReturn("test");

        Group group = mockGroup("test");
        given(groupManager.getGroupFromWorld("test")).willReturn(group);

        List<ItemStack> drops = new ArrayList<>();
        PlayerDeathEvent event = new PlayerDeathEvent(player, drops, 5, 5, "Bob died.");
        event.setKeepInventory(false);

        // when
        listener.onPlayerDeath(event);

        // then
        verify(player).getInventory();
    }

    @Test
    public void shouldNotRemoveInventory() {
        // given
        Player player = mock(Player.class);
        given(player.getActivePotionEffects()).willReturn(new ArrayList<>());
        Location loc = mock(Location.class);
        World world = mock(World.class);

        given(player.getLocation()).willReturn(loc);
        given(loc.getWorld()).willReturn(world);
        given(world.getName()).willReturn("test");

        Group group = mockGroup("test");
        given(groupManager.getGroupFromWorld("test")).willReturn(group);

        List<ItemStack> drops = new ArrayList<>();
        PlayerDeathEvent event = new PlayerDeathEvent(player, drops, 5, 5, "Bob died.");
        event.setKeepInventory(true);

        // when
        listener.onPlayerDeath(event);

        // then
        verify(player, never()).getInventory();
    }

    @Test
    public void shouldResetExp() {
        // given
        Player player = mock(Player.class);
        given(player.getInventory()).willReturn(mock(PlayerInventory.class));
        given(player.getActivePotionEffects()).willReturn(new ArrayList<>());
        Location loc = mock(Location.class);
        World world = mock(World.class);

        given(player.getLocation()).willReturn(loc);
        given(loc.getWorld()).willReturn(world);
        given(world.getName()).willReturn("test");

        Group group = mockGroup("test");
        given(groupManager.getGroupFromWorld("test")).willReturn(group);

        List<ItemStack> drops = new ArrayList<>();
        PlayerDeathEvent event = new PlayerDeathEvent(player, drops, 5, 5, 5, 5, "Bob died.");
        event.setKeepLevel(false);

        // when
        listener.onPlayerDeath(event);

        // then
        verify(player).setExp(5);
        verify(player).setLevel(5);
    }

    @Test
    public void shouldNotResetExp() {
        // given
        Player player = mock(Player.class);
        given(player.getInventory()).willReturn(mock(PlayerInventory.class));
        given(player.getActivePotionEffects()).willReturn(new ArrayList<>());
        Location loc = mock(Location.class);
        World world = mock(World.class);

        given(player.getLocation()).willReturn(loc);
        given(loc.getWorld()).willReturn(world);
        given(world.getName()).willReturn("test");

        Group group = mockGroup("test");
        given(groupManager.getGroupFromWorld("test")).willReturn(group);

        List<ItemStack> drops = new ArrayList<>();
        PlayerDeathEvent event = new PlayerDeathEvent(player, drops, 5, 5, 5, 5, "Bob died.");
        event.setKeepLevel(true);

        // when
        listener.onPlayerDeath(event);

        // then
        verify(player, never()).setExp(5);
        verify(player, never()).setLevel(5);
    }
}
