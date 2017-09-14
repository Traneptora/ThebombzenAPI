package com.thebombzen.mods.thebombzenapi.client;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;

import com.thebombzen.mods.thebombzenapi.ThebombzenAPI;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ThebombzenAPIConfigGuiFactory implements IModGuiFactory {
	
	private Class<? extends GuiScreen> guiScreenClass;
	
	public ThebombzenAPIConfigGuiFactory(Class<? extends GuiScreen> clazz){
		this.guiScreenClass = clazz;
	}
	
	@Override
	public void initialize(Minecraft minecraftInstance) {
		
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return Collections.emptySet();
	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		try {
			return guiScreenClass.getConstructor(GuiScreen.class).newInstance(parentScreen);
		} catch (InvocationTargetException ex) {
			// In theory, the constructor should only throw unchecked exceptions
			throw new RuntimeException(ex);
		} catch (NoSuchMethodException ex) {
			// Stupid modder, make your constructor visible :P
			ThebombzenAPI.instance.throwException("Cannot use unknown constructor", ex, true);
		} catch (IllegalAccessException ex) {
			// Stupid modder, make your constructor visible :P
			ThebombzenAPI.instance.throwException("Cannot use unknown constructor", ex, true);
		} catch (InstantiationException ex) {
			// Stupid modder, do not use an abstract class
			ThebombzenAPI.instance.throwException("Cannot use abstract constructor", ex, true);
		}
		// Execution should not reach here
		return null;
	}

}
