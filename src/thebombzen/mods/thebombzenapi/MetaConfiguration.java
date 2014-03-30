package thebombzen.mods.thebombzenapi;

import thebombzen.mods.thebombzenapi.configuration.ConfigOption;
import thebombzen.mods.thebombzenapi.configuration.ThebombzenAPIConfiguration;

/**
 * This is a dummy ThebombzenAPIConfiguration for ThebombzenAPI itself.
 * Do not model your configuration after this.
 * @author thebombzen
 */
class MetaConfiguration extends ThebombzenAPIConfiguration {

	public MetaConfiguration() {
		super(ThebombzenAPI.instance);
	}

	@Override
	public ConfigOption[] getAllOptions() {
		return new ConfigOption[0];
	}
	
}
