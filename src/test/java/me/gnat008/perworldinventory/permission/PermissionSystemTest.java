package me.gnat008.perworldinventory.permission;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;

/**
 * Test for {@link PermissionSystem).}
 */
public class PermissionSystemTest {

    @Test
    public void shouldHaveDefinedAndUniqueNames() {
        // given / when / then
        List<String> names = new ArrayList<>(PermissionSystem.values().length);
        List<String> pluginNames = new ArrayList<>(PermissionSystem.values().length);

        for (PermissionSystem system : PermissionSystem.values()) {
            assertThat("Name for enum entry '" + system + "' is not null",
                    system.getName(), not(nullValue()));
            assertThat("Plugin name for enum entry '" + system + "' is not null",
                    system.getPluginName(), not(nullValue()));
            assertThat("Only one enum entry has name '" + system.getName() + "'",
                    names, not(hasItem(system.getName())));
            assertThat("Only one enum entry has plugin name '" + system.getPluginName() + "'",
                    pluginNames, not(hasItem(system.getPluginName())));
            names.add(system.getName());
            pluginNames.add(system.getPluginName());
        }
    }

    @Test
    public void shouldRecognizePermissionSystemType() {
        assertThat(PermissionSystem.isPermissionSystem("bogus"), equalTo(false));
        assertThat(PermissionSystem.isPermissionSystem("PermissionsBukkit"), equalTo(true));
    }
}
