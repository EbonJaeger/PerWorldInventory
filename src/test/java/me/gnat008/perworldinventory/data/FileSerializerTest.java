package me.gnat008.perworldinventory.data;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.data.players.PWIPlayer;
import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link me.gnat008.perworldinventory.data.FileSerializer}
 */
@RunWith(MockitoJUnitRunner.class)
public class FileSerializerTest {

    @InjectMocks
    private FileSerializer fileSerializer;

    @Mock
    private PerWorldInventory plugin;

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
    @Ignore
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
