package me.gnat008.perworldinventory.listeners;

import me.gnat008.perworldinventory.ClassCollector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests that listener classes are well formed.
 */
public class ListenerConsistencyTest {

    private static List<Class<? extends Listener>> listenerClasses;

    @BeforeClass
    public static void collectListeners() {
        listenerClasses = new ClassCollector("src/main/java", "me/gnat008/perworldinventory/listeners")
            .collectClasses(Listener.class);
        if (listenerClasses.isEmpty()) {
            throw new IllegalStateException("Failed collecting any listener classes");
        }
    }

    @Test
    public void shouldOnlyHaveEventHandlerMethods() {
        listenerClasses.forEach(clz -> checkHasOnlyEventHandlerMethods(clz));
    }

    private static void checkHasOnlyEventHandlerMethods(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!Modifier.isPrivate(method.getModifiers())) {
                assertThat("Method '" + method.getName() + "' in '" + clazz + "' should have @EventHandler",
                    method.isAnnotationPresent(EventHandler.class), equalTo(true));
            }
        }
    }
}
