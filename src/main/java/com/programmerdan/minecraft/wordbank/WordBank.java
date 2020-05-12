package com.programmerdan.minecraft.wordbank;

import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;

import com.programmerdan.minecraft.wordbank.actions.ActionListener;
import com.programmerdan.minecraft.wordbank.actions.CommandListener;
import com.programmerdan.minecraft.wordbank.data.WordBankData;
import com.programmerdan.minecraft.wordbank.util.NameConstructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;
import org.bukkit.ChatColor;

/**
 * See README.md for details. Simple Bukkit plugin using some Cool Stuff under the hood.
 *
 * Designed on 1.9 but should be backwards compatible with 1.8.
 * 
 * @author ProgrammerDan
 * @since April 8 2016
 */
public class WordBank extends JavaPlugin {
	private static WordBank plugin;
	private WordBankConfig config;
	private WordBankData data;
	private LoadingCache<String, NameRecord> nameCache; // laggy database hits? that's not very cache money tbh

	public void onEnable() {
		WordBank.plugin = this;
		WordBank.instance().saveDefaultConfig();
		WordBank.instance().reloadConfig();
		FileConfiguration conf = WordBank.instance().getConfig();
		try {
			this.config = new WordBankConfig(conf);
		} catch(InvalidPluginException e) {
			getLogger().log(Level.SEVERE, "Failed to load config. Disabling plugin.", e);
			this.setEnabled(false);
		}
		
		// Do DB provision
		data = new WordBankData();
		nameCache = CacheBuilder.newBuilder()
				.expireAfterAccess(config.getNamecacheInvalidateMinutes(), TimeUnit.MINUTES)
				.expireAfterWrite(config.getNamecacheInvalidateMinutes(), TimeUnit.MINUTES)
				.maximumSize(config.getNamecacheMaxSize())
				.build(new CacheLoader<String, NameRecord>() {
					@Override
					public NameRecord load(String key) throws Exception {
						String value = null;
						long startTime = 0, endTime = 0;
						if (config().hasDB()) {
							try {
								startTime = System.nanoTime();
								try (Connection connection = data().getConnection();
										PreparedStatement statement = connection.prepareStatement(WordBankData.getvalue)) {
									statement.setString(1, key);
									try (ResultSet rs = statement.executeQuery()) {
										if (rs.next()) {
											value = rs.getString("val");
										}
									}
									statement.close();
								}
								endTime = System.nanoTime();
							} catch (SQLException se) {
								logger().log(Level.WARNING, "Failed to retrieve key/value data!", se);
								if (config().isFailRenameOnDbError()) {
									throw se;
								}
							}
							if (value != null) {
								// value exists in database, return record with marked=TRUE
								NameRecord record = new NameRecord(key, value, true);
								if (config().isDebug()) logger().log(Level.INFO, "Retrieved value {0}={1} from database in {2}ms", new Object[] { record.key, record.value, (endTime - startTime) / 1_000_000.0F });
								return record;
							} else {
								// value did not exist in database. debug time and carry on
								if (config().isDebug()) logger().log(Level.INFO, "Database entry not found for {0} in {1}ms", new Object[] { key, (endTime - startTime) / 1_000_000.0F });
							}
						}
						// no value in database, return record with marked=FALSE
						startTime = System.nanoTime();
						NameRecord record = NameConstructor.buildName(key);
						endTime = System.nanoTime();
						if (config().isDebug()) logger().log(Level.INFO, "Generated name for value {0}={1} in {2}ms", new Object[] { record.key, record.value, (endTime - startTime) / 1_000_000.0F });
						return record;
					}
				}
		);
		Bukkit.getPluginManager().registerEvents(new ActionListener(), this);
		plugin.getCommand("wordbank").setExecutor(new CommandListener());
	}

	public void onDisable() {
		try {
			this.data.close();
		} catch (SQLException e) {
			getLogger().log(Level.WARNING, "Failed to close data source", e);
		}
		this.data = null;
		this.config = null;
		WordBank.plugin = null;
	}

	public static WordBank instance() {
		return plugin;
	}
	public Logger logger() {
		return getLogger();
	}
	public void log(Level level, String message, Object...objects){
		getLogger().log(level, message, objects);;
	}
	public WordBankConfig config() {
		return config;
	}
	public WordBankData data() {
		return data;
	}
	
	public LoadingCache<String, NameRecord> nameCache() {
		return nameCache;
	}
}
