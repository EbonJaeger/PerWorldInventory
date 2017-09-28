package me.gnat008.perworldinventory.data.serializers;

import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link LocationSerializer}.
 */
public class LocationSerializerTest {

    @Test
    public void serializeLocationCorrectly() {
        // given
        World world = mock(World.class);
        given(world.getName()).willReturn("test-world");
        Location testLocation = new Location(world, 1.2, 3.4, 5.6, 7.8f, 9.0f);

        // when
        JsonObject result = LocationSerializer.serialize(testLocation);

        // then
        assertThat(result.get("world").getAsString(), equalTo("test-world"));
        assertThat(result.get("x").getAsDouble(), equalTo(1.2));
        assertThat(result.get("y").getAsDouble(), equalTo(3.4));
        assertThat(result.get("z").getAsDouble(), equalTo(5.6));
        assertThat(result.get("yaw").getAsFloat(), equalTo(7.8f));
        assertThat(result.get("pitch").getAsFloat(), equalTo(9.0f));
    }

    @Test
    @Ignore
    public void deserializeLocationCorrectly() {
        // given
        World world = mock(World.class);
        given(world.getName()).willReturn("test-world");

        JsonObject json = new JsonObject();
        json.addProperty("world", "test-world");
        json.addProperty("x", 1.2);
        json.addProperty("y", 3.4);
        json.addProperty("z", 5.6);
        json.addProperty("pitch", 9.0f);
        json.addProperty("yaw", 7.8f);

        // when
        Location result = LocationSerializer.deserialize(json);

        // then
        Location correct = new Location(world, 1.2, 3.4, 5.6, 7.8f, 9.0f);
        assertThat(result, equalTo(correct));
    }
}
