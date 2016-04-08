package com.programmerdan.minecraft.wordbank;

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
}
