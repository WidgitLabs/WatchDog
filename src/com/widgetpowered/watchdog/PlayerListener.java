package com.widgetpowered.watchdog;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
	
	
	/**
	 * Track player joins
	 * 
	 * @since       1.0.0
	 * @return      void
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player newPlayer = event.getPlayer();
		final String name = newPlayer.getDisplayName().toLowerCase();
		
		if (WatchDog.getInstance().getConfig().get("users." + name) != null) {
			String onlineNotice = WatchDog.getInstance().getConfig().getString("messages.playeronline", "Player &c%PLAYER% &fhas logged in! Run &c/wd info %PLAYER% &ffor details.");
			onlineNotice = onlineNotice.replace("%PLAYER%", name);
			
			for (final Player player : WatchDog.getInstance().getServer().getOnlinePlayers()) {
				if ((player != null) && (player instanceof Player) && (player.hasPermission("watchdog.statusupdates") || player.isOp())) {
					WatchDog.getInstance().printMessage(player, "notice", onlineNotice);
				}
			}
		}
	}
}