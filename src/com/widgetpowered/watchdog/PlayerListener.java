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
		final String name = newPlayer.getDisplayName();
		final String playerUUID = newPlayer.getUniqueId().toString();
		
		// Convert to UUID if necessary
		if (WatchDog.getInstance().getConfig().getString("users." + name.toLowerCase()) != null) {
			String node = ("players." + playerUUID);
			
			String addedBy = WatchDog.getInstance().getConfig().getString("users." + name.toLowerCase() + ".addedby");
			String addedOn = WatchDog.getInstance().getConfig().getString("users." + name.toLowerCase() + ".addedon");
			String reason = WatchDog.getInstance().getConfig().getString("users." + name.toLowerCase() + ".reason");
			
			WatchDog.getInstance().getConfig().set((node + ".name"), name);
			WatchDog.getInstance().getConfig().set((node + ".addedby"), addedBy);
			WatchDog.getInstance().getConfig().set((node + ".addedon"), addedOn);
			WatchDog.getInstance().getConfig().set((node + ".reason"), reason);
			
			WatchDog.getInstance().getConfig().set("users." + name.toLowerCase(), null);
			WatchDog.getInstance().saveConfig();
		}
		
		if (WatchDog.getInstance().getConfig().get("players." + playerUUID) != null) {
			String onlineNotice = WatchDog.getInstance().getConfig().getString("messages.playeronline", "Player &c%PLAYER% &fhas logged in! Run &c/wd info %PLAYER% &ffor details.");
			onlineNotice = onlineNotice.replace("%PLAYER%", name);
			
			for (final Player player : WatchDog.getInstance().getServer().getOnlinePlayers()) {
				if ((player != null) && (player instanceof Player) && (player.hasPermission("watchdog.statusupdates") || player.isOp())) {
					WatchDog.getInstance().messenger.printMessage(player, "notice", onlineNotice);
				}
			}
		}
	}
}