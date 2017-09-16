package com.thebombzen.mods.thebombzenapi;

/**
 * Stores constants that don't require the mod to be loaded or added to the classpath.
 * At this point it's just the version.
 * @author thebombzen
 */
public interface Constants {
	/**
	 * The current version of ThebombzenAPI.
	 */
	public static final String VERSION = "2.9.1";
	/**
	 * The version of Minecraft this was built for.
	 */
	public static final String SUPPORTED_MC_VERSIONS = "1.9.4 to 1.12.1";

	/**
	 * The versions of Minecraft this should be installed in.
	 */
	public static final String[] INSTALL_MC_VERSIONS = {"1.12.1", "1.11.2", "1.10.2", "1.9.4"};
}
