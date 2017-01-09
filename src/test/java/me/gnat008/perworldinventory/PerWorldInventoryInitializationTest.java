package me.gnat008.perworldinventory;

import ch.jalu.configme.properties.Property;
import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import me.gnat008.perworldinventory.commands.ExecutableCommand;
import me.gnat008.perworldinventory.commands.PerWorldInventoryCommand;
import me.gnat008.perworldinventory.commands.ReloadCommand;
import me.gnat008.perworldinventory.commands.SetWorldDefaultCommand;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.DataWriter;
import me.gnat008.perworldinventory.data.FileWriter;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.listeners.player.PlayerGameModeChangeListener;
import me.gnat008.perworldinventory.listeners.server.PluginListener;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import static me.gnat008.perworldinventory.TestHelper.getField;
import static me.gnat008.perworldinventory.TestHelper.setField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

/**
 * Test for the initialization in {@link PerWorldInventory}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PerWorldInventoryInitializationTest {

    private PerWorldInventory plugin;

    @Mock
    private PluginManager pluginManager;

    @Mock
    private Server server;

    @Mock
    private Settings settings;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File dataFolder;

    @Before
    public void setUpPlugin() throws IOException {
        dataFolder = temporaryFolder.newFolder();

        // Wire various Bukkit components
        setField(Bukkit.class, "server", null, server);
        given(server.getLogger()).willReturn(mock(Logger.class));
        given(server.getScheduler()).willReturn(mock(BukkitScheduler.class));
        given(server.getPluginManager()).willReturn(pluginManager);
        given(server.getVersion()).willReturn("1.9.4-RC1");

        // SettingsManager always returns the default
        given(settings.getProperty(any(Property.class))).willAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return ((Property<?>) invocation.getArguments()[0]).getDefaultValue();
            }
        });

        // PluginDescriptionFile is final and so cannot be mocked
        PluginDescriptionFile descriptionFile = new PluginDescriptionFile(
            "PerWorldInventory", "N/A", PerWorldInventory.class.getCanonicalName());
        JavaPluginLoader pluginLoader = new JavaPluginLoader(server);
        plugin = new PerWorldInventory(pluginLoader, descriptionFile, dataFolder, null);
        setField(JavaPlugin.class, "logger", plugin, mock(PluginLogger.class));
    }

    @Test
    public void shouldInitializeAllServices() {
        // given
        Injector injector = new InjectorBuilder().addDefaultHandlers("me.gnat008.perworldinventory").create();
        injector.register(PerWorldInventory.class, plugin);
        injector.register(Server.class, server);
        injector.register(PluginManager.class, pluginManager);
        injector.provide(DataFolder.class, dataFolder);
        injector.register(Settings.class, settings);

        // when
        plugin.injectServices(injector);
        plugin.registerEventListeners(injector);
        plugin.registerCommands(injector);

        // then - check various samples
        assertThat(injector.getIfAvailable(GroupManager.class), not(nullValue()));
        assertThat(injector.getIfAvailable(DataWriter.class), instanceOf(FileWriter.class));
        assertThat(injector.getIfAvailable(PWIPlayerManager.class), not(nullValue()));

        verifyRegisteredListener(PluginListener.class);
        verifyRegisteredListener(PlayerGameModeChangeListener.class);

        CommandVerifier commandVerifier = new CommandVerifier(plugin, injector);
        commandVerifier.assertHasCommand("pwi", PerWorldInventoryCommand.class);
        commandVerifier.assertHasCommand("reload", ReloadCommand.class);
        commandVerifier.assertHasCommand("setworlddefault", SetWorldDefaultCommand.class);
    }

    private void verifyRegisteredListener(Class<? extends Listener> listenerClass) {
        verify(pluginManager).registerEvents(
            argThat(instanceOf(listenerClass)), eq(plugin));
    }


    private static final class CommandVerifier {

        private final Injector injector;
        private final Map<String, ExecutableCommand> commands;

        CommandVerifier(PerWorldInventory plugin, Injector injector) {
            this.injector = injector;
            this.commands = getField(PerWorldInventory.class, "commands", plugin);
        }

        void assertHasCommand(String label, Class<? extends ExecutableCommand> expectedClass) {
            ExecutableCommand command = commands.get(label);
            assertThat(command, not(nullValue()));
            assertThat(command, sameInstance(injector.getIfAvailable(expectedClass)));
        }
    }

}
