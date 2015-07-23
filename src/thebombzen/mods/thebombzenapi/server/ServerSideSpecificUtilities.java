package thebombzen.mods.thebombzenapi.server;

import java.io.File;

import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.util.IChatComponent;
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
	public void crash(String info, Throwable e) {
		FMLCommonHandler.instance().exitJava(1, false);
	}

	@Override
	public File getMinecraftDirectory() {
		File source = FMLCommonHandler.instance().findContainerFor(ThebombzenAPI.instance).getSource();
		return source.getParentFile().getParentFile();
	}

	@Override
	public boolean isClient() {
		return false;
	}
	
	

	@Override
	public void addMessageToOwner(IChatComponent chatComponent) {
		RConConsoleSource.getInstance().addChatMessage(chatComponent);
	}

}
