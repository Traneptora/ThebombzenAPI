package com.thebombzen.mods.thebombzenapi;

import com.thebombzen.mods.thebombzenapi.configuration.ConfigOption;
import com.thebombzen.mods.thebombzenapi.configuration.ThebombzenAPIConfiguration;

/**
 * This is the ThebombzenAPIConfiguration for ThebombzenAPI itself.
 * @author thebombzen
 */
public class MetaConfiguration extends ThebombzenAPIConfiguration {

	/**
	 * Print update reminders to the user when updates are available.
	 */
	public static ConfigOption UPDATE_REMINDERS = new ConfigOption(true, "UPDATE_REMINDERS", "Update Reminders",
			"Display Update Reminders",
			"Disable if you do not want",
			"to see new updates.");
	
	/**
	 * Check across minecraft versions when checking for updates.
	 * Note: **It is the responsibility of the modder to honor this.**
	 * getVersionFileURLString is abstracted intentionally, and it is your responsibility to honor this option.
	 * To make this easy, use ThebombzenAPI.getCheckAllMinecraftVersions()
	 */
	public static ConfigOption CHECK_ALL_MINECRAFT_VERSIONS = new ConfigOption(true, "CHECK_ALL_MINECRAFT_VERSIONS", "Check All Minecraft Versions",
			"Check for updates across all minecraft versions",
			"Disable to only see only updates",
			"from the current Minecraft version.");
	
	/**
	 * Construct an empty configuration.
	 */
	public MetaConfiguration() {
		super(ThebombzenAPI.instance);
	}

	@Override
	public ConfigOption[] getAllOptions() {
		return new ConfigOption[]{UPDATE_REMINDERS, CHECK_ALL_MINECRAFT_VERSIONS};
	}
	
}
