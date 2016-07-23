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
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link FileWriter}
 */
@RunWith(DelayedInjectionRunner.class)
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
        File source = TestHelper.getJarFile(TestHelper.PROJECT_ROOT + "data/7f7c909b-24f1-49a4-817f-baa4f4973980/last-logout.json");
        File destination = new File(testFolder, "last-logout.json");
        Files.copy(source, destination);
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

        // when
        Location result = fileSerializer.getLogoutData(player);

        // then
        assertTrue(result != null);
    }
}
