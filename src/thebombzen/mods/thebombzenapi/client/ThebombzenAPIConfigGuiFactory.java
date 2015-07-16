package thebombzen.mods.thebombzenapi.client;

import java.util.HashSet;
import java.util.Set;

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
	public RuntimeOptionGuiHandler getHandlerFor(
			RuntimeOptionCategoryElement element) {
		return null;
	}

	@Override
	public void initialize(Minecraft minecraftInstance) {
		
	}

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return guiScreenClass;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return new HashSet<RuntimeOptionCategoryElement>();
	}

}
