package com.widgetpowered.watchdog;

import java.util.Calendar;
import java.util.HashSet;
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
		String arg = "";
		String reason = "";
		Integer length = 0;

		if (command.getName().equalsIgnoreCase("wd")) {
			if (sender.hasPermission("watchdog.use") || sender.isOp()) {
				length = args.length;

				if (length > 0) {
					if (length > 1) {
						arg = args[1];
						
						if (length > 2) {
							reason = stripPlayer(args);
						}
					}
					
					switch(args[0].toLowerCase()) {
						case "add":
							if (sender.hasPermission("watchdog.add") || sender.isOp()) {
								addPlayer(sender, arg, reason);
							}
							
							return true;
						case "remove":
							if (sender.hasPermission("watchdog.remove") || sender.isOp()) {
								removePlayer(sender, arg);
							}
							
							return true;
						case "notify":
							if (sender.hasPermission("watchdog.statusupdates") || sender.isOp()) {
								toggleNotify(sender, arg);
							}
							
							return true;
						case "count":
							getCount(sender);
							return true;
						case "search":
							searchUsers(sender, arg);
							return true;
						case "info":
							getInfo(sender, arg);
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
	 * Strip player name from args (if present)
	 * 
	 * @since       1.0.0
	 * @param       args
	 * @return      reason
	 */
	public String stripPlayer(String[] args) {
		String reason = "";
		
		for (int i = 2; i < args.length; i++) {
			reason += args[i] + (i == args.length - 1 ? "" : " ");
		}
		
		return reason;
	}
	
	
	/**
	 * Toggle notification
	 * 
	 * @since       1.2.0
	 * @return      void
	 */
	public void toggleNotify(CommandSender sender, String arg) {
		String node = ("notify." + sender.getName()).toLowerCase();
		String notice = "";
		
		if (arg.equals("status") || arg == null) {
			String status = getConfig().getString(node);

			if (status == "disabled") {
				notice = getConfig().getString("messages.notificationsdisabled", "Your notifications are disabled.");
			} else {
				notice = getConfig().getString("messages.notificationsenabled", "Your notifications are enabled.");
			}
		} else if (arg.equals("enable") || arg.equals("on")) {
			getConfig().set(node, "enabled");
			saveConfig();
			
			notice = getConfig().getString("messages.notifyenable", "Notifications enabled!");
		} else if(arg.equals("disable") || arg.equals("off")) {
			getConfig().set(node, "disabled");
			saveConfig();
			
			notice = getConfig().getString("messages.notifydisable", "Notifications disabled!");
		}
		
		if (notice == "") {
			sendHelp(sender);
		} else {
			printMessage(sender, "success", notice);
		}
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
	 * Retrieve the info for a player in the watchlist
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
	 * Search for a player in the watchlist
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public void searchUsers(CommandSender sender, String player) {
		if (getConfig().get("users." + player) != null) {
			getInfo(sender, player);
		} else {
			Set<String> users = getConfig().getConfigurationSection("users").getKeys(true);
			Set<String> found = new HashSet<String>();
			int userCount = 0;
			
			if (users != null) {
				for(String user: users) {
					if(! user.contains(".") && user.toLowerCase().contains(player.toLowerCase())) {
						found.add(user);
						userCount++;
					}
				}
			}

			if (! found.isEmpty()) {
				String addedBy = "";
				String addedOn = "";
				String reason = "";
				
				printMessage(sender, "success", "Found the following " + userCount + " players:");
				
				for(String foundUser: found) {
					addedBy = getConfig().getString("users." + foundUser + ".addedby", "console");
					addedOn = getConfig().getString("users." + foundUser + ".addedon", "unknown");
					reason = getConfig().getString("users." + foundUser + ".reason", "unknown");
					
					sender.sendMessage(ChatColor.GOLD + "    + " + ChatColor.WHITE + foundUser + ChatColor.GOLD + " [" + addedBy + " / " + addedOn + "]");
					sender.sendMessage("            - " + reason);
				}
			} else {
				printMessage(sender, "success", "No users found matching \"" + player + "\"");
			}
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
			
			sender.sendMessage(ChatColor.GOLD + "/wd notify [status|on|off] -- Enables/disables your notifications");
			sender.sendMessage(ChatColor.GOLD + "/wd count -- Shows the number of players in the watchlist");
			sender.sendMessage(ChatColor.GOLD + "/wd search [player] -- Search for a player in the watchlist");
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
			String status = getConfig().getString("notify." + sender.getName().toLowerCase());

			if(! messageType.equals("notice") || ! status.equals("disabled")) {
				String prefix = getConfig().getString("prefix." + messageType, "[WatchDog]");
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + ChatColor.WHITE + " " + message));
			}
		}
	}
}