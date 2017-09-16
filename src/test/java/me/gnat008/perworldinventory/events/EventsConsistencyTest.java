package me.gnat008.perworldinventory.events;

import me.gnat008.perworldinventory.ClassCollector;
import org.bukkit.event.Event;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests that all events have the required methods.
 */
public class EventsConsistencyTest {

    private static List<Class<? extends Event>> eventClasses;

    @BeforeClass
    public static void collectListeners() {
        eventClasses = new ClassCollector("src/main/java", "me/gnat008/perworldinventory/events")
                .collectClasses(Event.class);
        if (eventClasses.isEmpty()) {
            throw new IllegalStateException("Failed collecting any event classes");
        }
    }

    @Test
    public void shouldHaveHandlerMethod() {
        eventClasses.forEach(clz -> checkHasHandlerMethod(clz));
    }

    private static void checkHasHandlerMethod(Class<?> clazz) {
        boolean found = false;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals("getHandlers")) {
                assertThat("Method '" + method.getName() + "' in class '" + clazz.getName() + "' should be public",
                        Modifier.isPublic(method.getModifiers()), equalTo(true));
                found = true;
            }
        }

        assertThat("Class '" + clazz.getName() + "' should have public method getHandlers()",
                found, equalTo(true));
    }

    @Test
    public void shouldHaveHandlerListMethod() {
        eventClasses.forEach(clz -> checkHasHandlerListMethod(clz));
    }

    private static void checkHasHandlerListMethod(Class<?> clazz) {
        boolean found = false;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals("getHandlerList")) {
                assertThat("Method '" + method.getName() + "' in class '" + clazz.getName() + "' should be public",
                        Modifier.isPublic(method.getModifiers()), equalTo(true));
                assertThat("Method '" + method.getName() + "' in class '"+ clazz.getName() + "' should be static",
                        Modifier.isStatic(method.getModifiers()), equalTo(true));
                found = true;
            }
        }

        assertThat("Class '" + clazz.getName() + "' should have public static method getHandlerList()",
                found, equalTo(true));
    }
}
