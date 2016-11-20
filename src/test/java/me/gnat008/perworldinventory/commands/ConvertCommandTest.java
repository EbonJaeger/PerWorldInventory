package me.gnat008.perworldinventory.commands;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import me.gnat008.perworldinventory.data.converters.DataConverter;
import me.gnat008.perworldinventory.permission.AdminPermission;
import me.gnat008.perworldinventory.permission.PermissionManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.*;

/**
 * Test for {@link ConvertCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConvertCommandTest {

    @InjectMocks
    private ConvertCommand command;

    @Mock
    private PluginManager pluginManager;

    @Mock
    private DataConverter dataConverter;

    @Mock
    private PermissionManager permissionManager;

    @Test
    public void shouldNotExecuteNoPermission() {
        // given
        Player player = mock(Player.class);
        given(permissionManager.hasPermission(player, AdminPermission.CONVERT)).willReturn(false);

        // when
        command.executeCommand(player, Collections.<String>emptyList());

        // then
        verify(player).sendMessage(argThat(containsString("You do not have permission to do that")));
    }

    @Test
    public void shouldNotExecuteNotEnoughArgs() {
        // given
        CommandSender sender = mock(CommandSender.class);
        given(permissionManager.hasPermission(sender, AdminPermission.CONVERT)).willReturn(true);

        // when
        command.executeCommand(sender, Collections.<String>emptyList());

        // then
        verify(sender).sendMessage(argThat(containsString("Incorrect usage")));
    }

    @Test
    public void shouldNotExecuteTooManyArgs() {
        // given
        CommandSender sender = mock(CommandSender.class);
        given(permissionManager.hasPermission(sender, AdminPermission.CONVERT)).willReturn(true);
        List<String> args = new ArrayList<>();
        args.add("1");
        args.add("2");

        // when
        command.executeCommand(sender, args);

        // then
        verify(sender).sendMessage(argThat(containsString("Incorrect usage")));
    }

    @Test
    public void shouldNotExecuteWrongArgs() {
        // given
        CommandSender sender = mock(CommandSender.class);
        given(permissionManager.hasPermission(sender, AdminPermission.CONVERT)).willReturn(true);
        List<String> args = new ArrayList<>();
        args.add("1");

        // when
        command.executeCommand(sender, args);

        // then
        verify(sender).sendMessage(argThat(containsString("Invalid argument")));
    }

    @Test
    public void shouldExecuteMultiverseConversion() {
        // given
        CommandSender sender = mock(CommandSender.class);
        given(permissionManager.hasPermission(sender, AdminPermission.CONVERT)).willReturn(true);
        List<String> args = new ArrayList<>();
        args.add("multiverse");
        MultiverseInventories mvi = mock(MultiverseInventories.class);
        given(pluginManager.getPlugin("Multiverse-Inventories")).willReturn(mvi);
        given(pluginManager.isPluginEnabled("Multiverse-Inventories")).willReturn(true);

        // when
        command.executeCommand(sender, args);
        // then
        verify(sender).sendMessage(argThat(containsString("Converting from Multiverse-Inventories")));
        verify(dataConverter, only()).convertMultiVerseData();
    }

    @Test
    public void shouldExecuteMultiInvConversion() {
        // given
        CommandSender sender = mock(CommandSender.class);
        given(permissionManager.hasPermission(sender, AdminPermission.CONVERT)).willReturn(true);
        List<String> args = new ArrayList<>();
        args.add("multiinv");
        MultiInv multiInv = mock(MultiInv.class);
        given(pluginManager.getPlugin("MultiInv")).willReturn(multiInv);
        given(pluginManager.isPluginEnabled("MultiInv")).willReturn(true);

        // when
        command.executeCommand(sender, args);

        // then
        verify(sender).sendMessage(argThat(containsString("Converting from MultiInv is unsupported")));
    }
}
