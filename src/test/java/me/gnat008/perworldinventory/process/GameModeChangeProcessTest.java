package me.gnat008.perworldinventory.process;

import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.data.serializers.DeserializeCause;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PlayerPermission;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
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
 * Tests for {@link GameModeChangeProcess}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GameModeChangeProcessTest {

    @InjectMocks
    private GameModeChangeProcess process;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private PWIPlayerManager playerManager;

    @Mock
    private Settings settings;

    @Test
    public void shouldBypass() {
        // given
        Player player = mock(Player.class);

        Group group = mockGroup("world");

        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_GAMEMODE)).willReturn(true);
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(true);
        given(settings.getProperty(PwiProperties.DISABLE_BYPASS)).willReturn(false);

        // when
        process.processGameModeChange(player, GameMode.ADVENTURE, group);

        // then
        verify(playerManager, never()).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class), any(DeserializeCause.class));
    }

    @Test
    public void shouldNotBypassNoPermission() {
        // given
        Player player = mock(Player.class);

        Group group = mockGroup("world");

        GameMode newGameMode = GameMode.CREATIVE;
        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_GAMEMODE)).willReturn(false);
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(true);
        given(settings.getProperty(PwiProperties.DISABLE_BYPASS)).willReturn(false);

        // when
        process.processGameModeChange(player, newGameMode, group);

        // then
        verify(playerManager).getPlayerData(group, newGameMode, player, DeserializeCause.GAMEMODE_CHANGE);
    }

    @Test
    public void shouldNotBypassBecauseBypassDisabled() {
        // given
        Player player = mock(Player.class);

        Group group = mockGroup("world");

        GameMode newGameMode = GameMode.CREATIVE;
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(true);
        given(settings.getProperty(PwiProperties.DISABLE_BYPASS)).willReturn(true);

        // when
        process.processGameModeChange(player, newGameMode, group);

        // then
        verify(playerManager).getPlayerData(group, newGameMode, player, DeserializeCause.GAMEMODE_CHANGE);
    }

    @Test
    public void shouldDoNothingBecauseDisabled() {
        // given
        Player player = mock(Player.class);
        GameMode newGameMode = GameMode.CREATIVE;
        Group group = mockGroup("world");
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(false);

        // when
        process.processGameModeChange(player, newGameMode, group);

        // then
        verifyZeroInteractions(permissionManager);
        verifyZeroInteractions(playerManager);
    }
}
