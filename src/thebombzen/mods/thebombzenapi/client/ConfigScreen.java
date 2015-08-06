package thebombzen.mods.thebombzenapi.client;

import net.minecraft.client.gui.GuiScreen;
import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigScreen extends ThebombzenAPIConfigScreen {
	public ConfigScreen(GuiScreen parentScreen) {
		super(ThebombzenAPI.instance, parentScreen, ThebombzenAPI.instance.getConfiguration());
	}
}
