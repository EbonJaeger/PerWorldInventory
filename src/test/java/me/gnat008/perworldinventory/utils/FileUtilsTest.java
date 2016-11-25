package me.gnat008.perworldinventory.utils;

import me.gnat008.perworldinventory.TestHelper;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.GameMode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static me.gnat008.perworldinventory.utils.FileUtils.getFile;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link FileUtils}.
 */
public class FileUtilsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldGetSurvivalFile() throws IOException {
        // given
        Group group = new Group("test-group", new ArrayList<String>(), GameMode.SURVIVAL);
        GameMode gameMode = GameMode.SURVIVAL;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(TestHelper.TESTING_UUID);

        // when
        File result = getFile(temporaryFolder.newFolder(), gameMode, group, player.getUuid());

        // then
        assertTrue(result.getName().equals("test-group.json"));
    }

    @Test
    public void shouldGetCreativeFile() throws IOException {
        // given
        Group group = new Group("test-group", new ArrayList<String>(), GameMode.SURVIVAL);
        GameMode gameMode = GameMode.CREATIVE;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(TestHelper.TESTING_UUID);

        // when
        File result = getFile(temporaryFolder.newFolder(), gameMode, group, player.getUuid());

        // then
        assertTrue(result.getName().equals("test-group_creative.json"));
    }

    @Test
    public void shouldGetAdventureFile() throws IOException {
        // given
        Group group = new Group("test-group", new ArrayList<String>(), GameMode.SURVIVAL);
        GameMode gameMode = GameMode.ADVENTURE;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(TestHelper.TESTING_UUID);

        // when
        File result = getFile(temporaryFolder.newFolder(), gameMode, group, player.getUuid());

        // then
        assertTrue(result.getName().equals("test-group_adventure.json"));
    }

    @Test
    public void shouldGetSpectatorFile() throws IOException {
        // given
        Group group = new Group("test-group", new ArrayList<String>(), GameMode.SURVIVAL);
        GameMode gameMode = GameMode.SPECTATOR;
        PWIPlayer player = mock(PWIPlayer.class);
        given(player.getUuid()).willReturn(TestHelper.TESTING_UUID);

        // when
        File result = getFile(temporaryFolder.newFolder(), gameMode, group, player.getUuid());

        // then
        assertTrue(result.getName().equals("test-group_creative.json"));
    }
}
