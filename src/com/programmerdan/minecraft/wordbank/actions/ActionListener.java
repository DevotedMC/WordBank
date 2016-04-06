package com.programmerdan.minecraft.wordbank.actions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
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
		if (meta.hasLore()) return;
		if (WordBank.config().isDebug()) WordBank.log().info("  - has no lore");
		
		String curName = meta.getDisplayName();
		
		if (curName.length() == WordBank.config().getActivationLength()) {
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
					if (WordBank.config().isDebug()) WordBank.log().info("  - Paid and updating item");
					meta.setDisplayName(NameConstructor.buildName(meta.getDisplayName(), true));
					ArrayList<String> lore = new ArrayList<String>();
					lore.add(WordBank.config().getMakersMark());
					meta.setLore(lore);
					item.setItemMeta(meta);
					event.getPlayer().sendMessage("Applied a new " + WordBank.config().getMakersMark());
					if (WordBank.config().hasDB()) {
						try {
							if (WordBank.config().isDebug()) WordBank.log().info("  - Inserting item record");
							Connection connection = WordBank.data().getConnection();
							PreparedStatement insert = connection.prepareStatement(WordBankData.insert);
							insert.setString(1, curName);
							insert.setString(2, event.getPlayer().getUniqueId().toString());
							insert.setString(3, item.getType().toString());
							insert.executeUpdate();
							insert.close();
							connection.close();
						} catch (SQLException se) {
							WordBank.log().log(Level.WARNING, "Failed to insert key utilization", se);
						}
					}
				}
			}
			event.getPlayer().sendMessage("Insufficient resources! Needs " + WordBank.config().getCost());
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
		
		// not a marked item?
		if (meta0 != null && meta0.hasLore() && !meta0.getLore().contains(WordBank.config().getMakersMark())) return;
		
		// check for rename
		ItemMeta resultMeta = result.getItemMeta();
		
		if (!resultMeta.hasDisplayName() || !resultMeta.getDisplayName().equals(meta0.getDisplayName())) {
			event.setResult(null);
			for (HumanEntity he : event.getViewers()) {
				if (he != null) {
					he.sendMessage("Cannot rename a " + WordBank.config().getMakersMark());
				}
			}
		}
	}
}
