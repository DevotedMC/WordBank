package com.programmerdan.minecraft.wordbank.actions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.programmerdan.minecraft.wordbank.WordBank;
import com.programmerdan.minecraft.wordbank.data.WordBankData;

public class CommandListener implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		int page = 0;
		int count = 15;
		
		String key = null;
		
		if (args.length == 1) {
			try {
				int temp = Integer.valueOf(args[0]);
				page = temp;
			} catch (NumberFormatException nfe) {
				key = args[0];
			}
		} else if (args.length > 1) {
			try {
				int temp = Integer.valueOf(args[args.length - 1]);
				page = temp;
				
				key = String.join(" ", Arrays.copyOfRange(args, 0, args.length - 1));
			} catch (NumberFormatException nfe) {
				page = 0;
				key = String.join(" ", Arrays.copyOf(args, args.length));
			}
		}
		
		if (page < 0 || (key != null && key.length() != WordBank.config().getActivationLength())) {
			return false;
		}
		
		if (sender == null) {
			return false;
		}
		
		if (key == null) {
			try {
				Connection connection = WordBank.data().getConnection();
				PreparedStatement statement = connection.prepareStatement(WordBankData.keys);
				statement.setInt(1, count);
				statement.setInt(2, page * count);
				ResultSet rs = statement.executeQuery();
				sender.sendMessage(String.format("Listing all keys:    %s(Page %d)",
						ChatColor.BLUE, page));
				while (rs.next()) {
					sender.sendMessage(String.format("%s%10s %s-> %s%s %sUsed %d times",
							ChatColor.GOLD, rs.getString(1), ChatColor.GRAY,
							ChatColor.AQUA, rs.getString(2),
							ChatColor.WHITE, rs.getInt(3)));
				}
				rs.close();
				statement.close();
				connection.close();
			} catch (SQLException se) {
				WordBank.log().log(Level.WARNING, "Failed to retrieve data!", se);
			}
		} else {
			try {
				Connection connection = WordBank.data().getConnection();
				PreparedStatement statement = connection.prepareStatement(WordBankData.key);
				statement.setString(1, key);
				statement.setInt(2, count);
				statement.setInt(3, page * count);
				ResultSet rs = statement.executeQuery();
				sender.sendMessage(String.format("Listing key %s:    %s(Page %d)",
						key, ChatColor.BLUE, page));
				while (rs.next()) {
					sender.sendMessage(String.format("%s%10s %s: %sUsed %d total times on %d types of items",
							ChatColor.GOLD, rs.getString(1), ChatColor.GRAY,
							ChatColor.WHITE, rs.getInt(2), rs.getInt(3)));
				}
				rs.close();
				statement.close();
				connection.close();				
			} catch (SQLException se) {
				WordBank.log().log(Level.WARNING, "Failed to retrieve key data!", se);
			}
		}
		
		return true;
	}

}
