package me.gnat008.perworldinventory.data.players;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.SettingsMocker;
import me.gnat008.perworldinventory.data.DataWriter;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link PWIPlayerManager}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PWIPlayerManagerTest {

    @InjectMocks
    private PWIPlayerManager playerManager;

    @Mock
    private PerWorldInventory plugin;

    @Mock
    private DataWriter dataWriter;

    @Mock
    private GroupManager groupManager;

    private final static UUID TEST_UUID = UUID.randomUUID();

    @Test
    public void addPlayerShouldHaveSurvivalKey() {
        // given
        Player player = mockPlayer("playah", GameMode.SURVIVAL);
        Group group = new Group("test", new ArrayList<String>(), GameMode.SURVIVAL);
        SettingsMocker.create()
                .set("separate-gamemode-inventories", true)
                .save();

        // when
        String result = playerManager.addPlayer(player, group);

        // then
        String expected = TEST_UUID.toString() + ".test.survival";
        assertThat(result, equalTo(expected));
    }

    @Test
    public void addPlayerShouldHaveSurvivalKeyNoSeparation() {
        // given
        Player player = mockPlayer("playah", GameMode.CREATIVE);
        Group group = new Group("test", new ArrayList<String>(), GameMode.SURVIVAL);
        SettingsMocker.create()
                .set("separate-gamemode-inventories", false)
                .save();

        // when
        String result = playerManager.addPlayer(player, group);

        // then
        String expected = TEST_UUID.toString() + ".test.survival";
        assertThat(result, equalTo(expected));
    }

    @Test
    public void addPlayerShouldHaveCreativeKey() {
        // given
        Player player = mockPlayer("playah", GameMode.CREATIVE);
        Group group = new Group("test", new ArrayList<String>(), GameMode.SURVIVAL);
        SettingsMocker.create()
                .set("separate-gamemode-inventories", true)
                .save();

        // when
        String result = playerManager.addPlayer(player, group);

        // then
        String expected = TEST_UUID.toString() + ".test.creative";
        assertThat(result, equalTo(expected));
    }
    @Test
    public void addPlayerShouldHaveAdventureKey() {
        // given
        Player player = mockPlayer("playah", GameMode.ADVENTURE);
        Group group = new Group("test", new ArrayList<String>(), GameMode.SURVIVAL);
        SettingsMocker.create()
                .set("separate-gamemode-inventories", true)
                .save();

        // when
        String result = playerManager.addPlayer(player, group);

        // then
        String expected = TEST_UUID.toString() + ".test.adventure";
        assertThat(result, equalTo(expected));
    }

    @Test
    public void addPlayerShouldHaveSpectatorKey() {
        // given
        Player player = mockPlayer("playah", GameMode.SPECTATOR);
        Group group = new Group("test", new ArrayList<String>(), GameMode.SURVIVAL);
        SettingsMocker.create()
                .set("separate-gamemode-inventories", true)
                .save();

        // when
        String result = playerManager.addPlayer(player, group);

        // then
        String expected = TEST_UUID.toString() + ".test.spectator";
        assertThat(result, equalTo(expected));
    }

    private Player mockPlayer(String name, GameMode gameMode) {
        Player mock = mock(Player.class);
        PlayerInventory inv = mock(PlayerInventory.class);
        inv.setContents(new ItemStack[39]);
        inv.setArmorContents(new ItemStack[4]);

        Inventory enderChest = mock(Inventory.class);
        enderChest.setContents(new ItemStack[27]);

        given(mock.getInventory()).willReturn(inv);
        given(mock.getEnderChest()).willReturn(enderChest);
        given(mock.getName()).willReturn(name);
        given(mock.getUniqueId()).willReturn(TEST_UUID);
        given(mock.getGameMode()).willReturn(gameMode);
        given(plugin.isEconEnabled()).willReturn(false);

        return mock;
    }
}
