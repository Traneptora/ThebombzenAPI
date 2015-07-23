package thebombzen.mods.thebombzenapi;

import thebombzen.mods.thebombzenapi.configuration.ConfigOption;
import thebombzen.mods.thebombzenapi.configuration.ThebombzenAPIConfiguration;

/**
 * This is the ThebombzenAPIConfiguration for ThebombzenAPI itself.
 * @author thebombzen
 */
public class MetaConfiguration extends ThebombzenAPIConfiguration {

	public static ConfigOption UPDATE_REMINDERS = new ConfigOption(true, "UPDATE_REMINDERS", "Update Reminders",
			"Display Update Reminders",
			"Disable if you do not want",
			"to see new updates.");
	
	public MetaConfiguration() {
		super(ThebombzenAPI.instance);
	}

	@Override
	public ConfigOption[] getAllOptions() {
		return new ConfigOption[]{UPDATE_REMINDERS};
	}
	
}
