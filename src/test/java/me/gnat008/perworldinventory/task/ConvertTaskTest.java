package me.gnat008.perworldinventory.task;

import me.gnat008.perworldinventory.ReflectionTestUtils;
import me.gnat008.perworldinventory.service.ConvertService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

/**
 * Tests for {@link ConvertTask}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConvertTaskTest {

    @Mock
    private ConvertService convertService;

    @Captor
    private ArgumentCaptor<Collection<OfflinePlayer>> playerCaptor;

    @Test
    public void shouldRunTask() {
        // given
        OfflinePlayer[] players = asArray(
                mockOfflinePlayer("Bob"), mockOfflinePlayer("Bobby"), mockOfflinePlayer("LilBob"),
                mockOfflinePlayer("Bobbers"), mockOfflinePlayer("Nicole"), mockOfflinePlayer("RanOutaNames")
                );
        Set<OfflinePlayer> setPlayers = new HashSet<>();
        setPlayers.addAll(Arrays.asList(players));
        reset(convertService);
        ConvertTask task = new ConvertTask(convertService, null, players, setPlayers);

        // when (1 - first run, 5 players per run)
        task.run();

        // then (1)
        // In the first run, Bob through Nicole are converted
        assertRanConvertWithPlayers(players[0], players[1], players[2], players[3], players[4]);

        // when (2)
        reset(convertService);
        task.run();

        // then (2)
        // RanOutaNames is converted
        assertRanConvertWithPlayers(players[5]);
    }

    @Test
    public void shouldStopAndInformSenderOnComplete() {
        // given
        Player sender = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        given(sender.getUniqueId()).willReturn(uuid);

        Set<OfflinePlayer> set = new HashSet<>();

        ConvertTask task = new ConvertTask(convertService, sender, new OfflinePlayer[0], set);

        ReflectionTestUtils.setField(BukkitRunnable.class, task, "taskId", 29457);
        Server server = mock(Server.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        given(server.getScheduler()).willReturn(scheduler);
        ReflectionTestUtils.setField(Bukkit.class, null, "server", server);
        given(server.getPlayer(uuid)).willReturn(sender);
        given(sender.isOnline()).willReturn(true);

        // when
        task.run();

        // then
        verify(scheduler).cancelTask(task.getTaskId());
        verify(sender).sendMessage(argThat(containsString("Conversion has been completed!")));
    }

    @Test
    public void shouldStopAndInformConsoleOnComplete() {
        // given
        Set<OfflinePlayer> set = new HashSet<>();

        ConvertTask task = new ConvertTask(convertService, null, new OfflinePlayer[0], set);

        ReflectionTestUtils.setField(BukkitRunnable.class, task, "taskId", 29457);
        Server server = mock(Server.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        given(server.getScheduler()).willReturn(scheduler);
        ReflectionTestUtils.setField(Bukkit.class, null, "server", server);
        ConsoleCommandSender commandSender = mock(ConsoleCommandSender.class);
        given(server.getConsoleSender()).willReturn(commandSender);

        // when
        task.run();

        // then
        verify(scheduler).cancelTask(task.getTaskId());
        verify(commandSender).sendMessage(argThat(containsString("Conversion has been completed!")));
    }

    private OfflinePlayer mockOfflinePlayer(String name) {
        OfflinePlayer player = mock(OfflinePlayer.class);
        given(player.getName()).willReturn(name);

        return player;
    }

    private OfflinePlayer[] asArray(OfflinePlayer... players) {
        return players;
    }

    private void assertRanConvertWithPlayers(OfflinePlayer... players) {
        verify(convertService).executeConvert(playerCaptor.capture());
        assertThat(playerCaptor.getValue(), containsInAnyOrder(players));
    }
}
