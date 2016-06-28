package me.gnat008.perworldinventory.listeners;

import me.gnat008.perworldinventory.config.SettingsMocker;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.listeners.player.PlayerGameModeChangeListener;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PlayerPermission;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link PlayerGameModeChangeListener}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PlayerGameModeChangeListenerTest {

    @InjectMocks
    private PlayerGameModeChangeListener listener;

    @Mock
    private GroupManager groupManager;

    @Mock
    private PWIPlayerManager playerManager;

    @Mock
    private PermissionManager permissionManager;

    @Test
    public void shouldBypass() {
        // given
        World world = mock(World.class);
        given(world.getName()).willReturn("world");
        Player player = mock(Player.class);
        given(player.getWorld()).willReturn(world);
        PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(player, GameMode.ADVENTURE);
        Group group = getTestGroup();
        given(groupManager.getGroupFromWorld("world")).willReturn(group);
        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_GAMEMODE)).willReturn(true);
        SettingsMocker.create().set("separate-gamemode-inventories", true).save();

        // when
        listener.onPlayerGameModeChange(event);

        // then
        verify(playerManager).addPlayer(player, group);
        verify(playerManager, never()).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class));
    }

    @Test
    public void shouldNotBypass() {
        // given
        World world = mock(World.class);
        String worldName = "world";
        given(world.getName()).willReturn(worldName);
        Player player = mock(Player.class);
        given(player.getWorld()).willReturn(world);
        Group group = getTestGroup();
        GameMode newGameMode = GameMode.CREATIVE;
        PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(player, newGameMode);
        given(groupManager.getGroupFromWorld(worldName)).willReturn(group);
        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_GAMEMODE)).willReturn(false);
        SettingsMocker.create().set("separate-gamemode-inventories", true).save();

        // when
        listener.onPlayerGameModeChange(event);

        // then
        verify(playerManager).addPlayer(player, group);
        verify(playerManager).getPlayerData(group, newGameMode, player);
    }

    private static Group getTestGroup() {
        List<String> worlds = Arrays.asList("world", "second-world");
        return new Group("test-group", worlds, GameMode.SURVIVAL);
    }
}
