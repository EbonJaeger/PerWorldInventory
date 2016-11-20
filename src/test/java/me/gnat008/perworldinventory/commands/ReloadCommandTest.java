package me.gnat008.perworldinventory.commands;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.permission.AdminPermission;
import me.gnat008.perworldinventory.permission.PermissionManager;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link ReloadCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReloadCommandTest {

    @InjectMocks
    private ReloadCommand command;

    @Mock
    private PerWorldInventory plugin;

    @Mock
    private GroupManager groupManager;

    @Mock
    private PermissionManager permissionManager;

    @Test
    public void shouldNotExecuteNoPermission() {
        // given
        Player player = mock(Player.class);
        given(permissionManager.hasPermission(player, AdminPermission.RELOAD)).willReturn(false);

        // when
        command.executeCommand(player, Collections.<String>emptyList());

        // then
        verify(player).sendMessage(argThat(containsString("You do not have permission to do that")));
    }
}
