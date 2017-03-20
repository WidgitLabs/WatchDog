package com.widgetpowered.watchdog;

import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;


/**
 * Update checker
 *
 * @since       1.3.1
 */
public class UpdateCheck {
	
	
	private final WatchDog plugin;
	
	
	public UpdateCheck(WatchDog plugin) {
		this.plugin = plugin;
	}
	
	
	/**
	 * Check Spigot API for updates
	 * 
	 * @since       1.3.1
	 * @return      void
	 */
	@SuppressWarnings("resource")
	public void checkForUpdates() {
		try {
			plugin.getServer().getLogger().info("[WatchDog] Checking for updates...");
			
			HttpURLConnection connection = (HttpURLConnection) new URL("http://www.spigotmc.org/api/general.php").openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.getOutputStream().write(("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=37932").getBytes("UTF-8"));
			
			String version = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
			
			// Check if version is valid
			if (version.length() <= 7) {

				//if (version.equalsIgnoreCase(plugin.getDescription().getVersion())) {
				if (compare(plugin.getDescription().getVersion(), version)) {
					plugin.getServer().getLogger().info("[WatchDog] Version " + version + " is available! Go grab it now!");
				} else {
					if (version.equals(plugin.getDescription().getVersion())) {
						plugin.getServer().getLogger().info("[WatchDog] You have the latest version of WatchDog. All secure here!");
					} else {
						plugin.getServer().getLogger().info("[WatchDog] The latest version is " + version + " but you're running " + plugin.getDescription().getVersion() + "... Are you a beta tester???");
					}
				}
			}
		} catch (Exception ex) {
			plugin.getServer().getLogger().info("[WatchDog] Failed to check if plugin is up to date. Will try again later.");
			plugin.getServer().getLogger().info("[WatchDog] Exception: " + ex.getMessage());
		}
	}
	
	
	/**
	 * Version comparison
	 * 
	 * @since       1.3.1
	 * @return      boolean
	 */
	private static boolean compare(String oldVersion, String newVersion) {
		oldVersion = normalize(oldVersion);
		newVersion = normalize(newVersion);
		
		int cmp = oldVersion.compareTo(newVersion);
		
		return cmp < 0 ? true : cmp > 0 ? false : false;
	}
	
	
	/**
	 * Normalize version strings
	 * 
	 * @since       1.3.1
	 * @return      string
	 */
	public static String normalize(String version) {
		return normalizeVersion(version, ".", 4);
	}
	
	
	/**
	 * Normalize version strings
	 * 
	 * @since       1.3.1
	 * @return      string
	 */
	public static String normalizeVersion(String version, String sep, int maxWidth) {
		String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
		StringBuilder sb = new StringBuilder();
		for (String s:split) {
			sb.append(String.format("%" + maxWidth + 's',  s));
		}
		return sb.toString();
	}
}