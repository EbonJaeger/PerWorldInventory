package com.kill3rtaco.tacoserialization;

import java.util.*;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to help with the serialization of ItemStacks.
 * @author KILL3RTACO
 * @since 1.0
 *
 */
public class SingleItemSerialization {
	
	protected SingleItemSerialization() {
	}
	
	/**
	 * Serialize an ItemStack that is inside an inventory. An extra "index" key will be added to store the
	 * position of the ItemStack in the inventory
	 * @param items The items to serialize
	 * @param index The position of the ItemStack inside the inventory
	 * @return The serialized items
	 */
	public static JSONObject serializeItemInInventory(ItemStack items, int index) {
		return serializeItems(items, true, index);
	}
	
	/**
	 * Serialize an ItemStack
	 * @param items The items to serialize
	 * @return The serialized items
	 */
	public static JSONObject serializeItem(ItemStack items) {
		return serializeItems(items, false, 0);
	}
	
	private static JSONObject serializeItems(ItemStack items, boolean useIndex, int index) {
		try {
			JSONObject values = new JSONObject();
			if(items == null)
				return null;
			int id = items.getTypeId();
			int amount = items.getAmount();
			int data = items.getDurability();
			boolean hasMeta = items.hasItemMeta();
			String name = null, enchants = null;
            String[] flags = null;
			String[] lore = null;
			int repairPenalty = 0;
			Material mat = items.getType();
			JSONObject bannerMeta = null, bookMeta = null, armorMeta = null, skullMeta = null, fwMeta = null;
			if (mat == Material.BANNER) {
				bannerMeta = BannerSerialization.serializeBanner((BannerMeta) items.getItemMeta());
			} else if(mat == Material.BOOK_AND_QUILL || mat == Material.WRITTEN_BOOK) {
				bookMeta = BookSerialization.serializeBookMeta((BookMeta) items.getItemMeta());
			} else if(mat == Material.ENCHANTED_BOOK) {
				bookMeta = BookSerialization.serializeEnchantedBookMeta((EnchantmentStorageMeta) items.getItemMeta());
			} else if(Util.isLeatherArmor(mat)) {
				armorMeta = LeatherArmorSerialization.serializeArmor((LeatherArmorMeta) items.getItemMeta());
			} else if(mat == Material.SKULL_ITEM) {
				skullMeta = SkullSerialization.serializeSkull((SkullMeta) items.getItemMeta());
			} else if(mat == Material.FIREWORK) {
				fwMeta = FireworkSerialization.serializeFireworkMeta((FireworkMeta) items.getItemMeta());
			}
			if(hasMeta) {
				ItemMeta meta = items.getItemMeta();
				if(meta.hasDisplayName())
					name = meta.getDisplayName();
				if(meta.hasLore()) {
					lore = meta.getLore().toArray(new String[]{});
				}
				if(meta.hasEnchants())
					enchants = EnchantmentSerialization.serializeEnchantments(meta.getEnchants());
				if(meta instanceof Repairable){
					Repairable rep = (Repairable) meta;
					if(rep.hasRepairCost()){
						repairPenalty = rep.getRepairCost();
					}
				}

                if (meta.getItemFlags() != null && !meta.getItemFlags().isEmpty()) {
                    List<String> flagsList = new ArrayList<>();
                    for (ItemFlag flag : meta.getItemFlags()) {
                        flagsList.add(flag.toString());
                    }
                    flags = flagsList.toArray(new String[flagsList.size()]);
                }

			}

			values.put("id", id);
			values.put("amount", amount);
			values.put("data", data);
			if(useIndex)
				values.put("index", index);
			if(name != null)
				values.put("name", name);
			if(enchants != null)
				values.put("enchantments", enchants);
            if (flags != null)
                values.put("flags", flags);
			if(lore != null)
				values.put("lore", lore);
			if(repairPenalty != 0)
				values.put("repairPenalty", repairPenalty);
			if (bannerMeta != null && bannerMeta.length() > 0)
				values.put("banner-meta", bannerMeta);
			if(bookMeta != null && bookMeta.length() > 0)
				values.put("book-meta", bookMeta);
			if(armorMeta != null && armorMeta.length() > 0)
				values.put("armor-meta", armorMeta);
			if(skullMeta != null && skullMeta.length() > 0)
				values.put("skull-meta", skullMeta);
			if(fwMeta != null && fwMeta.length() > 0)
				values.put("firework-meta", fwMeta);
			return values;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Deserialize an ItemStack
	 * @param item The ItemStack to deserialize
	 * @return The Deserialized ItemStack
	 */
	public static ItemStack getItem(String item) {
		return getItem(item, 0);
	}
	
	/**
	 * Deserialize an ItemStack. An index if given strictly for debug purposes. When an error message is given
	 * the index will be used for more useful reference
	 * @param item The ItemStack to deserialize
	 * @param index The index of the ItemStack in an inventory or ItemStack array
	 * @return The deserialized ItemStack
	 */
	public static ItemStack getItem(String item, int index) {
		try {
			return getItem(new JSONObject(item), index);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Deserialize an ItemStack
	 * @param item The ItemStack to deserialize
	 * @return The Deserialized ItemStack
	 */
	public static ItemStack getItem(JSONObject item) {
		return getItem(item, 0);
	}
	
	/**
	 * Deserialize an ItemStack. An index if given strictly for debug purposes. When an error message is given
	 * the index will be used for more useful reference
	 * @param item The ItemStack to deserialize
	 * @param index The index of the ItemStack in an inventory or ItemStack array
	 * @return The deserialized ItemStack
	 */
public static ItemStack getItem(JSONObject item, int index) {
		try {
			int id = item.getInt("id");
			int amount = item.getInt("amount");
			int data = item.getInt("data");
			String name = null;
			Map<Enchantment, Integer> enchants = null;
            ArrayList<String> flags = null;
			ArrayList<String> lore = null;
			int repairPenalty = 0;
			if(item.has("name"))
				name = item.getString("name");
			if(item.has("enchantments"))
				enchants = EnchantmentSerialization.getEnchantments(item.getString("enchantments"));
            if (item.has("flags")) {
                JSONArray f = item.getJSONArray("flags");
                flags = new ArrayList<>();
                for (int i = 0; i < f.length(); i++) {
                    flags.add(f.getString(i));
                }
            }
			if(item.has("lore")) {
				JSONArray l = item.getJSONArray("lore");
				lore = new ArrayList<String>();
				for(int j = 0; j < l.length(); j++) {
					lore.add(l.getString(j));
				}
			}
			if(item.has("repairPenalty"))
				repairPenalty = item.getInt("repairPenalty");				


			if(Material.getMaterial(id) == null)
				throw new IllegalArgumentException("Item " + index + " - No Material found with id of " + id);
			Material mat = Material.getMaterial(id);
			ItemStack stuff = new ItemStack(mat, amount, (short) data);
			if (mat == Material.BANNER) {
				BannerMeta meta = BannerSerialization.getBannerMeta(item.getJSONObject("banner-meta"));
				stuff.setItemMeta(meta);
			} else if((mat == Material.BOOK_AND_QUILL || mat == Material.WRITTEN_BOOK) && item.has("book-meta")) {
				BookMeta meta = BookSerialization.getBookMeta(item.getJSONObject("book-meta"));
				stuff.setItemMeta(meta);
			} else if(mat == Material.ENCHANTED_BOOK && item.has("book-meta")) {
				EnchantmentStorageMeta meta = BookSerialization.getEnchantedBookMeta(item.getJSONObject("book-meta"));
				stuff.setItemMeta(meta);
			} else if(Util.isLeatherArmor(mat) && item.has("armor-meta")) {
				LeatherArmorMeta meta = LeatherArmorSerialization.getLeatherArmorMeta(item.getJSONObject("armor-meta"));
				stuff.setItemMeta(meta);
			} else if(mat == Material.SKULL_ITEM && item.has("skull-meta")) {
				SkullMeta meta = SkullSerialization.getSkullMeta(item.getJSONObject("skull-meta"));
				stuff.setItemMeta(meta);
			} else if(mat == Material.FIREWORK && item.has("firework-meta")) {
				FireworkMeta meta = FireworkSerialization.getFireworkMeta(item.getJSONObject("firework-meta"));
				stuff.setItemMeta(meta);
			}
			ItemMeta meta = stuff.getItemMeta();
			if(name != null)
				meta.setDisplayName(name);
            if (flags != null) {
                for (String flag : flags) {
                    meta.addItemFlags(ItemFlag.valueOf(flag));
                }
            }
			if(lore != null)
				meta.setLore(lore);
			stuff.setItemMeta(meta);
			if(repairPenalty != 0){
				Repairable rep = (Repairable) meta;
				rep.setRepairCost(repairPenalty);
				stuff.setItemMeta((ItemMeta) rep);
			}

			if(enchants != null)
				stuff.addUnsafeEnchantments(enchants);
			return stuff;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serialize an ItemStack from an inventory as a string 
	 * @param items The ItemStack to serialize
	 * @param index The position of the ItemStack
	 * @return The serialization string
	 */
	public static String serializeItemInInventoryAsString(ItemStack items, int index) {
		return serializeItemInInventoryAsString(items, index, false);
	}
	
	/**
	 * Serialize an ItemStack from an inventory as a string 
	 * @param items The ItemStack to serialize
	 * @param index The position of the ItemStack
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @return The serialization string
	 */
	public static String serializeItemInInventoryAsString(ItemStack items, int index, boolean pretty) {
		return serializeItemInInventoryAsString(items, index, pretty, 5);
	}
	
	/**
	 * Serialize an ItemStack from an inventory as a string 
	 * @param items The ItemStack to serialize
	 * @param index The position of the ItemStack
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @param indentFactor The amount of spaces in a tab
	 * @return The serialization string
	 */
	public static String serializeItemInInventoryAsString(ItemStack items, int index, boolean pretty, int indentFactor) {
		try {
			if(pretty) {
				return serializeItemInInventory(items, index).toString(indentFactor);
			} else {
				return serializeItemInInventory(items, index).toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serialize an ItemStack as a string 
	 * @param items The ItemStack to serialize
	 * @return The serialization string
	 */
	public static String serializeItemAsString(ItemStack items) {
		return serializeItemAsString(items, false);
	}
	
	/**
	 * Serialize an ItemStack as a string 
	 * @param items The ItemStack to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @return The serialization string
	 */
	public static String serializeItemAsString(ItemStack items, boolean pretty) {
		return serializeItemAsString(items, pretty, 5);
	}
	
	/**
	 * Serialize an ItemStack as a string 
	 * @param items The ItemStack to serialize
	 * @param pretty Whether the resulting string should be 'pretty' or not
	 * @param indentFactor The amount of spaces in a tab
	 * @return The serialization string
	 */
	public static String serializeItemAsString(ItemStack items, boolean pretty, int indentFactor) {
		try {
			if(pretty) {
				return serializeItem(items).toString(indentFactor);
			} else {
				return serializeItem(items).toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
