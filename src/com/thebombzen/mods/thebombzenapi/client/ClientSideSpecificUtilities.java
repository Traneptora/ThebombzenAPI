package com.thebombzen.mods.thebombzenapi.client;

import java.io.File;

import com.thebombzen.mods.thebombzenapi.SideSpecificUtlities;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	public void addMessageToOwner(ITextComponent textComponent) {
		Minecraft.getMinecraft().player.sendMessage(textComponent);
	}

}
