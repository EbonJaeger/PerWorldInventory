package me.gnat008.perworldinventory.data;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import ch.jalu.injector.testing.InjectDelayed;
import com.google.common.io.Files;
import me.gnat008.perworldinventory.DataFolder;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.ReflectionTestUtils;
import me.gnat008.perworldinventory.TestHelper;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static me.gnat008.perworldinventory.TestHelper.mockGroup;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link FlatFile}
 */
@RunWith(MockitoJUnitRunner.class)
public class FlatFileTest {

    private static final UUID UUID_WITH_DATA = UUID.fromString("7f7c909b-24f1-49a4-817f-baa4f4973980");

    @InjectDelayed
    private FlatFile flatFile;

    @Mock
    private PerWorldInventory plugin;
    @Mock
    private Settings settings;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        File testFolder = new File(temporaryFolder.newFolder(), "data/");
        String userDataPath = "7f7c909b-24f1-49a4-817f-baa4f4973980/";
        File source = TestHelper.getJarFile(TestHelper.PROJECT_ROOT + "data/" + userDataPath + "last-logout.json");
        File userFolder = new File(testFolder, userDataPath);
        userFolder.mkdirs();
        File destination = new File(userFolder, "last-logout.json");
        Files.copy(source, destination);

        File data = TestHelper.getJarFile(TestHelper.PROJECT_ROOT + "data/" + userDataPath + "test-group.json");
        destination = new File(userFolder, "test-group.json");
        Files.copy(data, destination);

        // Injector is restricted to creating classes only in 'data' package:
        // ensures that anything else that is required has to be provided explicitly
        Injector injector = new InjectorBuilder().addDefaultHandlers("me.gnat008.perworldinventory.data").create();
        injector.provide(DataFolder.class, testFolder);
        injector.register(PerWorldInventory.class, plugin);
        injector.register(Settings.class, settings);
        flatFile = injector.getSingleton(FlatFile.class);
    }

    @Test
    public void shouldGetSurvivalFile() {
        // given
        Group group = mockGroup("test-group");
        GameMode gameMode = GameMode.SURVIVAL;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(UUID_WITH_DATA);

        // when
        File result = flatFile.getFile(gameMode, group, player.getUuid());

        // then
        assertTrue(result.getName().equals("test-group.json"));
    }

    @Test
    public void shouldGetCreativeFile() {
        // given
        Group group = mockGroup("test-group");
        GameMode gameMode = GameMode.CREATIVE;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(UUID_WITH_DATA);

        // when
        File result = flatFile.getFile(gameMode, group, player.getUuid());

        // then
        assertTrue(result.getName().equals("test-group_creative.json"));
    }

    @Test
    public void shouldGetAdventureFile() {
        // given
        Group group = mockGroup("test-group");
        GameMode gameMode = GameMode.ADVENTURE;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(UUID_WITH_DATA);

        // when
        File result = flatFile.getFile(gameMode, group, player.getUuid());

        // then
        assertTrue(result.getName().equals("test-group_adventure.json"));
    }

    @Test
    public void shouldGetSpectatorFile() {
        // given
        Group group = mockGroup("test-group");
        GameMode gameMode = GameMode.SPECTATOR;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(UUID_WITH_DATA);

        // when
        File result = flatFile.getFile(gameMode, group, player.getUuid());

        // then
        assertTrue(result.getName().equals("test-group_creative.json"));
    }

    @Test
    public void lastLogoutLocationExists() {
        // given
        Player player = mock(Player.class);
        given(player.getUniqueId()).willReturn(TestHelper.TESTING_UUID);
        World world = mock(World.class);
        setUpWorldReturnedByBukkit(world);

        // when
        Location result = flatFile.getLogoutData(player);

        // then
        assertTrue(result != null);
        assertTrue(result.getWorld().equals(world));
    }

    @Test
    public void lastLogoutLocationDoesNotExist() {
        // given
        Player player = mock(Player.class);
        UUID randUUID = UUID.randomUUID();
        given(player.getUniqueId()).willReturn(randUUID);

        // when
        Location result = flatFile.getLogoutData(player);

        // then
        assertTrue(result == null);
    }

    /**
     * Sets the {@link Server} field in the Bukkit class with a mock and makes it return
     * the given World object for {@link Bukkit#getWorld(String)}.
     */
    private static void setUpWorldReturnedByBukkit(World world) {
        Server server = mock(Server.class);
        given(server.getWorld(anyString())).willReturn(world);
        ReflectionTestUtils.setField(Bukkit.class, "server", null, server);
    }
}
