package com.programmerdan.minecraft.wordbank.actions;

import com.programmerdan.minecraft.wordbank.NameRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.programmerdan.minecraft.wordbank.WordBank;
import java.util.concurrent.ExecutionException;
import org.bukkit.Bukkit;

/**
 * Manages the detection and application of WordBank keys.
 * 
 * Prevents the renaming of items that have a new key.
 * 
 * @author ProgrammerDan
 */
public class ActionListener implements Listener {
	private WordBank plugin;
	
	private HashMap<UUID, BukkitTask> pendingMarks;
	
	public ActionListener() {
		this(null);
	}
	public ActionListener(WordBank plugin) {
		this.plugin = plugin;
		pendingMarks = new HashMap<>();
	}
	
	protected WordBank plugin() {
		return this.plugin == null ? WordBank.instance() : this.plugin; 
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void TableTouch(PlayerInteractEvent event) {
		if (plugin().config().isDebug()) plugin().logger().info("TableTouch event");
		if (Action.RIGHT_CLICK_BLOCK != event.getAction()) return;
		if (plugin().config().isDebug()) plugin().logger().info("  - is Rightclick");
		if (event.getPlayer() == null) return;
		if (plugin().config().isDebug()) plugin().logger().info("  - has player");
		
		Block target = event.getClickedBlock();
		if (target == null || target.getType() != Material.ENCHANTMENT_TABLE) return;
		if (plugin().config().isDebug()) plugin().logger().info("  - is touch Enchantment Table");
		
		// no item or item has no custom data
		ItemStack item = event.getItem();
		if (item == null || !item.hasItemMeta()) return;
		if (plugin().config().isDebug()) plugin().logger().info("  - has meta");
		
		// no meta or no custom name
		ItemMeta meta = item.getItemMeta();
		if (meta == null || !meta.hasDisplayName()) return;
		if (plugin().config().isDebug()) plugin().logger().info("  - has name");
		
		// we use a lore tag to indicate if a custom name has been applied
		if (meta.hasLore() && meta.getLore().contains(plugin().config().getMakersMark())) return;
		if (plugin().config().isDebug()) plugin().logger().info("  - has no Makers Mark lore");
		
		String curName = meta.getDisplayName();
		String unmodifiedName = curName;
		
		if (plugin().config().isActivateAnyLength() || curName.length() == plugin().config().getActivationLength()) {
			if (plugin().config().isDebug()) plugin().logger().info("  - is eligible");
			// we've got a winrar!
			// Let's check if the player can pay his dues.
			Inventory pInv = event.getPlayer().getInventory();
			if (pInv.containsAtLeast(plugin().config().getCost(), plugin().config().getCost().getAmount())) {
				final UUID puid = event.getPlayer().getUniqueId();
				BukkitTask firstHit = pendingMarks.get(puid);
				
				// moved this out of if/else so the proper length name is used in
				// both code blocks rather than just one (required by cache)
				if (curName.length() > plugin().config().getActivationLength()) {
					curName = curName.substring(0, plugin().config().getActivationLength());
				} else if (curName.length() < plugin().config().getActivationLength()) {
					int diff = plugin().config().getActivationLength() - curName.length();
					curName = curName.concat( new String(new char[diff])
							.replaceAll("\0", plugin().config().getPadding()));
				}
				
				if (firstHit == null) {
					long confirmDelay = plugin().config().getConfirmDelay();
				
					plugin().logger().log(Level.INFO, "Pending a mark for player {0}", puid);
					event.getPlayer().sendMessage(String.format("Hit the table a second time in the next %d seconds to confirm renaming using %s.",
							confirmDelay / 1000l, unmodifiedName));

					pendingMarks.put(puid, 
						new BukkitRunnable() {
							@Override
							public void run() {
								pendingMarks.remove(puid);
							}
						}.runTaskLater(plugin(), confirmDelay / 50l) // convert to ticks
					);
					
					// Schedule ASYNC task to load and cache the mapped name
					// Do it now so it'll be ready when the player clicks again
					{
						final String finalCurName = curName;
						Bukkit.getScheduler().runTaskAsynchronously(plugin(), () -> {
							try {
								System.out.println(plugin().nameCache().get(finalCurName).value);
							} catch (ExecutionException e) {

							}
						});
					}
					
					event.setCancelled(true);
					return;
				} else {
					HashMap<Integer, ItemStack> incomplete = pInv.removeItem(plugin().config().getCost());
					if (incomplete != null && !incomplete.isEmpty()) {
						if (plugin().config().isDebug()) plugin().logger().info("  - lacks enough to pay for it");
						
						for (Map.Entry<Integer, ItemStack> cleanup : incomplete.entrySet()) {
							pInv.addItem(cleanup.getValue());
							// ignore overflow?
						}
					} else {
						
						try {
							if (plugin().config().isDebug()) plugin().logger().info("  - Paid and updating item");
							
							NameRecord newNameRecord = plugin().nameCache().getIfPresent(curName);
							
							if (newNameRecord == null) {
								event.getPlayer().sendMessage(String.format("%sThat name is still generating (or there was an error), wait a few seconds.", ChatColor.WHITE));
								event.setCancelled(true);
								return;
							} else {
								// Move the task cancel here so spamming wordbank
								// doesn't spawn crap tons of threads all blocking
								// while waiting for the first cache load
								firstHit.cancel();
								pendingMarks.remove(puid);
							}
							
							if (plugin().config().isDebug()) plugin().logger().log(
									Level.INFO, "  - Using key {0} to generate {1}", 
									new Object[]{curName, newNameRecord.value});
	
							meta.setDisplayName(newNameRecord.value);
							ArrayList<String> lore = new ArrayList<String>();
							lore.add(plugin().config().getMakersMark());
							meta.setLore(lore);
							item.setItemMeta(meta);
							
							event.getPlayer().sendMessage(String.format("%sApplied a new %s of %s %sto the %s",
									ChatColor.WHITE, plugin().config().getMakersMark(), 
									meta.getDisplayName(), ChatColor.WHITE,
									item.getType().toString()));
							
							// Schedule ASYNC task to add an entry to the database
							// force=true for... logging purposes? to the database for some reason?
							// why does it need player UUID and item type just to keep a unique key/value?
							Bukkit.getScheduler().runTaskAsynchronously(plugin(), () -> {
								newNameRecord.mark(plugin(), event.getPlayer().getUniqueId().toString(), item.getType().toString(), true);
							});
						} catch (Exception e) {
							plugin().logger().log(Level.WARNING, "Something went very wrong while renaming", e);
							event.getPlayer().sendMessage(String.format("Mystic renaming of %s has %sfailed%s. %sPlease report via /helpop.",
									item.getType().toString(), ChatColor.ITALIC, ChatColor.RESET, ChatColor.AQUA));
							// no refund to prevent gaming of glitches
						}
					}
					event.setCancelled(true);
					return;
				}
			}
			event.setCancelled(true);
			event.getPlayer().sendMessage(String.format("%sYou need %d of %s to create a %s",
					ChatColor.RED, plugin().config().getCost().getAmount(),
					plugin().config().getCost().getType().toString(),
					plugin().config().getMakersMark()));
		}
		return;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void ItemPrevention(PrepareAnvilEvent event) {
		if (plugin().config().isDebug()) plugin().logger().info("ItemPrevention event");

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
		if (meta0 != null && meta0.hasLore() && !meta0.getLore().contains(plugin().config().getMakersMark())) return;

		if (plugin().config().isDebug()) plugin().logger().log(Level.INFO, "Repairing a {0}", plugin().config().getMakersMark());
		
		// check for rename
		ItemMeta resultMeta = result.getItemMeta();
		if (resultMeta == null) return; // something weird?
		
		if (plugin().config().isDebug()) plugin().logger().log(Level.INFO, "  - output Meta Display Name is {0}", resultMeta.getDisplayName());
		if (plugin().config().isDebug()) plugin().logger().log(Level.INFO, "  - marked Meta Display Name is {0}", meta0.getDisplayName());
		
		if (!resultMeta.hasDisplayName() || !resultMeta.getDisplayName().equals(meta0.getDisplayName())) {
			resultMeta.setDisplayName(meta0.getDisplayName());
			result.setItemMeta(resultMeta);
		}
	}
}
