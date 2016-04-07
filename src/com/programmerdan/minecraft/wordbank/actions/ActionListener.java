package com.programmerdan.minecraft.wordbank.actions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.programmerdan.minecraft.wordbank.WordBank;
import com.programmerdan.minecraft.wordbank.data.WordBankData;
import com.programmerdan.minecraft.wordbank.util.NameConstructor;

/**
 * Manages the detection and application of WordBank keys.
 * 
 * Prevents the renaming of items that have a new key.
 * 
 * @author ProgrammerDan
 */
public class ActionListener implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void TableTouch(PlayerInteractEvent event) {
		if (WordBank.config().isDebug()) WordBank.log().info("TableTouch event");
		if (Action.RIGHT_CLICK_BLOCK != event.getAction()) return;
		if (WordBank.config().isDebug()) WordBank.log().info("  - is Rightclick");
		if (event.getPlayer() == null) return;
		if (WordBank.config().isDebug()) WordBank.log().info("  - has player");
		
		Block target = event.getClickedBlock();
		if (target == null || target.getType() != Material.ENCHANTMENT_TABLE) return;
		if (WordBank.config().isDebug()) WordBank.log().info("  - is touch Enchantment Table");
		
		// no item or item has no custom data
		ItemStack item = event.getItem();
		if (item == null || !item.hasItemMeta()) return;
		if (WordBank.config().isDebug()) WordBank.log().info("  - has meta");
		
		// no meta or no custom name
		ItemMeta meta = item.getItemMeta();
		if (meta == null || !meta.hasDisplayName()) return;
		if (WordBank.config().isDebug()) WordBank.log().info("  - has name");
		
		// we use a lore tag to indicate if a custom name has been applied
		if (meta.hasLore() && meta.getLore().contains(WordBank.config().getMakersMark())) return;
		if (WordBank.config().isDebug()) WordBank.log().info("  - has no Makers Mark lore");
		
		String curName = meta.getDisplayName();
		
		if (WordBank.config().isActivateAnyLength() || curName.length() == WordBank.config().getActivationLength()) {
			if (WordBank.config().isDebug()) WordBank.log().info("  - is eligible");
			// we've got a winrar!
			// Let's check if the player can pay his dues.
			Inventory pInv = event.getPlayer().getInventory();
			if (pInv.containsAtLeast(WordBank.config().getCost(), WordBank.config().getCost().getAmount())) {
				HashMap<Integer, ItemStack> incomplete = pInv.removeItem(WordBank.config().getCost());
				if (incomplete != null && !incomplete.isEmpty()) {
					if (WordBank.config().isDebug()) WordBank.log().info("  - lacks enough to pay for it");
					
					for (Map.Entry<Integer, ItemStack> cleanup : incomplete.entrySet()) {
						pInv.addItem(cleanup.getValue());
						// ignore overflow?
					}
				} else {
					try {
						if (WordBank.config().isDebug()) WordBank.log().info("  - Paid and updating item");
						
						if (curName.length() > WordBank.config().getActivationLength()) {
							curName = curName.substring(0, WordBank.config().getActivationLength());
						} else if (curName.length() < WordBank.config().getActivationLength()) {
							int diff = WordBank.config().getActivationLength() - curName.length();
							curName = curName.concat( new String(new char[diff])
									.replaceAll("\0", WordBank.config().getPadding()));
						}
						String newName = NameConstructor.buildName(curName, true);
						
						if (WordBank.config().isDebug()) WordBank.log().log(
								Level.INFO, "  - Using key {0} to generate {1}", 
								new Object[]{curName, newName});

						meta.setDisplayName(newName);
						ArrayList<String> lore = new ArrayList<String>();
						lore.add(WordBank.config().getMakersMark());
						meta.setLore(lore);
						item.setItemMeta(meta);
						
						event.getPlayer().sendMessage(String.format("%sApplied a new %s of %s %sto the %s",
								ChatColor.WHITE, WordBank.config().getMakersMark(), 
								meta.getDisplayName(), ChatColor.WHITE,
								item.getType().toString()));
						
						if (WordBank.config().hasDB()) {
							try {
								if (WordBank.config().isDebug()) WordBank.log().info("  - Inserting item record");
								Connection connection = WordBank.data().getConnection();
								PreparedStatement insert = connection.prepareStatement(WordBankData.insert);
								insert.setString(1, curName);
								insert.setString(2, event.getPlayer().getUniqueId().toString());
								insert.setString(3, item.getType().toString());
								insert.setString(4, newName);
								insert.executeUpdate();
								insert.close();
								connection.close();
							} catch (SQLException se) {
								WordBank.log().log(Level.WARNING, "Failed to insert key utilization", se);
							}
						}
					} catch (Exception e) {
						WordBank.log().log(Level.WARNING, "Something went very wrong while renaming", e);
						event.getPlayer().sendMessage(String.format("Mystic renaming of %s has %sfailed%s. %sPlease report via /helpop.",
								item.getType().toString(), ChatColor.ITALIC, ChatColor.RESET, ChatColor.AQUA));
						// no refund to prevent gaming of glitches
					}
					return;
				}
			}
			event.getPlayer().sendMessage(String.format("%sYou need %d of %s to create a %s",
					ChatColor.RED, WordBank.config().getCost().getAmount(),
					WordBank.config().getCost().getType().toString(),
					WordBank.config().getMakersMark()));
		}
		return;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void ItemPrevention(PrepareAnvilEvent event) {
		if (WordBank.config().isDebug()) WordBank.log().info("ItemPrevention event");

		if (event.getInventory() == null) return;
		
		AnvilInventory anvil = event.getInventory();
		
		ItemStack result = event.getResult();
		if (result == null) return;
		
		ItemStack slot0 = anvil.getItem(0);
		
		if (slot0 == null) return; // no item?
		if (slot0 != null && !slot0.hasItemMeta()) return; // neither has meta
		
		ItemMeta meta0 = slot0.getItemMeta();
		
		if (meta0 == null) return; // huh?!
		if (meta0 != null && !meta0.hasDisplayName()) return; // neither has name
		
		if (meta0 != null && !meta0.hasLore()) return; // neither has lore
		
		// not a marked item?
		if (meta0 != null && meta0.hasLore() && !meta0.getLore().contains(WordBank.config().getMakersMark())) return;

		if (WordBank.config().isDebug()) WordBank.log().log(Level.INFO, "Repairing a {0}", WordBank.config().getMakersMark());
		
		// check for rename
		ItemMeta resultMeta = result.getItemMeta();
		if (resultMeta == null) return; // something weird?
		
		if (WordBank.config().isDebug()) WordBank.log().log(Level.INFO, "  - output Meta Display Name is {0}", resultMeta.getDisplayName());
		if (WordBank.config().isDebug()) WordBank.log().log(Level.INFO, "  - marked Meta Display Name is {0}", meta0.getDisplayName());
		
		if (!resultMeta.hasDisplayName() || !resultMeta.getDisplayName().equals(meta0.getDisplayName())) {
			resultMeta.setDisplayName(meta0.getDisplayName());
			result.setItemMeta(resultMeta);
		}
	}
}
