package me.gnat008.perworldinventory.permission;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;

/**
 * Tests for {@link PlayerPermission}.
 */
public class PlayerPermissionTest {

    @Test
    public void shouldHaveUniqueNodes() {
        // given
        Set<String> nodes = new HashSet<>();

        // when/then
        for (PlayerPermission permission : PlayerPermission.values()) {
            if (nodes.contains(permission.getNode())) {
                fail("More than one enum value defines the node '" + permission.getNode() + "'");
            }
            nodes.add(permission.getNode());
        }
    }
}
