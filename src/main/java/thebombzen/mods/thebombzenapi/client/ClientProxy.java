package thebombzen.mods.thebombzenapi.client;

import java.io.File;

import net.minecraft.client.Minecraft;
import thebombzen.mods.thebombzenapi.CommonProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This class provides client-side-specific functions.
 * 
 * @author thebombzen
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

	/**
	 * Returns your .minecraft directory.
	 */
	@Override
	public File getMinecraftDirectory() {
		return Minecraft.getMinecraft().mcDataDir;
	}

	@Override
	public boolean isClientProxy() {
		return true;
	}

}
