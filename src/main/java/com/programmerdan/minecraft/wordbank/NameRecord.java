package com.programmerdan.minecraft.wordbank;

import com.programmerdan.minecraft.wordbank.data.WordBankData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

/**
 *
 * @author caucow
 */
public class NameRecord {
	public final String key;
	public final String value;
	private boolean marked;
	
	public NameRecord(String key, String value, boolean marked) {
		this.key = Objects.requireNonNull(key);
		this.value = Objects.requireNonNull(value);
		this.marked = marked;
	}
	
	/**
	 * Marks the wordbank key/value pair as used in the database, along with
	 * player UUID and the item type it was applied to. 
	 * @param wbplugin WordBank plugin (for database access)
	 * @param playerId player UUID
	 * @param itemType item type name was applied to
	 * @param force force a database update with the current pID and itemType
	 * regardless of whether this name is already marked
	 */
	public void mark(WordBank wbplugin, String playerId, String itemType, boolean force) {
		if (marked && !force) {
			return;
		}
		marked = true;
		if (wbplugin.config().hasDB()) {
			try {
				if (wbplugin.config().isDebug()) wbplugin.logger().info("  - Inserting item record");
				long startTime = 0, endTime = 0;
				startTime = System.nanoTime();
				try (
						Connection connection = wbplugin.data().getConnection();
						PreparedStatement insert = connection.prepareStatement(WordBankData.insert)) {
					insert.setString(1, key);
					insert.setString(2, playerId);
					insert.setString(3, itemType);
					insert.setString(4, value);
					insert.executeUpdate();
					insert.close();
					endTime = System.nanoTime();
					if (wbplugin.config().isDebug()) wbplugin.logger().log(Level.INFO, "Wrote key/value {0}={1} to database in {2}ms", new Object[] { key, value, (endTime - startTime) / 1_000_000.0F });
				}
			} catch (SQLException se) {
				wbplugin.logger().log(Level.WARNING, "Failed to insert key utilization", se);
			}
		}
	}
	
	public boolean isMarked() {
		return marked;
	}
}
