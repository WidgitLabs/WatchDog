package com.widgetpowered.watchdog;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler {
	
	
	/**
	 * Add a user to the watchlist
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public void addPlayer(CommandSender sender, String player, String reason) {
		Player targetPlayer = WatchDog.getInstance().getServer().getPlayerExact(player);
		String playerUUID = "", node = "";
		String notice = WatchDog.getInstance().getConfig().getString("messages.playeradded", "Player %PLAYER% has been added to the watchlist!");
		notice = notice.replace("%PLAYER%", player);
		
		Calendar cal = Calendar.getInstance();
		int mon = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
		String time = String.format("%02d-%02d %02d:%02d:%02d", mon, day, hour, min, sec);
		
		if (! player.isEmpty()) {
			if (targetPlayer != null) {
				playerUUID = targetPlayer.getUniqueId().toString();
			}

			if (! playerUUID.isEmpty()) {
				node = ("players." + playerUUID);

				WatchDog.getInstance().getConfig().set((node + ".name"), player);
			} else {
				node = ("users." + player.toLowerCase());
			}

			WatchDog.getInstance().getConfig().set((node + ".addedby"), sender.getName());
			WatchDog.getInstance().getConfig().set((node + ".addedon"), time);
			WatchDog.getInstance().getConfig().set((node + ".reason"), reason);
			WatchDog.getInstance().saveConfig();

			WatchDog.getInstance().messenger.printMessage(sender, "success", notice);
		}
	}
	
	
	/**
	 * Remove a user from the watchlist
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	public void removePlayer(CommandSender sender, String player) {
		Player targetPlayer = WatchDog.getInstance().getServer().getPlayerExact(player);
		String playerUUID = "";
		Boolean found = false;
		String removedNotice = WatchDog.getInstance().getConfig().getString("messages.playerremoved", "Player %PLAYER% has been removed from the watchlist!");
		String notFoundNotice = WatchDog.getInstance().getConfig().getString("messages.playernotfound", "Player %PLAYER% is not in the watchlist!");
		
		if (! player.isEmpty()) {
			if (targetPlayer != null) {
				playerUUID = targetPlayer.getUniqueId().toString();
			}
			
			removedNotice = removedNotice.replace("%PLAYER%", player);
			notFoundNotice = notFoundNotice.replace("%PLAYER%", player);
			
			if (WatchDog.getInstance().getConfig().get("users." + player.toLowerCase()) != null) {
				// Check old player name method
				found = true;
				
				WatchDog.getInstance().getConfig().set("users." + player.toLowerCase(), null);
				WatchDog.getInstance().saveConfig();
			} else if (! playerUUID.isEmpty() && WatchDog.getInstance().getConfig().getString("players." + playerUUID) != null) {
				// Check new UUID method
				found = true;
				
				removedNotice = removedNotice.replace("%PLAYER%", WatchDog.getInstance().getConfig().getString("players." + playerUUID + ".name"));
				notFoundNotice = notFoundNotice.replace("%PLAYER%", WatchDog.getInstance().getConfig().getString("players." + playerUUID + ".name"));

				WatchDog.getInstance().getConfig().set("players." + playerUUID, null);
				WatchDog.getInstance().saveConfig();
			} else {
				Set<String> players = WatchDog.getInstance().getConfig().getConfigurationSection("players").getKeys(true);

				if (players != null) {
					for (String playerRecord: players) {
						if (! playerRecord.contains(".")) {
							String playerName = WatchDog.getInstance().getConfig().getString("players." + playerRecord + ".name");

							if (playerName.equalsIgnoreCase(player)) {
								found = true;

								WatchDog.getInstance().getConfig().set("players." + playerRecord, null);
								WatchDog.getInstance().saveConfig();
							}
						}
					}
				}
			}

			if (found) {
				WatchDog.getInstance().messenger.printMessage(sender, "success", removedNotice);
			} else {
				WatchDog.getInstance().messenger.printMessage(sender, "success", notFoundNotice);
			}
		}
	}
	
	
	/**
	 * Toggle notification
	 * 
	 * @since       1.2.0
	 * @return      void
	 */
	public void toggleNotify(CommandSender sender, String param) {
		String senderUUID = WatchDog.getInstance().getServer().getPlayerExact(sender.getName()).getUniqueId().toString();
		
		String oldNode = ("notify." + sender.getName()).toLowerCase();
		String node = ("notify." + senderUUID);
		String notice = "", status = "";
		
		// Convert to UUID if necessary
		if (WatchDog.getInstance().getConfig().getString(oldNode) != null) {
			status = WatchDog.getInstance().getConfig().getString(oldNode);
			
			WatchDog.getInstance().getConfig().set(node, status);
			WatchDog.getInstance().getConfig().set(oldNode, null);
		}
		
		if (param.equals("status") || param == null) {
			status = WatchDog.getInstance().getConfig().getString(node);

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
		
		// Check old player name method
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
		
		// Check new UUID method
		if (WatchDog.getInstance().getConfig().isConfigurationSection("players")) {
			Set<String> players = WatchDog.getInstance().getConfig().getConfigurationSection("players").getKeys(true);
			
			if (players != null) {
				for (String player: players) {
					if (! player.contains(".")) {
						String playerName = WatchDog.getInstance().getConfig().getString("players." + player + ".name");

						if (online.equals("online")) {
							Player targetPlayer = WatchDog.getInstance().getServer().getPlayer(playerName);
							if (targetPlayer != null && targetPlayer.isOnline()) {
								userCount++;
								userList += playerName + ", ";
							}
						} else {
							userCount++;
							userList += playerName + ", ";
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
		Player targetPlayer = WatchDog.getInstance().getServer().getPlayerExact(player);
		String playerUUID = "";
		
		if (! player.isEmpty()) {
			if (targetPlayer != null) {
				playerUUID = targetPlayer.getUniqueId().toString();
			}
		
			if (WatchDog.getInstance().getConfig().get("users." + player.toLowerCase()) != null) {
				// Check old name based method
				getInfo(sender, player);
			} else if(! playerUUID.isEmpty() && WatchDog.getInstance().getConfig().get("players." + playerUUID) != null) {
				// Check new UUID method
				getInfo(sender, player);
			} else {
				Set<String> users = WatchDog.getInstance().getConfig().getConfigurationSection("users").getKeys(true);
				Set<String> players = WatchDog.getInstance().getConfig().getConfigurationSection("players").getKeys(true);
				Set<String> found = new HashSet<String>();
				int userCount = 0;
				
				if (users != null) {
					for (String user: users) {
						if (! user.contains(".") && user.toLowerCase().contains(player.toLowerCase())) {
							found.add(user);
							userCount++;
						}
					}
				}
			
				if (players != null) {
					for (String playerRecord: players) {
						if (! playerRecord.contains(".")) {
							String targetPlayerName = WatchDog.getInstance().getConfig().getString("players." + playerRecord + ".name");
							
							if (targetPlayerName.toLowerCase().contains(player.toLowerCase())) {
								found.add(targetPlayerName);
								userCount++;
							}
						}
					}
				}

				if (! found.isEmpty()) {
					WatchDog.getInstance().messenger.printMessage(sender, "success", "Found the following " + userCount + " players:");
					
					for(String foundUser: found) {
						getInfo(sender, foundUser);
					}
				} else {
					WatchDog.getInstance().messenger.printMessage(sender, "success", "No users found matching \"" + player + "\"");
				}
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
		Boolean found = false;
		String addedBy = "", addedOn = "", reason = "";
		Player targetPlayer = WatchDog.getInstance().getServer().getPlayerExact(player);
		String playerUUID = "";
		
		if (! player.isEmpty()) {
			if (targetPlayer != null) {
				playerUUID = targetPlayer.getUniqueId().toString();
			}
			
			if (WatchDog.getInstance().getConfig().get("users." + player.toLowerCase()) != null) {
				found = true;
				
				addedBy = WatchDog.getInstance().getConfig().getString("users." + player.toLowerCase() + ".addedby", "console");
				addedOn = WatchDog.getInstance().getConfig().getString("users." + player.toLowerCase() + ".addedon", "unknown");
				reason = WatchDog.getInstance().getConfig().getString("users." + player.toLowerCase() + ".reason", "unknown");
			} else if (! playerUUID.isEmpty() && WatchDog.getInstance().getConfig().getString("players." + playerUUID) != null) {
				found = true;
	
				addedBy = WatchDog.getInstance().getConfig().getString("players." + playerUUID + ".addedby", "console");
				addedOn = WatchDog.getInstance().getConfig().getString("players." + playerUUID + ".addedon", "unknown");
				reason = WatchDog.getInstance().getConfig().getString("players." + playerUUID + ".reason", "unknown");
			} else {
				Set<String> players = WatchDog.getInstance().getConfig().getConfigurationSection("players").getKeys(true);

				if (players != null) {
					for (String playerRecord: players) {
						String playerName = WatchDog.getInstance().getConfig().getString("players." + playerRecord + ".name");
						
						if (playerName.toLowerCase() == player.toLowerCase()) {
							found = true;
							
							addedBy = WatchDog.getInstance().getConfig().getString("players." + playerRecord + ".addedby", "console");
							addedOn = WatchDog.getInstance().getConfig().getString("players." + playerRecord + ".addedon", "unknown");
							reason = WatchDog.getInstance().getConfig().getString("players." + playerRecord + ".reason", "unknown");
						}
					}
				}
			}

			if (found) {
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
}