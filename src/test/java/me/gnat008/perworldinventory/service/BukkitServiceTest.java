package me.gnat008.perworldinventory.service;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.ReflectionTestUtils;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link BukkitService}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BukkitServiceTest {

    private BukkitService bukkitService;

    @Mock
    private PerWorldInventory plugin;

    @Mock
    private Server server;

    @Mock
    private BukkitScheduler scheduler;

    @Before
    public void setupBukkitServices() {
        ReflectionTestUtils.setField(Bukkit.class, null, "server", server);
        given(Bukkit.getScheduler()).willReturn(scheduler);

        bukkitService = new BukkitService(plugin);
    }

    @Test
    public void shouldRunSyncTaskDirectly() {
        // given
        Runnable task = () -> { /* empty */ };
        BukkitTask bukkitTask = mock(BukkitTask.class);
        given(scheduler.runTask(plugin, task)).willReturn(bukkitTask);

        // when
        BukkitTask result = bukkitService.runTask(task);

        // then
        assertThat(result, equalTo(bukkitTask));
        verify(scheduler, only()).runTask(plugin, task);
    }

    @Test
    public void shouldRunRepeatingTask() {
        // given
        Runnable task = () -> { /* nada */ };
        int delay = 20;
        int period = 60;
        BukkitTask bukkitTask = mock(BukkitTask.class);
        given(scheduler.runTaskTimer(plugin, task, delay, period)).willReturn(bukkitTask);

        // when
        BukkitTask result = bukkitService.runRepeatingTask(task, delay, period);

        // then
        assertThat(result, equalTo(bukkitTask));
        verify(scheduler, only()).runTaskTimer(plugin, task, delay, period);
    }

    @Test
    public void shouldRunAsyncTaskDirectly() {
        // given
        Runnable task = () -> { /* nothin doin */ };
        BukkitTask bukkitTask = mock(BukkitTask.class);
        given(scheduler.runTaskAsynchronously(plugin, task)).willReturn(bukkitTask);

        // when
        BukkitTask result = bukkitService.runTaskAsync(task);

        // then
        assertThat(result, equalTo(bukkitTask));
        verify(scheduler, only()).runTaskAsynchronously(plugin, task);
    }

    @Test
    public void shouldRunTaskOptionallySync() {
        // given
        Runnable task = () -> { /* weee */ };
        BukkitTask bukkitTask = mock(BukkitTask.class);
        given(scheduler.runTask(plugin, task)).willReturn(bukkitTask);

        // when
        BukkitTask result = bukkitService.runTaskOptionallyAsync(task, false);

        // then
        assertThat(result, equalTo(bukkitTask));
        verify(scheduler, only()).runTask(plugin, task);
    }

    @Test
    public void shouldRunTaskOptionallyAsync() {
        // given
        Runnable task = () -> { /* still going */ };
        BukkitTask bukkitTask = mock(BukkitTask.class);
        given(scheduler.runTaskAsynchronously(plugin, task)).willReturn(bukkitTask);

        // when
        BukkitTask result = bukkitService.runTaskOptionallyAsync(task, true);

        // then
        assertThat(result, equalTo(bukkitTask));
        verify(scheduler, only()).runTaskAsynchronously(plugin, task);
    }
}
