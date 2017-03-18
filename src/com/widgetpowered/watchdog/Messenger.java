package com.widgetpowered.watchdog;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Messenger {
	
	
	/**
	 * Outputs a plugin message
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public void printMessage(CommandSender sender, String messageType, String message) {
		if ((sender != null) && (sender instanceof Player)) {
			String status = WatchDog.getInstance().getConfig().getString("notify." + sender.getName().toLowerCase());

			if(! messageType.equals("notice") || ! status.equals("disabled")) {
				String prefix = WatchDog.getInstance().getConfig().getString("prefix." + messageType, "[WatchDog]");
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + ChatColor.WHITE + " " + message));
			}
		}
	}
	
	
	/**
	 * Show the plugin help
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public void sendHelp(CommandSender sender) {
		if (WatchDog.getInstance().playerHandler.canAccess(sender, "help")) {
			sender.sendMessage(ChatColor.DARK_AQUA + "WatchDog Help:");
			
			if (WatchDog.getInstance().playerHandler.canAccess(sender, "add")) {
				sender.sendMessage(ChatColor.GOLD + "/wd add [player] [reason] -- Adds a player to the watchlist");
			}
			
			if (WatchDog.getInstance().playerHandler.canAccess(sender, "remove")) {
				sender.sendMessage(ChatColor.GOLD + "/wd remove [player] -- Removes a player from the watchlist");
			}
			
			if (WatchDog.getInstance().playerHandler.canAccess(sender, "notify")) {
				sender.sendMessage(ChatColor.GOLD + "/wd notify [status|on|off] -- Enables/disables your notifications");
			}
			
			sender.sendMessage(ChatColor.GOLD + "/wd count [online] -- Shows the number of players in the watchlist");
			sender.sendMessage(ChatColor.GOLD + "/wd list [online] -- Shows a list of the players in the watchlist");
			sender.sendMessage(ChatColor.GOLD + "/wd search [player] -- Search for a player in the watchlist");
			sender.sendMessage(ChatColor.GOLD + "/wd info [player] -- Display the details of a watchlist entry");
		}
	}
}