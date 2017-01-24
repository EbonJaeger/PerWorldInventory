package me.gnat008.perworldinventory.process;

import me.gnat008.perworldinventory.TestHelper;
import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PlayerPermission;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Tests for {@link InventoryChangeProcess}.
 */
@RunWith(MockitoJUnitRunner.class)
public class InventoryChangeProcessTest {

    @InjectMocks
    private InventoryChangeProcess process;

    @Mock
    private GroupManager groupManager;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private PWIPlayerManager playerManager;

    @Mock
    private Settings settings;

    @BeforeClass
    public static void initLogger() {
        TestHelper.initMockLogger();
    }

    @Test
    public void shouldNotChangeInventoryBecauseFromGroupUnconfigured() {
        // given
        Player player = mock(Player.class);
        Group from = mockGroup("test_group" ,GameMode.SURVIVAL, false);
        Group to = mockGroup("other_group" ,GameMode.SURVIVAL, true);
        given(settings.getProperty(PwiProperties.SHARE_IF_UNCONFIGURED)).willReturn(true);
        given(settings.getProperty(PwiProperties.MANAGE_GAMEMODES)).willReturn(false);

        // when
        process.processWorldChange(player, from, to);

        // then
        verifyZeroInteractions(playerManager);
        verifyZeroInteractions(permissionManager);
    }

    @Test
    public void shouldNotChangeInventoryBecauseToGroupUnconfigured() {
        // given
        Player player = mock(Player.class);
        Group from = mockGroup("test_group", GameMode.SURVIVAL, true);
        Group to = mockGroup("other_group", GameMode.SURVIVAL, false);
        given(settings.getProperty(PwiProperties.SHARE_IF_UNCONFIGURED)).willReturn(true);
        given(settings.getProperty(PwiProperties.MANAGE_GAMEMODES)).willReturn(false);

        // when
        process.processWorldChange(player, from, to);

        // then
        verifyZeroInteractions(playerManager);
        verifyZeroInteractions(permissionManager);
    }

    @Test
    public void shouldChangeInventoryEvenIfGroupsNotConfigured() {
        // given
        Player player = mock(Player.class);
        given(player.getGameMode()).willReturn(GameMode.SURVIVAL);
        Group from = mockGroup("test_group", GameMode.SURVIVAL, false);
        Group to = mockGroup("other_group", GameMode.SURVIVAL, false);
        given(settings.getProperty(PwiProperties.SHARE_IF_UNCONFIGURED)).willReturn(false);
        given(settings.getProperty(PwiProperties.DISABLE_BYPASS)).willReturn(false);
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(true);
        given(settings.getProperty(PwiProperties.MANAGE_GAMEMODES)).willReturn(false);
        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(false);

        // when
        process.processWorldChange(player, from, to);

        // then
        verify(permissionManager).hasPermission(player, PlayerPermission.BYPASS_WORLDS);
        verify(playerManager).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class));
    }

    @Test
    public void shouldNotChangeInventoryBecauseSameGroup() {
        // given
        Player player = mock(Player.class);
        Group from = mockGroup("test_group", GameMode.SURVIVAL, true);
        Group to = from;
        given(settings.getProperty(PwiProperties.MANAGE_GAMEMODES)).willReturn(false);

        // when
        process.processWorldChange(player, from, to);

        // then
        verifyZeroInteractions(playerManager);
        verifyZeroInteractions(permissionManager);
    }

    @Test
    public void shouldNotChangeInventoryBecauseBypass() {
        // given
        Player player = mock(Player.class);
        Group from = mockGroup("test_group", GameMode.SURVIVAL, true);
        Group to = mockGroup("other_group", GameMode.SURVIVAL, true);
        given(settings.getProperty(PwiProperties.DISABLE_BYPASS)).willReturn(false);
        given(settings.getProperty(PwiProperties.MANAGE_GAMEMODES)).willReturn(false);
        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(true);

        // when
        process.processWorldChange(player, from, to);

        // then
        verifyZeroInteractions(playerManager);
    }

    @Test
    public void shouldNotBypassBecauseNoPermission() {
        // given
        Player player = mock(Player.class);
        given(player.getGameMode()).willReturn(GameMode.SURVIVAL);
        Group from = mockGroup("test_group", GameMode.SURVIVAL, true);
        Group to = mockGroup("other_group", GameMode.SURVIVAL, true);
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(true);
        given(settings.getProperty(PwiProperties.DISABLE_BYPASS)).willReturn(false);
        given(settings.getProperty(PwiProperties.MANAGE_GAMEMODES)).willReturn(false);
        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(false);

        // when
        process.processWorldChange(player, from, to);

        // then
        verify(playerManager).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class));
    }

    @Test
    public void shouldNotBypassBecauseBypassDisabled() {
        // given
        Player player = mock(Player.class);
        given(player.getGameMode()).willReturn(GameMode.SURVIVAL);
        Group from = mockGroup("test_group", GameMode.SURVIVAL, true);
        Group to = mockGroup("other_group", GameMode.SURVIVAL, true);
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(true);
        given(settings.getProperty(PwiProperties.DISABLE_BYPASS)).willReturn(true);
        given(settings.getProperty(PwiProperties.MANAGE_GAMEMODES)).willReturn(false);

        // when
        process.processWorldChange(player, from, to);

        // then
        verify(playerManager).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class));
    }

    private Group mockGroup(String name, GameMode gameMode, boolean configured) {
        Set<String> worlds = new HashSet<>();
        worlds.add(name);

        return new Group(name, worlds, gameMode, configured);
    }
}
