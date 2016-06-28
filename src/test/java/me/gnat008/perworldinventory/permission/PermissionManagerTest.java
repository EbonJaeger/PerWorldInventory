package me.gnat008.perworldinventory.permission;

import me.gnat008.perworldinventory.PerWorldInventory;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link PermissionManager}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PermissionManagerTest {

    @InjectMocks
    private PermissionManager permissionManager;

    @Mock
    PerWorldInventory plugin;

    @Mock
    Server server;

    @Mock
    PluginManager pluginManager;

    @Test
    public void shouldUseDefaultPermissionForCommandSender() {
        // given
        PermissionNode node = TestPermissions.HELP;
        CommandSender sender = mock(CommandSender.class);

        // when
        boolean result = permissionManager.hasPermission(sender, node);

        // then
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldGrantToOpSender() {
        // given
        PermissionNode node = TestPermissions.CONVERT;
        CommandSender sender = mock(CommandSender.class);
        given(sender.isOp()).willReturn(true);

        // when
        boolean result = permissionManager.hasPermission(sender, node);

        // then
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldDenyToOpSender() {
        // given
        PermissionNode node = TestPermissions.SYSTEM_LORD;
        CommandSender sender = mock(CommandSender.class);
        given(sender.isOp()).willReturn(true);

        // when
        boolean result = permissionManager.hasPermission(sender, node);

        // then
        assertThat(result, equalTo(false));
    }

    @Test
    public void shouldGrantToNonOpPlayer() {
        // given
        PermissionNode node = TestPermissions.HELP;
        Player player = mock(Player.class);

        // when
        boolean result = permissionManager.hasPermission(player, node);

        // then
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldDenyToNonOpPlayer() {
        // given
        PermissionNode node = TestPermissions.CONVERT;
        Player player = mock(Player.class);

        // when
        boolean result = permissionManager.hasPermission(player, node);

        // then
        assertThat(result, equalTo(false));
    }

    @Test
    public void shouldGrantToOpPlayer() {
        // given
        PermissionNode node = TestPermissions.CONVERT;
        Player player = mock(Player.class);
        given(player.isOp()).willReturn(true);

        // when
        boolean result = permissionManager.hasPermission(player, node);

        // then
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldDenyToOpPlayer() {
        // given
        PermissionNode node = TestPermissions.SYSTEM_LORD;
        Player player = mock(Player.class);
        given(player.isOp()).willReturn(true);

        // when
        boolean result = permissionManager.hasPermission(player, node);

        // then
        assertThat(result, equalTo(false));
    }

    @Test
    public void shouldHandleNullPermissionForCommandSender() {
        // given
        PermissionNode node = null;
        CommandSender sender = mock(CommandSender.class);

        // when
        boolean result = permissionManager.hasPermission(sender, node);

        // then
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldHandleNullPermissionForPlayer() {
        // given
        PermissionNode node = null;
        Player player = mock(Player.class);

        // when
        boolean result = permissionManager.hasPermission(player, node);

        // then
        assertThat(result, equalTo(true));
    }
}
