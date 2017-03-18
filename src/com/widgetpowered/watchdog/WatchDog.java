package com.widgetpowered.watchdog;

import org.bukkit.plugin.java.JavaPlugin;

public class WatchDog extends JavaPlugin {
	
	
	/**
	 * The WatchDog instance
	 */
	private static WatchDog instance;
	
	
	/**
	 * Instantiate helper classes
	 */
	public Messenger messenger = new Messenger();
	public PlayerHandler playerHandler = new PlayerHandler();
	public CommandHandler commandHandler = new CommandHandler();
	public Utils utils = new Utils();

	
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
}