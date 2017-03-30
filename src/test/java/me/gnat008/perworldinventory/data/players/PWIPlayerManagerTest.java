package me.gnat008.perworldinventory.data.players;

import ch.jalu.injector.testing.BeforeInjecting;
import ch.jalu.injector.testing.DelayedInjectionRunner;
import ch.jalu.injector.testing.InjectDelayed;
import me.gnat008.perworldinventory.BukkitService;
import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.TestHelper;
import me.gnat008.perworldinventory.config.PwiProperties;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.DataSource;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static me.gnat008.perworldinventory.TestHelper.mockGroup;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link PWIPlayerManager}.
 */
@RunWith(DelayedInjectionRunner.class)
public class PWIPlayerManagerTest {

    @InjectDelayed
    private PWIPlayerManager playerManager;

    @InjectDelayed
    private PWIPlayerFactory pwiPlayerFactory;

    @Mock
    private PerWorldInventory plugin;

    @Mock
    private BukkitService bukkitService;

    @Mock
    private DataSource dataSource;

    @Mock
    private GroupManager groupManager;

    @Mock
    private Settings settings;

    @BeforeInjecting
    public void initSettings() {
        given(settings.getProperty(PwiProperties.SAVE_INTERVAL)).willReturn(300);

        // Add mocks for Bukkit.getScheduler, called in @PostConstruct method
        Server server = mock(Server.class);
        TestHelper.setField(Bukkit.class, "server", null, server);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        given(server.getScheduler()).willReturn(scheduler);
    }

    @Test
    public void addPlayerShouldHaveSurvivalKey() {
        // given
        Player player = mockPlayer("playah", GameMode.SURVIVAL);
        Group group = mockGroup("test");
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(true);

        // when
        String result = playerManager.makeKey(player.getUniqueId(), group, GameMode.SURVIVAL);

        // then
        String expected = TestHelper.TEST_UUID + ".test.survival";
        assertThat(result, equalTo(expected));
    }

    @Test
    public void addPlayerShouldHaveSurvivalKeyNoSeparation() {
        // given
        Player player = mockPlayer("player", GameMode.CREATIVE);
        Group group = mockGroup("test");
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(false);

        // when
        String result = playerManager.makeKey(player.getUniqueId(), group, GameMode.CREATIVE);

        // then
        String expected = TestHelper.TEST_UUID + ".test.survival";
        assertThat(result, equalTo(expected));
    }

    @Test
    public void addPlayerShouldHaveCreativeKey() {
        // given
        Player player = mockPlayer("Nicole", GameMode.CREATIVE);
        Group group = mockGroup("test");
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(true);

        // when
        String result = playerManager.makeKey(player.getUniqueId(), group, GameMode.CREATIVE);

        // then
        String expected = TestHelper.TEST_UUID + ".test.creative";
        assertThat(result, equalTo(expected));
    }
    @Test
    public void addPlayerShouldHaveAdventureKey() {
        // given
        Player player = mockPlayer("Bob", GameMode.ADVENTURE);
        Group group = mockGroup("test");
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(true);

        // when
        String result = playerManager.makeKey(player.getUniqueId(), group, GameMode.ADVENTURE);

        // then
        String expected = TestHelper.TEST_UUID + ".test.adventure";
        assertThat(result, equalTo(expected));
    }

    @Test
    public void addPlayerShouldHaveSpectatorKey() {
        // given
        Player player = mockPlayer("someDude", GameMode.SPECTATOR);
        Group group = mockGroup("test");
        given(settings.getProperty(PwiProperties.SEPARATE_GAMEMODE_INVENTORIES)).willReturn(true);

        // when
        String result = playerManager.makeKey(player.getUniqueId(), group, GameMode.SPECTATOR);

        // then
        String expected = TestHelper.TEST_UUID + ".test.spectator";
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
        given(mock.getUniqueId()).willReturn(TestHelper.TEST_UUID);
        given(mock.getGameMode()).willReturn(gameMode);
        AttributeInstance attribute = mock(AttributeInstance.class);
        given(mock.getAttribute(Attribute.GENERIC_MAX_HEALTH)).willReturn(attribute);
        given(attribute.getBaseValue()).willReturn(20.0);

        return mock;
    }
}
