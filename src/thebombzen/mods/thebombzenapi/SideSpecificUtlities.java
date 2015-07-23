package thebombzen.mods.thebombzenapi;

import java.io.File;

import net.minecraft.util.IChatComponent;

/**
 * This interface provides side-specific functions.
 * 
 * @author thebombzen
 */
public interface SideSpecificUtlities {

	/**
	 * Force a crash if a fatal error occurred.
	 * @param info Short information about the crash.
	 * @param e the Exception that caused it.
	 */
	public void crash(String info, Throwable e);

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
	
	/**
	 * Print a message to the owner. This is the singleplayer user on a client and the server console on the server.
	 * @param chatComponent The chat component to send
	 */
	public void addMessageToOwner(IChatComponent chatComponent);

}
