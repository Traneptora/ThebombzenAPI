package thebombzen.mods.thebombzenapi;

/**
 * This is a dummy ThebombzenAPIConfiguration for ThebombzenAPI itself.
 * Do not model your configuration after this.
 * @author thebombzen
 */
public class ThebombzenAPIMetaConfiguration extends ThebombzenAPIConfiguration<ThebombzenAPIMetaConfigOption> {

	public ThebombzenAPIMetaConfiguration() {
		super(ThebombzenAPI.instance, ThebombzenAPIMetaConfigOption.class);
	}
	
}
