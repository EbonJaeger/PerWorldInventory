package me.gnat008.perworldinventory.groups;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.Settings;
import org.bukkit.GameMode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import static me.gnat008.perworldinventory.TestHelper.mockGroup;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link GroupManager}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GroupManagerTest {

    @InjectMocks
    private GroupManager groupManager;

    @Mock
    private PerWorldInventory plugin;

    @Mock
    private Settings settings;

    @Test
    public void addGroupWithLowercaseName() {
        // given
        String name = "test";
        Set<String> worlds = new HashSet<>();
        worlds.add(name);
        GameMode gameMode = GameMode.SURVIVAL;

        // when
        groupManager.addGroup(name, worlds, gameMode);

        // then
        Group expected = mockGroup(name, worlds, gameMode);
        Group actual = groupManager.getGroup("test");

        assertThat(actual.getName(), equalTo(expected.getName()));
        assertThat(actual.getWorlds(), equalTo(expected.getWorlds()));
        assertThat(actual.getGameMode(), equalTo(expected.getGameMode()));
    }

    @Test
    public void addGroupWithUppercaseName() {
        // given
        String name = "TeSt";
        Set<String> worlds = new HashSet<>();
        worlds.add(name);
        GameMode gameMode = GameMode.SURVIVAL;

        // when
        groupManager.addGroup(name, worlds, gameMode);

        // then
        Group expected = mockGroup(name, worlds, gameMode);
        Group actual = groupManager.getGroup("TeSt");

        assertThat(actual.getName(), equalTo(expected.getName()));
        assertThat(actual.getWorlds(), equalTo(expected.getWorlds()));
        assertThat(actual.getGameMode(), equalTo(expected.getGameMode()));
    }

    @Test
    public void addGroupWithUppercaseNameLowercaseGet() {
        // given
        String name = "TeSt";
        Set<String> worlds = new HashSet<>();
        worlds.add(name);
        GameMode gameMode = GameMode.SURVIVAL;

        // when
        groupManager.addGroup(name, worlds, gameMode);

        // then
        Group expected = mockGroup(name, worlds, gameMode);
        Group actual = groupManager.getGroup("test");

        assertThat(actual.getName(), equalTo(expected.getName()));
        assertThat(actual.getWorlds(), equalTo(expected.getWorlds()));
        assertThat(actual.getGameMode(), equalTo(expected.getGameMode()));
    }

    @Test
    public void getGroupFromWorldWhereExists() {
        // given
        String name = "test";
        Set<String> worlds = new HashSet<>();
        worlds.add(name);
        GameMode gameMode = GameMode.SURVIVAL;

        groupManager.addGroup(name, worlds, gameMode);

        // when
        Group result = groupManager.getGroupFromWorld("test");

        // then
        Group expected = mockGroup(name, worlds, gameMode);

        assertThat(result.getName(), equalTo(expected.getName()));
        assertThat(result.getWorlds(), equalTo(expected.getWorlds()));
        assertThat(result.getGameMode(), equalTo(expected.getGameMode()));
    }

    @Test
    public void getGroupFromWorldWhereNotExists() {
        // given
        String name = "test";
        Set<String> worlds = new HashSet<>();
        worlds.add(name);
        worlds.add(name + "_nether");
        worlds.add(name + "_the_end");
        GameMode gameMode = GameMode.SURVIVAL;

        // when
        Group result = groupManager.getGroupFromWorld("test");

        // then
        Group expected = mockGroup(name, worlds, gameMode);

        assertNotNull(result);
        assertThat(result.getName(), equalTo(expected.getName()));
        assertThat(result.getWorlds(), equalTo(expected.getWorlds()));
        assertThat(result.getGameMode(), equalTo(expected.getGameMode()));
    }
}
