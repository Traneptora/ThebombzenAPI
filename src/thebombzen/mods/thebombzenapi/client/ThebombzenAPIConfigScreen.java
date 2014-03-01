package thebombzen.mods.thebombzenapi.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import thebombzen.mods.thebombzenapi.ThebombzenAPIBaseMod;
import thebombzen.mods.thebombzenapi.ThebombzenAPIConfigOption;
import thebombzen.mods.thebombzenapi.ThebombzenAPIConfiguration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This is a config screen for a mod, based on a configuration.
 * 
 * @author thebombzen
 */

@SideOnly(Side.CLIENT)
public abstract class ThebombzenAPIConfigScreen extends GuiScreen {

	/**
	 * The configuration associated with this screen.
	 */
	protected ThebombzenAPIConfiguration<?> config;

	/**
	 * The button currently being used to set keystrokes.
	 */
	protected GuiButton currentKeyButton = null;

	/**
	 * The mod associated with this config screen.
	 */
	protected ThebombzenAPIBaseMod mod;

	/**
	 * The parent screen of this screen.
	 */
	protected GuiScreen parentScreen;

	/**
	 * The title of this screen.
	 */
	protected final String title;

	/**
	 * tooltipButtons : ThebombzenAPIConfigGuiButton ->
	 * ThebombzenAPIConfigOption provides a mapping to retrieve the option from
	 * the button.
	 */
	protected Map<ThebombzenAPIConfigGuiButton, ThebombzenAPIConfigOption> tooltipButtons = new HashMap<ThebombzenAPIConfigGuiButton, ThebombzenAPIConfigOption>();

	/**
	 * Construct a config screen.
	 * 
	 * @param mod
	 *            The mod for this config screen.
	 * @param parentScreen
	 *            The parent screen for this config screen.
	 * @param config
	 *            The configuration associated with this config screen.
	 */
	protected ThebombzenAPIConfigScreen(ThebombzenAPIBaseMod mod,
			GuiScreen parentScreen, ThebombzenAPIConfiguration<?> config) {
		this.mod = mod;
		title = mod.getLongName() + " Options";
		this.config = config;
		this.parentScreen = parentScreen;
	}

	/**
	 * Perform an action upon mouse click.
	 * Formerly actionPerformed
	 * @param button
	 *            the GuiButton that was pressed.
	 */
	@Override
	protected void actionPerformed(GuiButton button) {
		int id = button.id;
		if (id == 4912) {
			// field_146297_k == mc
			// func_147108_a == displayGuiScreen
			mc.displayGuiScreen(this.parentScreen);
			return;
		} else if (id >= 4913) {
			ThebombzenAPIConfigOption option = tooltipButtons.get(button);
			if (option.getOptionType() == ThebombzenAPIConfigOption.BOOLEAN) {
				boolean newProp = !config.getPropertyBoolean(option);
				config.setProperty(option, Boolean.toString(newProp));
				// field_146128_j == displayString
				button.displayString = getDisplayGuiString(option);
			} else if (option.getOptionType() == ThebombzenAPIConfigOption.FINITE_STRING) {
				String[] strings = option.getFiniteStringOptions();
				int index = Arrays.asList(strings).indexOf(
						config.getProperty(option));
				index = (index + 1) % strings.length;
				config.setProperty(option, strings[index]);
				button.displayString = getDisplayGuiString(option);
			} else if (option.getOptionType() == ThebombzenAPIConfigOption.KEY) {
				if (button != currentKeyButton) {
					if (currentKeyButton != null) {
						currentKeyButton.displayString = getDisplayGuiString(tooltipButtons
								.get(currentKeyButton));
					}
					button.displayString = option.getShortInfo() + ": > ??? <";
					currentKeyButton = button;
				}
			}
		}
	}

	/**
	 * Draw the screen like the options screen, and the applicable tooltips.
	 */
	@Override
	public void drawScreen(int i, int j, float f) {
		// func_146276_q_ == drawDefaultBackground()
		this.drawDefaultBackground();
		// field_146289_q == fontRenderer
		// field_146294_l == width
		this.drawCenteredString(fontRendererObj, title, width / 2, 10,
				16777215);
		super.drawScreen(i, j, f);
		// field_146297_k == mc
		for (ThebombzenAPIConfigGuiButton button : tooltipButtons.keySet()) {
			button.drawTooltip(mc, i, j);
		}
	}

	/**
	 * Gets the display string for a given config option.
	 * 
	 * @param option
	 *            the config option
	 * @return The display string in (key: value) format.
	 */
	protected String getDisplayGuiString(ThebombzenAPIConfigOption option) {
		if (option.getOptionType() == ThebombzenAPIConfigOption.BOOLEAN) {
			return option.getShortInfo() + ": "
					+ (config.getPropertyBoolean(option) ? "ON" : "OFF");
		} else {
			return option.getShortInfo() + ": " + config.getProperty(option);
		}
	}

	/**
	 * Initialize this Gui.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		ThebombzenAPIConfigOption[] options = config.getAllOptions();
		int i = 0;
		for (ThebombzenAPIConfigOption option : options) {
			if (option.getOptionType() == ThebombzenAPIConfigOption.ARBITRARY_STRING) {
				continue;
			}
			// field_146294_l == width
			// field_146295_m == height
			ThebombzenAPIConfigGuiButton button = new ThebombzenAPIConfigGuiButton(
					this, 4913 + i, width / 2 - 206 + (i & 1) * 207,
					height / 6 + 23 * (i >> 1) - 18, 205, 20,
					getDisplayGuiString(option), option.getInfo());
			i++;
			// field_146292_n == buttonList
			buttonList.add(button);
			tooltipButtons.put(button, option);
		}
		buttonList.add(new GuiButton(4912, width / 2 - 100,
				height / 6 + 168, 200, 20, StatCollector
						.translateToLocal("gui.done")));
	}

	/**
	 * Deal with KEY options when a key is typed.
	 */
	@Override
	public void keyTyped(char keyChar, int keyCode) {
		super.keyTyped(keyChar, keyCode);
		if (keyCode != 1 && currentKeyButton != null) {
			ThebombzenAPIConfigOption option = tooltipButtons
					.get(currentKeyButton);
			config.setProperty(option, Keyboard.getKeyName(keyCode));
			// field_146126_j == displayString
			currentKeyButton.displayString = getDisplayGuiString(option);
			currentKeyButton = null;
		}
	}

}
