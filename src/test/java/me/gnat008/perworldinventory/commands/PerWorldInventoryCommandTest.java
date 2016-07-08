package me.gnat008.perworldinventory.commands;

import org.bukkit.command.CommandSender;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link PerWorldInventoryCommand}.
 */
public class PerWorldInventoryCommandTest {

    @Test
    public void shouldDisplayInformation() {
        // given
        ExecutableCommand command = new PerWorldInventoryCommand();
        CommandSender sender = mock(CommandSender.class);

        // when
        command.executeCommand(sender, Collections.<String>emptyList());

        // then
        ArgumentCaptor<String> messagesCaptor = ArgumentCaptor.forClass(String.class);
        verify(sender, times(2)).sendMessage(messagesCaptor.capture());
        assertThat(messagesCaptor.getAllValues().get(0), containsString("/pwi help"));
        assertThat(messagesCaptor.getAllValues().get(1), containsString("/pwi version"));
    }
}
