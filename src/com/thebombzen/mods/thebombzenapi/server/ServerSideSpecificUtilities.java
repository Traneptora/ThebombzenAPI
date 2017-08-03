package com.thebombzen.mods.thebombzenapi.server;

import java.io.File;

import org.apache.logging.log4j.LogManager;

import com.thebombzen.mods.thebombzenapi.SideSpecificUtlities;
import com.thebombzen.mods.thebombzenapi.ThebombzenAPI;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	public void addMessageToOwner(ITextComponent textComponent) {
		LogManager.getLogger(DedicatedServer.class).info(textComponent.getUnformattedText());
	}

}
