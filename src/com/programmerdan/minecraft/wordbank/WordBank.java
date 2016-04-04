package com.programmerdan.minecraft.wordbank;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;

public class WordBank extends JavaPlugin {
	private static WordBank plugin;
	private WordBankConfig config;

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
	}

	public void onDisable() {
		WordBank.plugin = null;
	}

	public static WordBank instance() {
		return plugin;
	}
	public static Logger log() {
		return WordBank.plugin.getLogger();
	}
	public static WordBankConfig config() {
		return WordBank.instance().config;
	}
}
