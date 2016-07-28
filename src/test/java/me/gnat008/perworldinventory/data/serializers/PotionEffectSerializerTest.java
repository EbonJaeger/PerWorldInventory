package me.gnat008.perworldinventory.data.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Color;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link PotionEffectSerializer}.
 */
@Ignore
public class PotionEffectSerializerTest {

    @Test
    public void serializePotionEffectWithParticlesAndColor() {
        // given
        ArrayList<PotionEffect> effects = new ArrayList<>();
        PotionEffect effect = new PotionEffect(PotionEffectType.CONFUSION, 30, 1, true, true, Color.AQUA);
        effects.add(effect);

        // when
        JsonArray serialized = PotionEffectSerializer.serialize(effects);

        // then
        JsonObject json = serialized.get(0).getAsJsonObject();
        assertTrue(json.get("type").getAsString().equals("CONFUSION"));
        assertTrue(json.get("amp").getAsInt() == 1);
        assertTrue(json.get("duration").getAsInt() == 30);
        assertTrue(json.get("ambient").getAsBoolean());
        assertTrue(json.get("particles").getAsBoolean());
        assertTrue(Color.fromRGB(json.get("color").getAsInt()) == Color.AQUA);
    }
}
