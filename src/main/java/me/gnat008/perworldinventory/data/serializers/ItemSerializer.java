/*
 * Copyright (C) 2014-2016  EbonJaguar
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.gnat008.perworldinventory.data.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.gnat008.perworldinventory.PerWorldInventory;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ItemSerializer {

    @Inject
    private static PerWorldInventory plugin;

    ItemSerializer() {}

    public static JsonObject serializeInventoryItem(ItemStack item, int index) {
        return serializeItem(item, true, index);
    }

    public static JsonObject serializeItem(ItemStack item, boolean useIndex, int index) {
        JsonObject values = new JsonObject();
        if (item == null)
            return null;

        /*
         * Check to see if the item is a skull with a null owner.
         * This is because some people are getting skulls with null owners, which causes Spigot to throw an error
         * when it tries to serialize the item. If this ever gets fixed in Spigot, this will be removed.
         */
        if (item.getType() == Material.SKULL_ITEM) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta.hasOwner() && (meta.getOwner() == null || meta.getOwner().isEmpty())) {
                item.setItemMeta(plugin.getServer().getItemFactory().getItemMeta(Material.SKULL_ITEM));
            }
        }

        if (useIndex)
            values.addProperty("index", index);

        ByteArrayOutputStream outputStream;
        BukkitObjectOutputStream dataObject;
        try {
            outputStream = new ByteArrayOutputStream();
            dataObject = new BukkitObjectOutputStream(outputStream);
            dataObject.writeObject(item);
            dataObject.close();

            values.addProperty("item", Base64Coder.encodeLines(outputStream.toByteArray()));
        } catch (IOException ex) {
            plugin.getLogger().severe("Error saving an item:");
            plugin.getLogger().severe("Item: " + item.getType().toString());
            plugin.getLogger().severe("Reason: " + ex.getMessage());
            return null;
        }

        return values;
    }

    public static ItemStack deserializeItem(JsonObject data) {
        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data.get("item").getAsString()));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            return (ItemStack) dataInput.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            plugin.getLogger().severe("Error loading an item:" + ex.getMessage());
            return new ItemStack(Material.AIR);
        }
    }

    /**
     * Get an ItemStack from a JsonObject.
     *
     * @param item The data for the item
     * @return The ItemStack
     * @deprecated Kept for compatibility reasons. Use ItemSerializer#deserializeItem(JsonObject data) whenever possible
     */
    @Deprecated
    public static ItemStack getItem(JsonObject item) {
        int id = item.get("id").getAsInt();
        int amount = item.get("amount").getAsInt();
        short data = item.get("data").getAsShort();
        int repairPenalty = 0;

        String name = null;
        Map<Enchantment, Integer> enchants = null;
        ArrayList<String> flags = null;
        ArrayList<String> lore = null;

        if (item.has("name"))
            name = item.get("name").getAsString();
        if (item.has("enchantments"))
            enchants = DeprecatedMethodUtil.getEnchantments(item.get("enchantments").getAsString());
        if (item.has("flags")) {
            JsonArray f = item.getAsJsonArray("flags");
            flags = new ArrayList<>();
            for (int i = 0; i < f.size() - 1; i++)
                flags.add(f.get(i).getAsString());
        }
        if (item.has("lore")) {
            JsonArray l = item.getAsJsonArray("flags");
            lore = new ArrayList<>();
            for (int i = 0; i < l.size() - 1; i++)
                lore.add(l.get(i).getAsString());
        }
        if (item.has("repairPenalty"))
            repairPenalty = item.get("repairPenalty").getAsInt();

        Material mat = Material.getMaterial(id);
        ItemStack is = new ItemStack(mat, amount, data);

        if (mat == Material.BANNER) {
            BannerMeta meta = DeprecatedMethodUtil.getBannerMeta(item.getAsJsonObject("banner-meta"));
            is.setItemMeta(meta);
        } else if ((mat == Material.BOOK_AND_QUILL || mat == Material.WRITTEN_BOOK) && item.has("book-meta")) {
            BookMeta meta = DeprecatedMethodUtil.getBookMeta(item.getAsJsonObject("book-meta"));
            is.setItemMeta(meta);
        } else if (mat == Material.ENCHANTED_BOOK && item.has("book-meta")) {
            EnchantmentStorageMeta meta = DeprecatedMethodUtil.getEnchantedBookMeta(item.getAsJsonObject("book-meta"));
            is.setItemMeta(meta);
        } else if (DeprecatedMethodUtil.isLeatherArmor(mat) && item.has("armor-meta")) {
            LeatherArmorMeta meta = DeprecatedMethodUtil.getLeatherArmorMeta(item.getAsJsonObject("armor-meta"));
            is.setItemMeta(meta);
        } else if (mat == Material.SKULL_ITEM && item.has("skull-meta")) {
            SkullMeta meta = DeprecatedMethodUtil.getSkullMeta(item.getAsJsonObject("skull-meta"));
            is.setItemMeta(meta);
        } else if (mat == Material.FIREWORK && item.has("firework-meta")) {
            FireworkMeta meta = DeprecatedMethodUtil.getFireworkMeta(item.getAsJsonObject("firework-meta"));
            is.setItemMeta(meta);
        }

        ItemMeta meta = is.getItemMeta();
        if (name != null)
            meta.setDisplayName(name);
        if (flags != null) {
            for (String flag : flags) {
                meta.addItemFlags(ItemFlag.valueOf(flag));
            }
        }
        if (lore != null)
            meta.setLore(lore);
        is.setItemMeta(meta);
        if (repairPenalty != 0) {
            Repairable rep = (Repairable) meta;
            rep.setRepairCost(repairPenalty);
            is.setItemMeta((ItemMeta) rep);
        }

        if (enchants != null)
            is.addUnsafeEnchantments(enchants);
        return is;
    }
}
