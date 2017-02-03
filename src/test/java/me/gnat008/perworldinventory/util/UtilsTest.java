package me.gnat008.perworldinventory.util;

import me.gnat008.perworldinventory.util.Utils;
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
    public void shouldReturnTrueSameMinorVersion() {
        // given/when
        boolean result = Utils.checkServerVersion(VERSION_1_9, 1, 9, 0);

        // then
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldReturnTrueSameMinorSamePatchVersion() {
        // given/when
        boolean result = Utils.checkServerVersion(VERSION_1_9_2, 1, 9, 2);

        // then
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldReturnTrueSameMinorHigherPatchVersion() {
        // given/when
        boolean result = Utils.checkServerVersion(VERSION_1_9_4, 1, 9, 2);

        // then
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldReturnTrueHigherMinorVersion() {
        // given/when
        boolean result = Utils.checkServerVersion(VERSION_1_10_2, 1, 9, 2);

        // then
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldReturnFalseLowerMinorVersion() {
        // given/when
        boolean result = Utils.checkServerVersion(VERSION_1_8_8, 1, 9, 2);

        // then
        assertThat(result, equalTo(false));
    }

    @Test
    public void shouldReturnFalseSameMinorLowerPatchVersion() {
        // given/when
        boolean result = Utils.checkServerVersion(VERSION_1_9_2, 1, 9, 4);

        // then
        assertThat(result, equalTo(false));
    }
}
