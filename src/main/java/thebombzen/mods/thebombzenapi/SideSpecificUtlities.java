package thebombzen.mods.thebombzenapi;

import java.io.File;

/**
 * This interface provides side-specific functions.
 * 
 * @author thebombzen
 */
public interface SideSpecificUtlities {

	/**
	 * Get the base minecraft directory.
	 */
	public File getMinecraftDirectory();

	/**
	 * Returns whether this is the client proxy. This is useful because
	 * constructors don't get an Event and
	 * (this instanceof ClientSideSpecificUtilities) crashes the server
	 * with ClassNotFoundException caused by NoClassDefFoundError
	 * 
	 * @return true if this is the client, false if this is the server;
	 */
	public boolean isClient();

}
