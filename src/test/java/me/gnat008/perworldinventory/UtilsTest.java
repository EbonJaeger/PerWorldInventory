package me.gnat008.perworldinventory;

import org.bukkit.Bukkit;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Utils}.
 */
public class UtilsTest {

    /**
     * Version Strings that would normally be returned by {@link Bukkit#getVersion()}.
     */
    private final String VERSION_1_8_8 = "git-Spigot-8a048fe-3c19fef (MC: 1.8.8)";
    private final String VERSION_1_9 = "git-Spigot-8a048fe-3c19fef (MC: 1.9)";
    private final String VERSION_1_9_2 = "git-Spigot-8a048fe-3c19fef (MC: 1.9.2)";
    private final String VERSION_1_9_4 = "git-Spigot-8a048fe-3c19fef (MC: 1.9.4)";
    private final String VERSION_1_10 = "git-Spigot-8a048fe-3c19fef (MC: 1.10)";
    private final String VERSION_1_10_2 = "git-Spigot-8a048fe-3c19fef (MC: 1.10.2)";

    @Test
    public void shouldReturnFalseFor1_8_8() {
        // given/when
        boolean result = Utils.checkServerVersion(VERSION_1_8_8);

        // then
        assertThat(result, equalTo(false));
    }

    @Test
    public void shouldReturnFalseFor1_9() {
        // given/when
        boolean result = Utils.checkServerVersion(VERSION_1_9);

        // then
        assertThat(result, equalTo(false));
    }

    @Test
    public void shouldReturnTrueFor1_9_2() {
        // given/when
        boolean result = Utils.checkServerVersion(VERSION_1_9_2);

        // then
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldReturnTrueFor1_9_4() {
        // given/when
        boolean result = Utils.checkServerVersion(VERSION_1_9_4);

        // then
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldReturnTrueFor1_10() {
        // given/when
        boolean result = Utils.checkServerVersion(VERSION_1_10);

        // then
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldReturnTrueFor1_10_2() {
        // given/when
        boolean result = Utils.checkServerVersion(VERSION_1_10_2);

        // then
        assertThat(result, equalTo(true));
    }
}
