package thebombzen.mods.thebombzenapi;

import java.io.File;

import net.minecraft.server.MinecraftServer;

/**
 * This class provides server-side-specific functions.
 * 
 * @author thebombzen
 */
public class CommonProxy {

	/**
	 * Get the base minecraft folder.
	 * 
	 * @return
	 */
	public File getMinecraftDirectory() {
		return new File(MinecraftServer.getServer().getFolderName());
	}

	/**
	 * Returns whether this is the client proxy. This is useful because (this
	 * instanceof ClientProxy) crashes the server with ClassNotFoundException
	 * caused by NoClassDefFoundError
	 * 
	 * @return true if (this instanceof ClientProxy), false otherwise
	 */
	public boolean isClientProxy() {
		return false;
	}

}
