package com.programmerdan.minecraft.wordbank;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.InvalidPluginException;

import com.zaxxer.hikari.HikariConfig;

public class WordBankConfig {
	private static int expected_config_level = 1;
	
	private HikariConfig db_config;
	private WordList words;
	private int activation_length;
	private boolean activate_any_length;
	private String padding;
	private ItemStack cost;
	private CharConfig color;
	private int word_max;
	private CharConfig word_count;
	private CharConfig[] word_config;
	private String makers_mark;
	private boolean debug;
	private WordBank plugin;
	private long confirm_delay;
	private boolean fail_rename_on_db_error;
	private int namecache_invalidate_minutes;
	private int namecache_max_size;
	private boolean dblog_all_item_marks;
	private boolean prevent_dblookup_spam;
	
	public WordBankConfig(ConfigurationSection config) throws InvalidPluginException {
		this(config, null);
	}
	public WordBankConfig(ConfigurationSection config, WordBank plugin) throws InvalidPluginException {
		this.plugin = plugin;
		int actual_config_level = config.getInt("configuration_file_version", -1);
		if (actual_config_level < 0 || actual_config_level > WordBankConfig.expected_config_level) {
			throw new org.bukkit.plugin.InvalidPluginException("Invalid configuration file");
		}
		
		this.cost = config.getItemStack("cost", new ItemStack(Material.EXP_BOTTLE, 10));
		this.debug = config.getBoolean("debug", false);
		
		try (InputStream words = new FileInputStream(
				new File(plugin().getDataFolder(),config.getString("wordlist_file")))) {
			this.words = new WordList(words);
		} catch (IOException e) {
			plugin().logger().log(Level.SEVERE, "Failed to load word list.", e);
		} catch (NullPointerException npe) {
			plugin().logger().log(Level.SEVERE, "Failed to load word list.", npe);
		}
		
		this.activation_length = config.getInt("activation_length", 10);
		this.activate_any_length = config.getBoolean("activate_any_length", false);
		this.padding = config.getString("padding", " ").substring(0, 1);
		
		this.word_max = config.getInt("word.max", 3);
		this.makers_mark = config.getString("makers_mark", "Marked Item");
		this.color = new CharConfig(config.getConfigurationSection("color"), activation_length);
		this.word_count = new CharConfig(config.getConfigurationSection("word.count"), activation_length);
		this.word_config = new CharConfig[this.word_max];
		for (int a = 0; a < this.word_max; a++) {
			this.word_config[a] = new CharConfig(config.getConfigurationSection("word." + a), activation_length);
		}
		
		this.confirm_delay = config.getLong("confirm_delay", 10000l);
		
		// true if, should the database be enabled AND throw an exception when
		// getting a value for a key name, wordbank should skip trying to generate
		// a new value for that key (prevents possible ambiguity in names if
		// the configs change and the che cache can't access the database)
		this.fail_rename_on_db_error = config.getBoolean("fail_rename_on_db_error", true);
		this.namecache_invalidate_minutes = config.getInt("namecache_invalidate_minutes", 5);
		this.namecache_max_size = config.getInt("namecache_max_size", 500);
		// Before the change to use async load/generate, every time a player
		// used wordbank to generate a name for an item, it added a new entry to
		// the database regardless of whether an entry already existed with that
		// wbkey. Setting this to true will keep that old behavior (for...
		// counting number of times a name is used or something? idk)
		this.dblog_all_item_marks = config.getBoolean("dblog_all_item_marks", false);
		// Set to true if players are spamming wordbank too fast and slowing the
		// database.
		this.prevent_dblookup_spam = config.getBoolean("prevent_dblookup_spam", true);
		
		// dbconfig 
		this.db_config = null;
		ConfigurationSection db = config.getConfigurationSection("db");
		if (db != null && db.contains("user") && db.contains("name")) {
			this.db_config = new HikariConfig();
			this.db_config.setJdbcUrl("jdbc:" + db.getString("driver", "mysql") + "://" + db.getString("host", "localhost") + ":" +
					db.getString("port", "3306") + "/" + db.getString("name"));
			
			this.db_config.setConnectionTimeout(3000l);
			this.db_config.setIdleTimeout(1800000l);
			this.db_config.setMaxLifetime(7200000l);
			this.db_config.setMaximumPoolSize(2);
			this.db_config.setUsername(db.getString("user"));
			this.db_config.setPassword(db.getString("password"));
			this.db_config.addDataSourceProperty("cachePrepStmts", "true");
			this.db_config.addDataSourceProperty("prepStmtCacheSize", "10");
			this.db_config.addDataSourceProperty("prepStmtCacheSqlLimit", "256");
		}
	}
	
	protected WordBank plugin() {
		return this.plugin == null ? WordBank.instance() : this.plugin; 
	}

	public WordList getWords() {
		return words;
	}

	public int getActivationLength() {
		return activation_length;
	}
	
	public boolean isActivateAnyLength() {
		return activate_any_length;
	}
	
	public String getPadding(){
		return padding;
	}

	public ItemStack getCost() {
		return cost;
	}

	public CharConfig getColor() {
		return color;
	}

	public int getWordMax() {
		return word_max;
	}

	public CharConfig getWordCount() {
		return word_count;
	}

	public CharConfig getWordConfig(int idx) {
		if (idx < 0 || idx > word_config.length) {
			throw new IndexOutOfBoundsException("No word config exists for that");
		}
		return word_config[idx];
	}
	
	public String getMakersMark() {
		return makers_mark;
	}
	
	public long getConfirmDelay() {
		return confirm_delay;
	}

	public boolean isDebug() {
		return debug;
	}
	
	public boolean hasDB() {
		return db_config != null;
	}
	
	public HikariConfig database() {
		return this.db_config;
	}
	
	public boolean isFailRenameOnDbError() {
		return fail_rename_on_db_error;
	}
	
	public int getNamecacheInvalidateMinutes() {
		return namecache_invalidate_minutes;
	}
	
	public int getNamecacheMaxSize() {
		return namecache_max_size;
	}
	
	public boolean isDBLogAllItemMarks() {
		return dblog_all_item_marks;
	}
	
	public boolean isPreventDBLookupSpam() {
		return prevent_dblookup_spam;
	}
}
