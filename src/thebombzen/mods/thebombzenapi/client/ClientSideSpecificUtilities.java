package thebombzen.mods.thebombzenapi.client;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.IChatComponent;
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
	public void crash(String info, Throwable e) {
		Minecraft.getMinecraft().displayCrashReport(new CrashReport(info, e));
	}

	@Override
	public File getMinecraftDirectory() {
		return Minecraft.getMinecraft().mcDataDir;
	}

	@Override
	public boolean isClient() {
		return true;
	}

	@Override
	public void addMessageToOwner(IChatComponent chatComponent) {
		Minecraft.getMinecraft().thePlayer.addChatMessage(chatComponent);
	}

}
