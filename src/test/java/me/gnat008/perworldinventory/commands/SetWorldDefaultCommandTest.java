package me.gnat008.perworldinventory.commands;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.data.FlatFile;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static me.gnat008.perworldinventory.TestHelper.mockGroup;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

/**
 * Tests for {@link SetWorldDefaultCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SetWorldDefaultCommandTest {

    @InjectMocks
    private SetWorldDefaultCommand command;

    @Mock
    private PerWorldInventory plugin;

    @Mock
    private FlatFile fileSerializer;

    @Mock
    private GroupManager groupManager;

    @Test
    public void shouldNotExecuteNotAPlayer() {
        // given
        CommandSender sender = mock(CommandSender.class);

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
        List<String> args = Arrays.asList("default", "blarg");

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
        List<String> args = new ArrayList<>();
        args.add("serverDefault");

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
        List<String> args = new ArrayList<>();
        args.add("blarg");
        //Group group = new Group("blarg", null, null);
        Group group = mockGroup("blarg");
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
        World world = mock(World.class);
        given(player.getWorld()).willReturn(world);
        given(world.getName()).willReturn("world");

        Set<String> worlds = new HashSet<>();
        worlds.add("world");
        Group group = mockGroup("test", worlds);
        given(groupManager.getGroupFromWorld("world")).willReturn(group);

        // when
        command.executeCommand(player, Collections.<String>emptyList());

        // then
        ArgumentCaptor<Group> captor = ArgumentCaptor.forClass(Group.class);
        verify(fileSerializer, only()).setGroupDefault(eq(player), captor.capture());
        assertThat(captor.getValue().getName(), equalTo("test"));
    }
}
