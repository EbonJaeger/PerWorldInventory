package me.gnat008.perworldinventory.commands;

import ch.jalu.injector.testing.BeforeInjecting;
import ch.jalu.injector.testing.DelayedInjectionRunner;
import ch.jalu.injector.testing.InjectDelayed;
import me.gnat008.perworldinventory.DataFolder;
import me.gnat008.perworldinventory.data.players.PWIPlayerFactory;
import me.gnat008.perworldinventory.data.serializers.PlayerSerializer;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static me.gnat008.perworldinventory.TestHelper.mockGroup;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

/**
 * Tests for {@link SetWorldDefaultCommand}.
 */
// TODO Gnat008: Fix tests so they work again
@Ignore
@RunWith(DelayedInjectionRunner.class)
public class SetWorldDefaultCommandTest {

    @InjectDelayed
    private SetWorldDefaultCommand command;

    @Mock
    private GroupManager groupManager;
    @Mock
    private PlayerSerializer playerSerializer;
    @Mock
    private PWIPlayerFactory pwiPlayerFactory;
    @DataFolder
    private File dataFolder;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeInjecting
    public void setUpDataFolder() throws IOException {
        dataFolder = temporaryFolder.newFolder();
    }

    @Test
    public void shouldNotExecuteNotAPlayer() {
        // given
        CommandSender sender = mock(CommandSender.class);

        // when
        command.executeCommand(sender, Collections.emptyList());

        // then
        verify(sender).sendMessage(argThat(containsString("This command may only be run from ingame")));
        // Check that no file was created in the data folder
        assertThat(dataFolder.list(), emptyArray());
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
    }

    @Test
    public void shouldSetForDefaultGroup() {
        // given
        Player player = mock(Player.class);
        List<String> args = Collections.singletonList("serverDefault");

        // when
        command.executeCommand(player, args);

        // then
        // check that expected file was created (or move writing logic to another class ;))
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
        //verify(fileSerializer, only()).setGroupDefault(eq(player), captor.capture());
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
        command.executeCommand(player, Collections.emptyList());

        // then
        ArgumentCaptor<Group> captor = ArgumentCaptor.forClass(Group.class);
        //verify(fileSerializer, only()).setGroupDefault(eq(player), captor.capture());
        assertThat(captor.getValue().getName(), equalTo("test"));
    }
}
