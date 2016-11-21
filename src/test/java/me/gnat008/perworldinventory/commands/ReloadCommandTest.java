package me.gnat008.perworldinventory.commands;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

/**
 * Tests for {@link ReloadCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReloadCommandTest {

    @InjectMocks
    private ReloadCommand command;

    @Mock
    private PerWorldInventory plugin;

    @Mock
    private GroupManager groupManager;

    @Mock
    private Settings settings;

    @Test
    public void shouldPerformReload() {
        // given
        Player player = mock(Player.class);
        FileConfiguration worldsConfig = mock(FileConfiguration.class);
        given(plugin.getWorldsConfig()).willReturn(worldsConfig);

        // when
        command.executeCommand(player, Collections.emptyList());

        // then
        verify(player).sendMessage(argThat(containsString("Configuration files reloaded")));
        verify(settings).reload();
        verify(plugin).reload();
        verify(groupManager).loadGroupsToMemory(worldsConfig);
    }
}
