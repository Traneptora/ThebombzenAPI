package thebombzen.mods.thebombzenapi.client;

import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigScreen extends ThebombzenAPIConfigScreen {
	public ConfigScreen(GuiScreen parentScreen) {
		super(ThebombzenAPI.instance, parentScreen, ThebombzenAPI.instance.getConfiguration());
	}
}
