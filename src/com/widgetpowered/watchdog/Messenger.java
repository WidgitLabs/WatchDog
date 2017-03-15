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
}