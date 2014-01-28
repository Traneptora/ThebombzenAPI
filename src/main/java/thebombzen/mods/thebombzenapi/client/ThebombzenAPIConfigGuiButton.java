package thebombzen.mods.thebombzenapi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This is a {GuiButton} that provides a tooltip for a config option upon mouse
 * over.
 * 
 * @author thebombzen
 */
@SideOnly(Side.CLIENT)
public class ThebombzenAPIConfigGuiButton extends GuiButton {

	/**
	 * This is the parent screen. Used to determine tooltip placement.
	 */
	private GuiScreen parentScreen;

	/**
	 * These are the tooltip lines to display.
	 */
	private String[] toolTips;

	/**
	 * This is the stored value of the maximum line length.
	 */
	private int toolTipWidth = -1;

	/**
	 * Construct a button, and give the size.
	 * 
	 * @param parentScreen
	 *            The screen containing this button.
	 * @param id
	 *            The button ID
	 * @param x
	 *            The x-coordinate, upper-left is 0
	 * @param y
	 *            The y-coordinate, upper-left is 0
	 * @param width
	 *            The width of the button
	 * @param height
	 *            The height of the button
	 * @param displayString
	 *            The string to write ON the button.
	 * @param tooltips
	 *            A list of lines to display upon mouseover in a tooltip.
	 */
	public ThebombzenAPIConfigGuiButton(GuiScreen parentScreen, int id, int x,
			int y, int width, int height, String displayString,
			String... tooltips) {
		super(id, x, y, width, height, displayString);
		this.toolTips = tooltips;
		this.parentScreen = parentScreen;
	}

	/**
	 * Construct a button, and use the default size.
	 * 
	 * @param parentScreen
	 *            The screen containing this button.
	 * @param id
	 *            The button ID
	 * @param x
	 *            The x-coordinate, upper-left is 0
	 * @param y
	 *            The y-coordinate, upper-left is 0
	 * @param displayString
	 *            The string to write ON the button.
	 * @param tooltips
	 *            A list of lines to display upon mouseover in a tooltip.
	 */
	public ThebombzenAPIConfigGuiButton(GuiScreen parentScreen, int id, int x,
			int y, String displayString, String... tooltips) {
		super(id, x, y, displayString);
		this.toolTips = tooltips;
		this.parentScreen = parentScreen;
	}

	/**
	 * Formerly "drawTooltip" This draws the tooltip over the button, and calls
	 * the superclass draw function.
	 */
	@Override
	public void func_146112_a(Minecraft minecraft, int i, int j) {
		super.func_146112_a(minecraft, i, j);

		// field_146120_f == width
		// field_146121_g == height
		// field_146128_h == xPosition
		// field_146129_i == yPosition

		if (i >= field_146128_h && j >= field_146129_i
				&& i < (field_146128_h + field_146120_f)
				&& j < (field_146129_i + field_146121_g)) {

			FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

			int lineHeight = fontRenderer.FONT_HEIGHT + 3;

			int x = i + 12;
			int y = j - lineHeight * toolTips.length - 12;
			if (toolTipWidth == -1) {
				for (String line : toolTips) {
					toolTipWidth = Math.max(toolTipWidth,
							fontRenderer.getStringWidth(line));
				}
			}

			// field_146294_l == width
			if (x + toolTipWidth >= parentScreen.field_146294_l) {
				x -= toolTipWidth + 24;
			}

			if (y < 3) {
				y += lineHeight * toolTips.length + 24;
			}

			drawGradientRect(x - 3, y - 3, x + toolTipWidth + 3, y + lineHeight
					* toolTips.length, 0xc0000000, 0xc0000000);

			for (int index = 0; index < toolTips.length; index++) {
				fontRenderer.drawStringWithShadow(toolTips[index], x, y + index
						* lineHeight, -1);
			}
		}
	}

}
