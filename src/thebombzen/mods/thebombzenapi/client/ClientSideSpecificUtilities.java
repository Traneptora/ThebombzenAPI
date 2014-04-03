package thebombzen.mods.thebombzenapi.client;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import thebombzen.mods.thebombzenapi.SideSpecificUtlities;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This class provides client-side-specific functions.
 * 
 * @author thebombzen
 */
@SideOnly(Side.CLIENT)
public class ClientSideSpecificUtilities implements SideSpecificUtlities {

	@Override
	public File getMinecraftDirectory() {
		return Minecraft.getMinecraft().mcDataDir;
	}

	@Override
	public boolean isClient() {
		return true;
	}

	@Override
	public void crash(String info, Throwable e) {
		Minecraft.getMinecraft().displayCrashReport(new CrashReport(info, e));
	}

}
