package me.gnat008.perworldinventory.listeners.entity;

import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Pig;
import org.bukkit.event.entity.EntityPortalEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link EntityPortalEventListener}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityPortalEventListenerTest {

    @InjectMocks
    private EntityPortalEventListener listener;

    @Mock
    private GroupManager groupManager;

    @Test
    public void shouldTeleportBecauseNotItem() {
        // given
        Pig entity = mock(Pig.class);
        World world = mock(World.class);
        Location from = new Location(world, 1, 2, 3);
        World worldNether = mock(World.class);
        Location to = new Location(worldNether, 1, 2, 3);
        EntityPortalEvent event = new EntityPortalEvent(entity, from, to, mock(TravelAgent.class));

        // when
        listener.onEntityPortalTeleport(event);

        // then
        assertThat(event.isCancelled(), equalTo(false));
    }

    @Test
    public void shouldTeleportBecauseSameGroup() {
        // given
        Group group = mockGroup("test_group", GameMode.SURVIVAL, false);

        Item entity = mock(Item.class);

        World world = mock(World.class);
        given(world.getName()).willReturn("test_group");
        Location from = new Location(world, 1, 2, 3);

        World worldNether = mock(World.class);
        given(worldNether.getName()).willReturn("test_group_nether");
        Location to = new Location(worldNether, 1, 2, 3);

        given(groupManager.getGroupFromWorld("test_group")).willReturn(group);
        given(groupManager.getGroupFromWorld("test_group_nether")).willReturn(group);

        EntityPortalEvent event = new EntityPortalEvent(entity, from, to, mock(TravelAgent.class));

        // when
        listener.onEntityPortalTeleport(event);

        // then
        assertThat(event.isCancelled(), equalTo(false));
    }

    @Test
    public void shouldNotTeleportBecauseDifferentGroups() {
        // given
        Group group = mockGroup("test_group", GameMode.SURVIVAL, false);
        Group otherGroup = mockGroup("other_group", GameMode.SURVIVAL, false);

        Item entity = mock(Item.class);

        World world = mock(World.class);
        given(world.getName()).willReturn("test_group");
        Location from = new Location(world, 1, 2, 3);

        World worldNether = mock(World.class);
        given(worldNether.getName()).willReturn("other_group_nether");
        Location to = new Location(worldNether, 1, 2, 3);

        given(groupManager.getGroupFromWorld("test_group")).willReturn(group);
        given(groupManager.getGroupFromWorld("other_group_nether")).willReturn(otherGroup);

        EntityPortalEvent event = new EntityPortalEvent(entity, from, to, mock(TravelAgent.class));

        // when
        listener.onEntityPortalTeleport(event);

        // then
        assertThat(event.isCancelled(), equalTo(true));
    }

    private Group mockGroup(String name, GameMode gameMode, boolean configured) {
        Set<String> worlds = new HashSet<>();
        worlds.add(name);
        worlds.add(name + "_nether");

        return new Group(name, worlds, gameMode, configured);
    }
}
