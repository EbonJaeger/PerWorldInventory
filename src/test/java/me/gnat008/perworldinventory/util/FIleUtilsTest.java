package me.gnat008.perworldinventory.util;

import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.GameMode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static me.gnat008.perworldinventory.TestHelper.mockGroup;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link FileUtils}.
 */
public class FIleUtilsTest {

    private static final UUID UUID = java.util.UUID.randomUUID();

    private File testDirectory;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        testDirectory = temporaryFolder.newFolder();
    }

    @Test
    public void shouldGetSurvivalFile() {
        // given
        Group group = mockGroup("test-group");
        GameMode gameMode = GameMode.SURVIVAL;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(UUID);

        // when
        File result = FileUtils.getFile(testDirectory, gameMode, group);

        // then
        assertTrue(result.getName().equals("test-group.json"));
    }

    @Test
    public void shouldGetCreativeFile() {
        // given
        Group group = mockGroup("test-group");
        GameMode gameMode = GameMode.CREATIVE;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(UUID);

        // when
        File result = FileUtils.getFile(testDirectory, gameMode, group);

        // then
        assertTrue(result.getName().equals("test-group_creative.json"));
    }

    @Test
    public void shouldGetAdventureFile() {
        // given
        Group group = mockGroup("test-group");
        GameMode gameMode = GameMode.ADVENTURE;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(UUID);

        // when
        File result = FileUtils.getFile(testDirectory, gameMode, group);

        // then
        assertTrue(result.getName().equals("test-group_adventure.json"));
    }

    @Test
    public void shouldGetSpectatorFile() {
        // given
        Group group = mockGroup("test-group");
        GameMode gameMode = GameMode.SPECTATOR;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(UUID);

        // when
        File result = FileUtils.getFile(testDirectory, gameMode, group);

        // then
        assertTrue(result.getName().equals("test-group_creative.json"));
    }
}
