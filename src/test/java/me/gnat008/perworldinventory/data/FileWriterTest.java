package me.gnat008.perworldinventory.data;

import ch.jalu.injector.testing.BeforeInjecting;
import ch.jalu.injector.testing.DelayedInjectionRunner;
import ch.jalu.injector.testing.InjectDelayed;
import com.google.common.io.Files;
import me.gnat008.perworldinventory.DataFolder;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.TestHelper;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link FileWriter}
 */
@RunWith(DelayedInjectionRunner.class)
// Current test setup not supported by v. 0.2 of injector
@Ignore
public class FileWriterTest {

    @InjectDelayed
    private FileWriter fileSerializer;

    @Mock
    private PerWorldInventory plugin;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @DataFolder
    private File testFolder;

    @BeforeInjecting
    public void setup() throws IOException {
        testFolder = temporaryFolder.newFolder();
        String userDataPath = "data/7f7c909b-24f1-49a4-817f-baa4f4973980/";
        File source = TestHelper.getJarFile(TestHelper.PROJECT_ROOT + userDataPath + "last-logout.json");
        File userFolder = new File(testFolder, userDataPath);
        userFolder.mkdirs();
        File destination = new File(userFolder, "last-logout.json");
        Files.copy(source, destination);

        File data = TestHelper.getJarFile(TestHelper.PROJECT_ROOT + userDataPath + "test-group.json");
        destination = new File(userFolder, "test-group.json");
        Files.copy(data, destination);
    }

    @Test
    public void shouldGetSurvivalFile() {
        // given
        Group group = new Group("test-group", new ArrayList<String>(), GameMode.SURVIVAL);
        GameMode gameMode = GameMode.SURVIVAL;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(UUID.fromString("7f7c909b-24f1-49a4-817f-baa4f4973980"));

        // when
        File result = fileSerializer.getFile(gameMode, group, player.getUuid());

        // then
        assertTrue(result.getName().equals("test-group.json"));
    }

    @Test
    public void shouldGetCreativeFile() {
        // given
        Group group = new Group("test-group", new ArrayList<String>(), GameMode.SURVIVAL);
        GameMode gameMode = GameMode.CREATIVE;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(UUID.fromString("7f7c909b-24f1-49a4-817f-baa4f4973980"));

        // when
        File result = fileSerializer.getFile(gameMode, group, player.getUuid());

        // then
        assertTrue(result.getName().equals("test-group_creative.json"));
    }

    @Test
    public void shouldGetAdventureFile() {
        // given
        Group group = new Group("test-group", new ArrayList<String>(), GameMode.SURVIVAL);
        GameMode gameMode = GameMode.ADVENTURE;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(UUID.fromString("7f7c909b-24f1-49a4-817f-baa4f4973980"));

        // when
        File result = fileSerializer.getFile(gameMode, group, player.getUuid());

        // then
        assertTrue(result.getName().equals("test-group_adventure.json"));
    }

    @Test
    public void shouldGetSpectatorFile() {
        // given
        Group group = new Group("test-group", new ArrayList<String>(), GameMode.SURVIVAL);
        GameMode gameMode = GameMode.SPECTATOR;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(UUID.fromString("7f7c909b-24f1-49a4-817f-baa4f4973980"));

        // when
        File result = fileSerializer.getFile(gameMode, group, player.getUuid());

        // then
        assertTrue(result.getName().equals("test-group_creative.json"));
    }

    @Test
    public void lastLogoutLocationExists() {
        // given
        Player player = mock(Player.class);
        given(player.getUniqueId()).willReturn(UUID.fromString("7f7c909b-24f1-49a4-817f-baa4f4973980"));
        World world = mock(World.class);
        setUpWorldReturnedByBukkit(world);

        // when
        Location result = fileSerializer.getLogoutData(player);

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
        Location result = fileSerializer.getLogoutData(player);

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
        TestHelper.setField(Bukkit.class, "server", null, server);
    }
}
