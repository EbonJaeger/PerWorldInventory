package me.gnat008.perworldinventory.process;

import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Tests for {@link GameModeChangeProcess}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GameModeChangeProcessTest {

    @InjectMocks
    private GameModeChangeProcess process;

    @Mock
    private GroupManager groupManager;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private PWIPlayerManager playerManager;

    @Mock
    private Settings settings;

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
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(true);
        given(settings.getProperty(PwiProperties.DISABLE_BYPASS)).willReturn(false);

        // when
        process.processGameModeChange(event);

        // then
        verify(playerManager).addPlayer(player, group);
        verify(playerManager, never()).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class));
    }

    @Test
    public void shouldNotBypassNoPermission() {
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
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(true);
        given(settings.getProperty(PwiProperties.DISABLE_BYPASS)).willReturn(false);

        // when
        process.processGameModeChange(event);

        // then
        verify(playerManager).addPlayer(player, group);
        verify(playerManager).getPlayerData(group, newGameMode, player);
    }

    @Test
    public void shouldNotBypassBecauseBypassDisabled() {
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
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(true);
        given(settings.getProperty(PwiProperties.DISABLE_BYPASS)).willReturn(true);

        // when
        process.processGameModeChange(event);

        // then
        verify(playerManager).addPlayer(player, group);
        verify(playerManager).getPlayerData(group, newGameMode, player);
    }

    @Test
    public void shouldDoNothingBecauseDisabled() {
        // given
        Player player = mock(Player.class);
        GameMode newGameMode = GameMode.CREATIVE;
        PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(player, newGameMode);
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(false);

        // when
        process.processGameModeChange(event);

        // then
        verifyZeroInteractions(groupManager);
        verifyZeroInteractions(permissionManager);
        verifyZeroInteractions(playerManager);
    }

    private static Group getTestGroup() {
        List<String> worlds = Arrays.asList("world", "second-world");
        return new Group("test-group", worlds, GameMode.SURVIVAL);
    }
}
