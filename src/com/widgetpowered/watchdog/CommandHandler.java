package com.widgetpowered.watchdog;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler {
	
	
	/**
	 * Command handler
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Process calls to 'wd' and 'watchdog' only
		if (! command.getName().equalsIgnoreCase("wd") && ! command.getName().equalsIgnoreCase("watchdog")) {
			return false;
		}

		// Bail if user doesn't have at least the 'use' permission
		if (! sender.hasPermission("watchdog.use")) {
			return false;
		}
		
		// Bail if no option is specified
		if (args.length == 0) {
			WatchDog.getInstance().messenger.sendHelp(sender);
			return true;
		}
		
		// Parse out the passed option
		String option = args[0];
		String player = args[1];
		String reason, param;
		
		// Bail if the user can't access the specified option
		if (! WatchDog.getInstance().playerHandler.canAccess(sender, option)) {
			WatchDog.getInstance().messenger.sendHelp(sender);
			return false;
		}
		
		switch (option) {
			case "add":
				reason = WatchDog.getInstance().utils.parseReason(args);
				
				addPlayer(sender, player, reason);
				break;
			case "remove":				
				removePlayer(sender, player);
				break;
			case "notify":
				param = args[1];
				
				toggleNotify(sender, param);
				break;
			case "count":
			case "list":
				String status = args[1];
				param = args[2];
				
				getWatchlist(sender, status, param);
				break;
			case "search":
				searchUsers(sender, player);
				break;
			case "info":
				getInfo(sender, player);
				break;
			case "help":
			default:
				break;
		}
		
		WatchDog.getInstance().messenger.sendHelp(sender);
		return true;
	}
	
	
	/**
	 * Add a user to the watchlist
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public void addPlayer(CommandSender sender, String player, String reason) {
		String node = ("users." + player).toLowerCase();
		
		String notice = WatchDog.getInstance().getConfig().getString("messages.playeradded", "Player %PLAYER% has been added to the watchlist!");
		notice = notice.replace("%PLAYER%", player);
		
		Calendar cal = Calendar.getInstance();
		int mon = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
		String time = String.format("%02d-%02d %02d:%02d:%02d", mon, day, hour, min, sec);

		WatchDog.getInstance().getConfig().set((node + ".addedby"), sender.getName());
		WatchDog.getInstance().getConfig().set((node + ".addedon"), time);
		WatchDog.getInstance().getConfig().set((node + ".reason"), reason);
		WatchDog.getInstance().saveConfig();
		
		WatchDog.getInstance().messenger.printMessage(sender, "success", notice);
	}
	
	
	/**
	 * Remove a user from the watchlist
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public void removePlayer(CommandSender sender, String player) {
		String removedNotice = WatchDog.getInstance().getConfig().getString("messages.playerremoved", "Player %PLAYER% has been removed from the watchlist!");
		removedNotice = removedNotice.replace("%PLAYER%", player);
		
		String notFoundNotice = WatchDog.getInstance().getConfig().getString("messages.playernotfound", "Player %PLAYER% is not in the watchlist!");
		notFoundNotice = notFoundNotice.replace("%PLAYER%", player);
		
		if (WatchDog.getInstance().getConfig().get("users." + player.toLowerCase()) != null) {	
			WatchDog.getInstance().getConfig().set("users." + player.toLowerCase(), null);
			WatchDog.getInstance().saveConfig();
			
			WatchDog.getInstance().messenger.printMessage(sender, "success", removedNotice);
		} else {
			WatchDog.getInstance().messenger.printMessage(sender, "success", notFoundNotice);
		}
	}
	
	
	/**
	 * Toggle notification
	 * 
	 * @since       1.2.0
	 * @return      void
	 */
	public void toggleNotify(CommandSender sender, String param) {
		String node = ("notify." + sender.getName()).toLowerCase();
		String notice = "";
		
		if (param.equals("status") || param == null) {
			String status = WatchDog.getInstance().getConfig().getString(node);

			if (status == "disabled") {
				notice = WatchDog.getInstance().getConfig().getString("messages.notificationsdisabled", "Your notifications are disabled.");
			} else {
				notice = WatchDog.getInstance().getConfig().getString("messages.notificationsenabled", "Your notifications are enabled.");
			}
		} else if (param.equals("enable") || param.equals("on")) {
			WatchDog.getInstance().getConfig().set(node, "enabled");
			WatchDog.getInstance().saveConfig();
			
			notice = WatchDog.getInstance().getConfig().getString("messages.notifyenable", "Notifications enabled!");
		} else if(param.equals("disable") || param.equals("off")) {
			WatchDog.getInstance().getConfig().set(node, "disabled");
			WatchDog.getInstance().saveConfig();
			
			notice = WatchDog.getInstance().getConfig().getString("messages.notifydisable", "Notifications disabled!");
		}
		
		if (notice == "") {
			WatchDog.getInstance().messenger.sendHelp(sender);
		} else {
			WatchDog.getInstance().messenger.printMessage(sender, "success", notice);
		}
	}
	
	
	/**
	 *  Retrieve the watchlist
	 *  
	 *  @since      1.2.2
	 *  @return     void
	 */
	public void getWatchlist(CommandSender sender, String online, String displayType) {
		int userCount = 0;
		String userList = "";
		String onlineStatus = " ";
		
		if (WatchDog.getInstance().getConfig().isConfigurationSection("users")) {
			Set<String> users = WatchDog.getInstance().getConfig().getConfigurationSection("users").getKeys(true);
			
			if (users != null) {
				for(String user: users) {
					if (! user.contains(".")) {
						if (online.equals("online")) {
							Player targetPlayer = WatchDog.getInstance().getServer().getPlayerExact(user);
							if (targetPlayer != null && targetPlayer.isOnline()) {
								userCount++;
								userList += user + ", ";
							}
						} else {
							userCount++;
							userList += user + ", ";
						}
					}
				}
			}
		}
		
		if (online.equals("online")) {
			onlineStatus = " online ";
		}
		
		if (userCount == 0) {
			WatchDog.getInstance().messenger.printMessage(sender, "success", "There are no" + onlineStatus + "users in the watchlist.");
		} else if (userCount == 1) {
			WatchDog.getInstance().messenger.printMessage(sender, "success", "There is 1" + onlineStatus + "user in the watchlist.");
			
			if (displayType.equals("list")) {
				sender.sendMessage(userList.substring(0, userList.length() - 2));
			}
		} else {
			WatchDog.getInstance().messenger.printMessage(sender, "success", "There are " + userCount + onlineStatus + "users in the watchlist.");
			
			if (displayType.equals("list")) {
				sender.sendMessage(userList.substring(0, userList.length() - 2));
			}
		}
	}
	
	
	/**
	 * Search for a player in the watchlist
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public void searchUsers(CommandSender sender, String player) {
		if (WatchDog.getInstance().getConfig().get("users." + player.toLowerCase()) != null) {
			getInfo(sender, player);
		} else {
			Set<String> users = WatchDog.getInstance().getConfig().getConfigurationSection("users").getKeys(true);
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
				
				WatchDog.getInstance().messenger.printMessage(sender, "success", "Found the following " + userCount + " players:");
				
				for(String foundUser: found) {
					addedBy = WatchDog.getInstance().getConfig().getString("users." + foundUser + ".addedby", "console");
					addedOn = WatchDog.getInstance().getConfig().getString("users." + foundUser + ".addedon", "unknown");
					reason = WatchDog.getInstance().getConfig().getString("users." + foundUser + ".reason", "unknown");
					
					sender.sendMessage(ChatColor.GOLD + "    + " + ChatColor.WHITE + foundUser + ChatColor.GOLD + " [" + addedBy + " / " + addedOn + "]");
					sender.sendMessage("            - " + reason);
				}
			} else {
				WatchDog.getInstance().messenger.printMessage(sender, "success", "No users found matching \"" + player + "\"");
			}
		}
	}
	
	
	/**
	 * Retrieve the info for a player in the watchlist
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public void getInfo(CommandSender sender, String player) {
		if (WatchDog.getInstance().getConfig().get("users." + player.toLowerCase()) != null) {
			String addedBy = WatchDog.getInstance().getConfig().getString("users." + player + ".addedby", "console");
			String addedOn = WatchDog.getInstance().getConfig().getString("users." + player + ".addedon", "unknown");
			String reason = WatchDog.getInstance().getConfig().getString("users." + player + ".reason", "unknown");
			
			WatchDog.getInstance().messenger.printMessage(sender, "success", "Player " + player + " entry:");
			sender.sendMessage(ChatColor.GOLD + "    Added: " + ChatColor.WHITE + addedOn);
			sender.sendMessage(ChatColor.GOLD + "    Added By: " + ChatColor.WHITE + addedBy);
			sender.sendMessage(ChatColor.GOLD + "    Reason: " + ChatColor.WHITE + reason);
		} else {
			String notFoundNotice = WatchDog.getInstance().getConfig().getString("messages.playernotfound", "Player %PLAYER% is not in the watchlist!");
			notFoundNotice = notFoundNotice.replace("%PLAYER%", player);
			
			WatchDog.getInstance().messenger.printMessage(sender, "success", notFoundNotice);
		}
	}
}