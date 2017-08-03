package com.thebombzen.mods.thebombzenapi;

import java.io.File;

import net.minecraft.util.text.ITextComponent;

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
	 * On the client this is the standard Minecraft directory.
	 * On the server this is the base directory of the server.
	 */
	public File getMinecraftDirectory();
	
	/**
	 * Returns whether this is the client proxy. This is useful because
	 * constructors don't get an Event and
	 * (this instanceof ClientSideSpecificUtilities) crashes the server
	 * with ClassNotFoundException caused by NoClassDefFoundError
	 * 
	 * @return true if this is the client, false if this is the server
	 */
	public boolean isClient();
	
	/**
	 * Print a message to the owner. This is the singleplayer user on a client and the server console on the server.
	 * The text is printed unformatted to the server and formatted to the client.
	 * @param textComponent The text component to send
	 */
	public void addMessageToOwner(ITextComponent textComponent);

}
