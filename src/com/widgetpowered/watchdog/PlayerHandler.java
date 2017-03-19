package com.widgetpowered.watchdog;

import org.bukkit.command.CommandSender;

public class PlayerHandler {

	
	/**
	 * Check player access 
	 *
	 * @since       1.0.0
	 * @return      Boolean hasAccess Whether or not the player can access a given command
	 */
	public Boolean canAccess(CommandSender sender, String option) {
		Boolean hasAccess = false;

		if (sender.isOp()) {
			hasAccess = true;
		} else {
			if (sender.hasPermission("watchdog.use")) {
				switch(option.toLowerCase()) {
					case "add":
						hasAccess = sender.hasPermission("watchdog.add");
						break;
					case "remove":
						hasAccess = sender.hasPermission("watchdog.remove");
						break;
					case "notify":
						hasAccess = sender.hasPermission("watchdog.statusupdates");
						break;
					default:
						hasAccess = true;
						break;
				}
			}
		}

		return hasAccess;
	}
}