package me.gnat008.perworldinventory.commands;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.data.FileWriter;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.permission.AdminPermission;
import me.gnat008.perworldinventory.permission.PermissionManager;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link SetWorldDefaultCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SetWorldDefaultsCommandTest {

    @InjectMocks
    private SetWorldDefaultCommand command;

    @Mock
    private PerWorldInventory plugin;

    @Mock
    private FileWriter fileSerializer;

    @Mock
    private GroupManager groupManager;

    @Mock
    private PermissionManager permissionManager;

    @Test
    public void shouldNotExecuteNoPermission() {
        // given
        Player player = mock(Player.class);
        given(permissionManager.hasPermission(player, AdminPermission.SETDEFAULTS)).willReturn(false);

        // when
        command.executeCommand(player, Collections.<String>emptyList());

        // then
        verify(player).sendMessage(argThat(containsString("You do not have permission to do that")));
        verifyZeroInteractions(fileSerializer);
    }

    @Test
    public void shouldNotExecuteNotAPlayer() {
        // given
        CommandSender sender = mock(CommandSender.class);
        given(permissionManager.hasPermission(sender, AdminPermission.SETDEFAULTS)).willReturn(true);

        // when
        command.executeCommand(sender, Collections.<String>emptyList());

        // then
        verify(sender).sendMessage(argThat(containsString("This command may only be run from ingame")));
        verifyZeroInteractions(fileSerializer);
    }

    @Test
    public void shouldNotExecuteTooManyArgs() {
        // given
        Player player = mock(Player.class);
        given(permissionManager.hasPermission(player, AdminPermission.SETDEFAULTS)).willReturn(true);
        List<String> args = new ArrayList<>();
        args.add("default");
        args.add("blarg");

        // when
        command.executeCommand(player, args);

        // then
        verify(player).sendMessage(argThat(containsString("Incorrect number of arguments")));
        verifyZeroInteractions(fileSerializer);
    }

    @Test
    public void shouldSetForDefaultGroup() {
        // given
        Player player = mock(Player.class);
        given(permissionManager.hasPermission(player, AdminPermission.SETDEFAULTS)).willReturn(true);
        List<String> args = new ArrayList<>();
        args.add("serverDefault");
        Group group = new Group("__default", null, null);

        // when
        command.executeCommand(player, args);

        // then
        ArgumentCaptor<Group> captor = ArgumentCaptor.forClass(Group.class);
        verify(fileSerializer, only()).setGroupDefault(eq(player), captor.capture());
        assertThat(captor.getValue().getName(), equalTo("__default"));
    }

    @Test
    public void shouldSetForArbitraryGroup() {
        // given
        Player player = mock(Player.class);
        given(permissionManager.hasPermission(player, AdminPermission.SETDEFAULTS)).willReturn(true);
        List<String> args = new ArrayList<>();
        args.add("blarg");
        Group group = new Group("blarg", null, null);
        given(groupManager.getGroup("blarg")).willReturn(group);

        // when
        command.executeCommand(player, args);

        // then
        ArgumentCaptor<Group> captor = ArgumentCaptor.forClass(Group.class);
        verify(fileSerializer, only()).setGroupDefault(eq(player), captor.capture());
        assertThat(captor.getValue().getName(), equalTo("blarg"));
    }

    @Test
    public void shouldSetForGroupPlayerStandingIn() {
        // given
        Player player = mock(Player.class);
        given(permissionManager.hasPermission(player, AdminPermission.SETDEFAULTS)).willReturn(true);

        World world = mock(World.class);
        given(player.getWorld()).willReturn(world);
        given(world.getName()).willReturn("world");

        List<String> worlds = new ArrayList<>();
        worlds.add("world");
        Group group = new Group("test", worlds, GameMode.SURVIVAL);
        given(groupManager.getGroupFromWorld("world")).willReturn(group);

        // when
        command.executeCommand(player, Collections.<String>emptyList());

        // then
        ArgumentCaptor<Group> captor = ArgumentCaptor.forClass(Group.class);
        verify(fileSerializer, only()).setGroupDefault(eq(player), captor.capture());
        assertThat(captor.getValue().getName(), equalTo("test"));
    }
}
