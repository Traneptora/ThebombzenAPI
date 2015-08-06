package thebombzen.mods.thebombzenapi.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigGuiFactory extends ThebombzenAPIConfigGuiFactory {
	public ConfigGuiFactory(){
		super(ConfigScreen.class);
	}
}
