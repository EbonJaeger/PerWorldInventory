package me.gnat008.perworldinventory.commands;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import me.gnat008.perworldinventory.service.ConvertService;
import org.bukkit.command.CommandSender;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

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
    private ConvertService convertService;

    @Test
    public void shouldNotExecuteNotEnoughArgs() {
        // given
        CommandSender sender = mock(CommandSender.class);

        // when
        command.executeCommand(sender, Collections.<String>emptyList());

        // then
        verify(sender).sendMessage(argThat(containsString("Incorrect usage")));
    }

    @Test
    public void shouldNotExecuteTooManyArgs() {
        // given
        CommandSender sender = mock(CommandSender.class);
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
        List<String> args = Collections.singletonList("multiverse");
        MultiverseInventories mvi = mock(MultiverseInventories.class);
        given(pluginManager.getPlugin("Multiverse-Inventories")).willReturn(mvi);
        given(pluginManager.isPluginEnabled("Multiverse-Inventories")).willReturn(true);

        // when
        command.executeCommand(sender, args);
        // then
        verify(sender).sendMessage(argThat(containsString("Converting from Multiverse-Inventories")));
        verify(convertService, only()).runConversion(sender);
    }

    @Test
    public void shouldExecuteMultiInvConversion() {
        // given
        CommandSender sender = mock(CommandSender.class);
        List<String> args = Collections.singletonList("multiinv");
        MultiInv multiInv = mock(MultiInv.class);
        given(pluginManager.getPlugin("MultiInv")).willReturn(multiInv);
        given(pluginManager.isPluginEnabled("MultiInv")).willReturn(true);

        // when
        command.executeCommand(sender, args);

        // then
        verify(sender).sendMessage(argThat(containsString("Converting from MultiInv is unsupported")));
    }
}
