package com.programmerdan.minecraft.wordbank;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class WordBank extends JavaPlugin {
	private static WordBank plugin;

	public WordBank() {
	}

	public void onEnable() {
		WordBank.plugin = this;
	}

	public void onDisable() {

	}

	public static WordBank instance() {
		return plugin;
	}
	public static Logger log() {
		return WordBank.plugin.getLogger();
	}
}
