package com.section214.watchdog;

import java.util.Calendar;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WatchDog extends JavaPlugin {
	
	
	/**
	 * The WatchDog instance
	 */
	private static WatchDog instance;
	
	
	/**
	 * Enable all the things
	 *
	 * @since       1.0.0
	 * @return      void
	 */
	@Override
	public void onEnable() {
		saveDefaultConfig();
		
		instance = this;
		
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
	}
	
	
	/**
	 * Return instance
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public static WatchDog getInstance() {
		return instance;
	}
	
	
	/**
	 * Command handler
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String player = "";
		String reason = "";
		Integer length = 0;

		if (command.getName().equalsIgnoreCase("wd")) {
			if (sender.hasPermission("watchdog.use") || sender.isOp()) {
				length = args.length;

				if (length > 0) {
					if (length > 1) {
						player = args[1];

						if (length > 2) {
							reason = setupReason(args);
						}
					}
					
					switch(args[0].toLowerCase()) {
						case "add":
							if (sender.hasPermission("watchdog.add") || sender.isOp()) {
								addPlayer(sender, player, reason);
							}
							
							return true;
						case "remove":
							if (sender.hasPermission("watchdog.remove") || sender.isOp()) {
								removePlayer(sender, player);
							}
							
							return true;
						case "count":
							getCount(sender);
							return true;
						case "info":
							getInfo(sender, player);
							return true;
						case "help":
						default:
							break;
					}
				}
				
				sendHelp(sender);
			}
		}
		
		return true;
	}
	
	
	/**
	 * Setup the reason (if present)
	 * 
	 * @since       1.0.0
	 * @param       args
	 * @return      reason
	 */
	public String setupReason(String[] args) {
		String reason = "";
		
		for (int i = 2; i < args.length; i++) {
			reason += args[i] + (i == args.length - 1 ? "" : " ");
		}
		
		return reason;
	}
	
	
	/**
	 * Add a user to the watchlist
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public void addPlayer(CommandSender sender, String player, String reason) {
		String node = ("users." + player).toLowerCase();
		
		String notice = getConfig().getString("messages.playeradded", "Player %PLAYER% has been added to the watchlist!");
		notice = notice.replace("%PLAYER%", player);
		
		Calendar cal = Calendar.getInstance();
		int mon = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
		String time = String.format("%02d-%02d %02d:%02d:%02d", mon, day, hour, min, sec);

		getConfig().set((node + ".addedby"), sender.getName());
		getConfig().set((node + ".addedon"), time);
		getConfig().set((node + ".reason"), reason);
		saveConfig();
		
		printMessage(sender, "success", notice);
	}
	
	
	/**
	 * Remove a user from the watchlist
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public void removePlayer(CommandSender sender, String player) {
		String removedNotice = getConfig().getString("messages.playerremoved", "Player %PLAYER% has been removed from the watchlist!");
		removedNotice = removedNotice.replace("%PLAYER%", player);
		
		String notFoundNotice = getConfig().getString("messages.playernotfound", "Player %PLAYER% is not in the watchlist!");
		notFoundNotice = notFoundNotice.replace("%PLAYER%", player);
		
		if (getConfig().get("users." + player) != null) {	
			getConfig().set("users." + player, null);
			saveConfig();
			
			printMessage(sender, "success", removedNotice);
		} else {
			printMessage(sender, "success", notFoundNotice);
		}
	}
	
	
	/**
	 * Retrieve the info for a player i the watchlist
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public void getInfo(CommandSender sender, String player) {
		if (getConfig().get("users." + player) != null) {
			String addedBy = getConfig().getString("users." + player + ".addedby", "console");
			String addedOn = getConfig().getString("users." + player + ".addedon", "unknown");
			String reason = getConfig().getString("users." + player + ".reason", "unknown");
			
			printMessage(sender, "success", "Player " + player + " entry:");
			sender.sendMessage(ChatColor.GOLD + "    Added: " + ChatColor.WHITE + addedOn);
			sender.sendMessage(ChatColor.GOLD + "    Added By: " + ChatColor.WHITE + addedBy);
			sender.sendMessage(ChatColor.GOLD + "    Reason: " + ChatColor.WHITE + reason);
		} else {
			String notFoundNotice = getConfig().getString("messages.playernotfound", "Player %PLAYER% is not in the watchlist!");
			notFoundNotice = notFoundNotice.replace("%PLAYER%", player);
			
			printMessage(sender, "success", notFoundNotice);
		}
	}
	
	
	/**
	 * Retrieve a count of the players in the watchlist
	 * 
	 * @since       1.1.0
	 * @return      void
	 */
	public void getCount(CommandSender sender) {
		Set<String> users = getConfig().getConfigurationSection("users").getKeys(true);
		int userCount = 0;
		
		if (users != null) {
			for(String user: users) {
				if(! user.contains(".")) {
					userCount++;
				}
			}
		}
		
		printMessage(sender, "success", "There are " + userCount + " users in the watchlist.");
	}
	
	
	/**
	 * Show the plugin help
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public void sendHelp(CommandSender sender) {
		if (sender.hasPermission("watchdog.use") || sender.isOp()) {
			sender.sendMessage(ChatColor.DARK_AQUA + "WatchDog Help:");
			
			if (sender.hasPermission("watchdog.add") || sender.isOp()) {
				sender.sendMessage(ChatColor.GOLD + "/wd add [player] [reason] -- Adds a player to the watchlist");
			}
			
			if (sender.hasPermission("watchdog.remove") || sender.isOp()) {
				sender.sendMessage(ChatColor.GOLD + "/wd remove [player] -- Removes a player from the watchlist");
			}
			
			sender.sendMessage(ChatColor.GOLD + "/wd count -- Shows the number of players in the watchlist");
			sender.sendMessage(ChatColor.GOLD + "/wd info [player] -- Display the details of a watchlist entry");
		}
	}
	
	
	/**
	 * Outputs a plugin message
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public void printMessage(CommandSender sender, String messageType, String message) {
		if ((sender != null) && (sender instanceof Player)) {
			String prefix = getConfig().getString("prefix." + messageType, "[WatchDog]");
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + ChatColor.WHITE + " " + message));
		}
	}
}