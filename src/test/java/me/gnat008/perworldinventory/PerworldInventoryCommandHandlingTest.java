package me.gnat008.perworldinventory;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import me.gnat008.perworldinventory.commands.ConvertCommand;
import me.gnat008.perworldinventory.commands.HelpCommand;
import me.gnat008.perworldinventory.commands.PerWorldInventoryCommand;
import me.gnat008.perworldinventory.commands.ReloadCommand;
import me.gnat008.perworldinventory.commands.SetWorldDefaultCommand;
import me.gnat008.perworldinventory.commands.VersionCommand;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

import static me.gnat008.perworldinventory.TestHelper.setField;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Test for the command handling in {@link PerWorldInventory}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PerworldInventoryCommandHandlingTest {

    private PerWorldInventory plugin;

    @Mock
    private PluginLoader pluginLoader;
    @Mock
    private Server server;

    @Mock
    private PerWorldInventoryCommand pwiCommand;
    @Mock
    private ConvertCommand convertCommand;
    @Mock
    private HelpCommand helpCommand;
    @Mock
    private ReloadCommand reloadCommand;
    @Mock
    private SetWorldDefaultCommand setWorldDefaultsCommand;
    @Mock
    private VersionCommand versionCommand;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUpPlugin() throws IOException {
        File dataFolder = temporaryFolder.newFolder();

        // Set mock server
        setField(Bukkit.class, "server", null, server);
        given(server.getLogger()).willReturn(mock(Logger.class));

        // PluginDescriptionFile is final and so cannot be mocked
        PluginDescriptionFile descriptionFile = new PluginDescriptionFile(
            "PerWorldInventory", "N/A", PerWorldInventory.class.getCanonicalName());
        plugin = new PerWorldInventory(pluginLoader, server, descriptionFile, dataFolder, null);
        setField(JavaPlugin.class, "logger", plugin, mock(PluginLogger.class));

        Injector injector = new InjectorBuilder().addDefaultHandlers("me.gnat008.perworldinventory").create();
        injector.register(ConvertCommand.class, convertCommand);
        injector.register(HelpCommand.class, helpCommand);
        injector.register(PerWorldInventoryCommand.class, pwiCommand);
        injector.register(ReloadCommand.class, reloadCommand);
        injector.register(SetWorldDefaultCommand.class, setWorldDefaultsCommand);
        injector.register(VersionCommand.class, versionCommand);
        plugin.registerCommands(injector);
    }

    @Test
    public void shouldRunMainCommand() {
        // given
        CommandSender sender = mock(CommandSender.class);
        Command command = newCommandWithName("pwi");

        // when
        boolean result = plugin.onCommand(sender, command, "pwi");

        // then
        assertThat(result, equalTo(true));
        verify(pwiCommand).executeCommand(sender, Collections.emptyList());
    }

    @Test
    public void shouldRunVersionCommand() {
        // given
        CommandSender sender = mock(CommandSender.class);
        Command command = newCommandWithName("Pwi");

        // when
        boolean result = plugin.onCommand(sender, command, "pwi", "convert", "abc", "def");

        // then
        assertThat(result, equalTo(true));
        verify(convertCommand).executeCommand(sender, Arrays.asList("abc", "def"));
    }

    @Test
    public void shouldFallbackToMainCommand() {
        // given
        CommandSender sender = mock(CommandSender.class);
        Command command = newCommandWithName("pWI");

        // when
        boolean result = plugin.onCommand(sender, command, "Pwi", "invalid", "123", "456");

        // then
        assertThat(result, equalTo(true));
        verify(pwiCommand).executeCommand(sender, Collections.emptyList());
    }

    @Test
    public void shouldHandleUnknownCommand() {
        // given
        CommandSender sender = mock(CommandSender.class);
        Command command = newCommandWithName("bogus");

        // when
        boolean result = plugin.onCommand(sender, command, "bogus");

        // then
        assertThat(result, equalTo(false));
        verifyZeroInteractions(pwiCommand);
    }

    private static Command newCommandWithName(String name) {
        Command command = mock(Command.class);
        given(command.getName()).willReturn(name);
        return command;
    }

}
