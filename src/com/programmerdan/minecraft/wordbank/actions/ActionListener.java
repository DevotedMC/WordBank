package com.programmerdan.minecraft.wordbank.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.programmerdan.minecraft.wordbank.WordBank;
import com.programmerdan.minecraft.wordbank.util.NameConstructor;

public class ActionListener implements Listener {
	
	public void TableTouch(PlayerInteractEvent event) {
		if (Action.RIGHT_CLICK_BLOCK != event.getAction()) return;
		if (event.getPlayer() == null) return;
		
		Block target = event.getClickedBlock();
		if (target == null || target.getType() != Material.ENCHANTMENT_TABLE) return;			
		
		ItemStack item = event.getItem();
		if (item == null || !item.hasItemMeta()) return; // no item or item has no custom data
		
		ItemMeta meta = item.getItemMeta();
		
		if (meta == null || !meta.hasDisplayName()) return; // no meta or no custom name
		
		if (meta.hasLore()) return; // we use a lore tag to indicate if a custom name has been applied
		
		String curName = meta.getDisplayName();
		
		if (curName.length() == WordBank.config().getActivationLength()) {
			// we've got a winrar!
			// Let's check if the player can pay his dues.
			Inventory pInv = event.getPlayer().getInventory();
			if (pInv.containsAtLeast(WordBank.config().getCost(), WordBank.config().getCost().getAmount())) {
				HashMap<Integer, ItemStack> incomplete = pInv.removeItem(WordBank.config().getCost());
				if (incomplete != null && !incomplete.isEmpty()) {
					// TODO: Note refund here.
					// too rich
					for (Map.Entry<Integer, ItemStack> cleanup : incomplete.entrySet()) {
						pInv.addItem(cleanup.getValue());// ignore overflow?
					}
				} else {
					meta.setDisplayName(NameConstructor.buildName(meta.getDisplayName(), true));
					ArrayList<String> lore = new ArrayList<String>();
					lore.add(WordBank.config().getMakersMark());
					meta.setLore(lore);
					item.setItemMeta(meta);
					// TODO: Tell player success
				}
			}
			// TODO: Tell player to gtfo
		}
		return;
	}
	
	public void ItemPrevention(PrepareAnvilEvent event) {
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
		// otherwise, carray on.
	}
}
