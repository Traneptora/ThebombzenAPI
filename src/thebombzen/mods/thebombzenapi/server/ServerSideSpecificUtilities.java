package thebombzen.mods.thebombzenapi.server;

import java.io.File;

import thebombzen.mods.thebombzenapi.SideSpecificUtlities;
import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This class provides server-side-specific functions.
 * 
 * @author thebombzen
 */
@SideOnly(Side.SERVER)
public class ServerSideSpecificUtilities implements SideSpecificUtlities {

	@Override
	public File getMinecraftDirectory() {
		File source = FMLCommonHandler.instance().findContainerFor(ThebombzenAPI.instance).getSource();
		return source.getParentFile().getParentFile();
	}

	@Override
	public boolean isClient() {
		return false;
	}

}
