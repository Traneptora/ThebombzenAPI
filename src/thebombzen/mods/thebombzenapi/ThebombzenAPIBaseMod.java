package thebombzen.mods.thebombzenapi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thebombzen.mods.thebombzenapi.configuration.ThebombzenAPIConfiguration;

/**
 * This class is the superclass of Thebombzen's mods. Extend this to get
 * ThebombzenAPI functionality. I'm using inheritance because it's practical,
 * and because FML doesn't require inheriting any particular class.
 * 
 * This implements Comparable so it can be sorted in alphabetical order by a SortedSet.
 * 
 * You still need a Mod annotation on your mod. This will not load your mod for you.
 * @author thebombzen
 */
public abstract class ThebombzenAPIBaseMod implements Comparable<ThebombzenAPIBaseMod> {

	/**
	 * Array of keycodes that toggle boolean values.
	 */
	@SideOnly(Side.CLIENT)
	protected int[] toggleKeyCodes;

	/**
	 * Array of boolean values to be toggled by the toggle keys.
	 */
	@SideOnly(Side.CLIENT)
	protected boolean[] toggles;

	/**
	 * Default values of the boolean toggles.
	 */
	@SideOnly(Side.CLIENT)
	protected boolean[] defaultToggles;

	/**
	 * Location of the mod data.
	 */
	protected File modFolder;

	/**
	 * Debug messages are logged here.
	 */
	protected PrintWriter debugLogger = null;

	/**
	 * A StringBuilder which builds a debug message.
	 */
	protected StringBuilder debugBuilder = null;

	/**
	 * This contains the previous contents of the debugging StringBuilder, used to avoid repeats.
	 */
	protected String prevDebugString = "";
	
	/**
	 * This contains the previous line of the debugging output, used to avoid repeats.
	 */
	protected String prevImmediateDebugString = "";

	/**
	 * Debug output goes to this file.
	 */
	protected File debugFile;
	
	/**
	 * If this flag is set to true, then we need to reload the memory from the correct memory file.
	 */
	protected boolean dirtyMemoryFlag = true;
	
	/**
	 * Mark the memory for reloading from the memory file. This is useful if the world has changed or just loaded.
	 */
	public void markMemoryDirty(){
		dirtyMemoryFlag = true;
	}
	
	/**
	 * Returns true if the memory should be reloaded from the memory file
	 * and false if the memory does not need to be reloaded from the memory file.
	 */
	public boolean isMemoryDirty(){
		return dirtyMemoryFlag;
	}
	
	@Override
	public int compareTo(ThebombzenAPIBaseMod mod){
		if (this == mod){
			return 0;
		}
		if (this instanceof ThebombzenAPI){
			return -1;
		}
		if (mod instanceof ThebombzenAPI){
			return 1;
		}
		return this.getLongName().compareTo(mod.getLongName());
	}

	/**
	 * This finalizer closes the debug logger upon a crash or other closure.
	 */
	@Override
	protected void finalize() throws Throwable {
		debugLogger.close();
	}

	/**
	 * Writes the string to the debug logger, without checking if debug is
	 * enabled. Used for runtime errors.
	 * 
	 * @param string
	 *            The {String} to write.
	 */
	public void forceDebug(String string) {
		forceDebug("%s", string);
	}

	/**
	 * Writes the debug info to the debug logger, without checking if debug is
	 * enabled. Uses {String.format} syntax. Used for runtime errors.
	 * 
	 * @param format
	 *            The string format
	 * @param args
	 *            The format arguments.
	 */
	public void forceDebug(String format, Object... args) {
		String s = String.format(format, args);
		System.err.println(s);
		if (s.equals(prevImmediateDebugString)){
			return;
		}
		if (s.matches("^=+START=+$")){
			debugBuilder = new StringBuilder(s).append(ThebombzenAPI.NEWLINE);
			return;
		} else if (s.matches("^=+END=+$")){
			debugBuilder.append(s).append(ThebombzenAPI.NEWLINE);
			s = debugBuilder.toString();
			debugBuilder = null;
			if (s.equals(prevDebugString)){
				return;
			} else {
				prevDebugString = s;
			}
		} else if (debugBuilder != null){
			debugBuilder.append(s).append(ThebombzenAPI.NEWLINE);
			return;
		}
		if (s.equals(prevImmediateDebugString)){
			return;
		}
		prevImmediateDebugString = s;
		if (debugLogger != null){
			debugLogger.println(s);
			debugLogger.flush();
		}
	}

	/**
	 * Debugs an exception to the debug logger (and to System.err)
	 * without checking whether or not debug is enabled. Used for runtime errors.
	 * @param exception
	 */
	public void forceDebugException(Throwable exception){
		exception.printStackTrace();
		if (debugLogger != null) {
			exception.printStackTrace(debugLogger);
			debugLogger.flush();
		}
	}

	/**
	 * Gets an NBTTagCompound containing the current toggle data.
	 */
	@SideOnly(Side.CLIENT)
	public NBTTagCompound getCompoundFromCurrentData() {
		NBTTagCompound settings = new NBTTagCompound();
		byte[] togglesByte = new byte[getNumToggleKeys()];
		for (int i = 0; i < getNumToggleKeys(); i++) {
			togglesByte[i] = isToggleEnabled(i) ? (byte) 1 : (byte) 0;
		}
		settings.setByteArray("toggles", togglesByte);

		NBTTagCompound data = new NBTTagCompound();
		data.setTag("Settings", settings);
		return data;
	}

	/**
	 * Gets an NBTTagCompound containing the default toggle data.
	 */
	@SideOnly(Side.CLIENT)
	public NBTTagCompound getCompoundFromDefaultData() {
		NBTTagCompound settings = new NBTTagCompound();
		byte[] togglesByte = new byte[getNumToggleKeys()];
		for (int i = 0; i < getNumToggleKeys(); i++) {
			togglesByte[i] = isToggleDefaultEnabled(i) ? (byte) 1 : (byte) 0;
		}
		settings.setByteArray("toggles", togglesByte);

		NBTTagCompound data = new NBTTagCompound();
		data.setTag("Settings", settings);
		return data;
	}

	/**
	 * Gets the configuration for this mod.
	 * Mods need to create their own implementation of ThebombzenAPIConfiguration.
	 */
	public abstract <T extends ThebombzenAPIConfiguration> T getConfiguration();

	/**
	 * This returns the location of the data compound memory file.
	 */
	@SideOnly(Side.CLIENT)
	public File getCorrectMemoryFile() {
		if (Minecraft.getMinecraft().theWorld == null) {
			return null;
		}
		if (Minecraft.getMinecraft().isSingleplayer()) {
			if (ThebombzenAPI.hasServerWorldLoaded()){
				return new File(((SaveHandler) DimensionManager.getWorld(Minecraft.getMinecraft().thePlayer.dimension)
						.getSaveHandler()).getWorldDirectory(),
						getLongName().toUpperCase() + "_MEMORY.dat");
			} else {
				return null;
			}
		} else {
			return new File(getModFolder(),
					getLongName().toUpperCase()
							+ "_MEMORY_"
							+ Minecraft.getMinecraft().getCurrentServerData().serverIP
									.toLowerCase() + ".dat");
		}
	}

	/**
	 * This is a String containing the URL of the download location.
	 * This is not a direct download location but rather a URL to display to the user
	 * so he or she can go and download an update manually. 
	 */
	public abstract String getDownloadLocationURLString();

	/**
	 * Fetches the latest version of the mod from a file on the interwebs,
	 * in order to be compared to the current version.
	 * @return the version String of the latest version
	 */
	public String getLatestVersion() {
		String latestVersion = null;
		try {
			URL versionURL = getVersionFileURL();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					versionURL.openStream()));
			latestVersion = br.readLine();
			br.close();
		} catch (Exception e) {
			latestVersion = getLongVersionString();
		}
		return latestVersion;
	}

	/**
	 * Gets the full name of the mod.
	 */
	public abstract String getLongName();

	/**
	 * Gets the long version String of the mod.
	 * This is usually of the form "Mod, version #, Minecraft #"
	 */
	public abstract String getLongVersionString();

	/**
	 * Gets the data folder for the mod.
	 */
	public File getModFolder() {
		return modFolder;
	}

	/**
	 * Gets the number of key-activated toggles.
	 */
	@SideOnly(Side.CLIENT)
	public abstract int getNumToggleKeys();

	/**
	 * Gets the acronym for the mod name, usually two to six characters in all caps,
	 * like AS, WAGT, or TBZAPI.
	 */
	public abstract String getShortName();

	/**
	 * Returns the extended keycode for the toggle at the specified index.
	 * @throws UnsupportedOperationException if there aren't any toggle keys
	 * @throws IndexOutOfBoundsException if the index isn't between 0 and numToggles, i/e.
	 */
	@SideOnly(Side.CLIENT)
	public int getToggleKeyCode(int index) {
		if (getNumToggleKeys() <= 0) {
			throw new UnsupportedOperationException();
		}
		if (index < 0 || index >= getNumToggleKeys()) {
			throw new IndexOutOfBoundsException();
		}
		return toggleKeyCodes[index];
	}

	/**
	 * Gets the message printed to the user when a toggle is changed by him or her. e.g.
	 * "Such and such toggle is enabled."
	 * 
	 * @param index
	 *            index of the toggle
	 * @param enabled
	 *            true if the toggle was just enabled, false if it was just disabled.
	 */
	@SideOnly(Side.CLIENT)
	protected abstract String getToggleMessageString(int index, boolean enabled);

	/**
	 * Returns a URL pointing toward the current version file. This is the same URL
	 * given by getVersionFileURLString().
	 */
	public URL getVersionFileURL() throws MalformedURLException {
		return new URL(getVersionFileURLString());
	}
	
	/**
	 * Returns a URL pointing toward a version file, which is a file
	 * that contains a version String as its first line. The version file
	 * this retrieves should have the latest version as its first line.
	 */
	protected abstract String getVersionFileURLString();

	/**
	 * This is the equivalent of preInit, but handled by ThebombzenAPI.
	 * During the init1 phase, the configuration is not loaded from the disk.
	 * Override it to make it do something.
	 * @param event the event passed to ThebombzenAPI by FML.
	 */
	public void init1(FMLPreInitializationEvent event) {
		
	}

	/**
	 * This is the equivalent of load, but handled by ThebombzenAPI.
	 * The configuration is loaded between init1 and init2 and it
	 * should be loaded from the disk here.
	 * Override it to make it do something.
	 * @param event the event passed to ThebombzenAPI by FML.
	 */
	public void init2(FMLInitializationEvent event) {
		
	}

	/**
	 * This is the equivalent of postInit, but handled by ThebombzenAPI.
	 * Override it to make it do something.
	 * @param event the event passed to ThebombzenAPI by FML.
	 */
	public void init3(FMLPostInitializationEvent event) {

	}

	/**
	 * This is the initialization routine.
	 * This is code that would be contained in the constructor but FML whines that
	 * no code should be executed during mod construction.
	 * ThebombzenAPI handles it all before init1.
	 */
	void initialize(){
		
		if (FMLCommonHandler.instance().getSide().isClient()) {
			toggleKeyCodes = new int[getNumToggleKeys()];
			toggles = new boolean[getNumToggleKeys()];
			defaultToggles = new boolean[getNumToggleKeys()];
		}

		File mineFile = ThebombzenAPI.sideSpecificUtilities.getMinecraftDirectory();
		File modsFolder = new File(mineFile, "mods");
		modFolder = new File(modsFolder, getLongName());
		modFolder.mkdirs();

		debugFile = new File(modFolder, "DEBUG.txt");
		try {
			debugLogger = new PrintWriter(new FileWriter(debugFile));
		} catch (IOException ioe) {
			debugLogger = null;
			throwException("Unable to open debug output file.", ioe, false);
		}
	}

	/**
	 * Determines if the toggle is "default enabled." A toggle is "default enabled" if
	 * it will default to "enabled" on new worlds and unvisited servers.
	 * 
	 * @param index
	 *            The index of the toggle
	 * @return true if it is "default enabled," false otherwise.
	 * @throws IndexOutOfBoundsException if index isn't between 0 and numToggles, i/e.
	 * @throws UnsupportedOperationException if there are no toggles
	 */
	@SideOnly(Side.CLIENT)
	public boolean isToggleDefaultEnabled(int index) {
		if (getNumToggleKeys() <= 0) {
			throw new UnsupportedOperationException();
		}
		if (index < 0 || index >= getNumToggleKeys()) {
			throw new IndexOutOfBoundsException();
		}
		return defaultToggles[index];
	}

	/**
	 * Determines if the toggle is currently enabled.
	 * 
	 * @param index
	 *            The index of the toggle.
	 * @return true if it is currently enabled, false if it is currently disabled.
	 * @throws IndexOutOfBoundsException if index isn't between 0 and numToggles, i/e.
	 * @throws UnsupportedOperationException if there are no toggles
	 */
	@SideOnly(Side.CLIENT)
	public boolean isToggleEnabled(int index) {
		if (getNumToggleKeys() <= 0) {
			throw new UnsupportedOperationException();
		}
		if (index < 0 || index >= getNumToggleKeys()) {
			throw new IndexOutOfBoundsException();
		}
		return toggles[index];
	}
	
	
	/**
	 * Load the saved data (currently only toggles) from the correct memory file.
	 */
	@SideOnly(Side.CLIENT)
	public void readFromCorrectMemoryFile() {
		File file = getCorrectMemoryFile();
		if (file != null) {
			NBTTagCompound data = readFromMemoryFile(file);
			saveCompoundToCurrentData(data);
			dirtyMemoryFlag = false;
		}
	}
	
	/**
	 * Read and return memory data from the specified file.
	 * Currently the only memory data is toggles.
	 * @param file
	 *            The file to read from
	 * @return The NBTTagCompound containing the data that was read.
	 */
	@SideOnly(Side.CLIENT)
	public NBTTagCompound readFromMemoryFile(File file) {
		try {
			if (!file.isFile()) {
				writeToMemoryFile(file, getCompoundFromDefaultData());
				return getCompoundFromDefaultData();
			}
			InputStream in = new FileInputStream(file);
			NBTTagCompound data = null;
			try {
				data = CompressedStreamTools.readCompressed(in);
			} catch (IOException ioe) {
				writeToMemoryFile(file, getCompoundFromCurrentData());
			}
			if (data != null) {
				return data;
			}
		} catch (IOException ioe) {
			throwException("Couldn't read from memory file.", ioe, false);
		}
		return getCompoundFromCurrentData();
	}

	/**
	 * This method takes the data from the NBTTagCompound and changes the mod
	 * state to match the data in the compound. It loads the data from the compound
	 * into the mod state.
	 * 
	 * @param data
	 *            The data compound to load from
	 */
	@SideOnly(Side.CLIENT)
	public void saveCompoundToCurrentData(NBTTagCompound data) {
		NBTTagCompound settings = data.getCompoundTag("Settings");
		if (settings == null) {
			return;
		}
		byte[] togglesByte = settings.getByteArray("toggles");
		if (togglesByte.length == 0) {
			for (int i = 0; i < getNumToggleKeys(); i++) {
				setToggleEnabled(i, isToggleDefaultEnabled(i), false);
			}
		} else {
			for (int i = 0; i < getNumToggleKeys(); i++) {
				setToggleEnabled(i, togglesByte[i] != 0, false);
			}
		}
	}

	/**
	 * Sets the "default enabled" status of a toggle. A toggle is "default enabled" if
	 * it will default to "enabled" on new worlds and unvisited servers.
	 * 
	 * @param index
	 *            The index of the toggle
	 * @param enabled true to set it to "default enabled", false to turn off "default enabled"
	 * @throws IndexOutOfBoundsException if index isn't between 0 and numToggles, i/e.
	 * @throws UnsupportedOperationException if there are no toggles
	 */
	@SideOnly(Side.CLIENT)
	public void setToggleDefaultEnabled(int index, boolean enabled) {
		if (getNumToggleKeys() <= 0) {
			throw new UnsupportedOperationException();
		}
		if (index < 0 || index >= getNumToggleKeys()) {
			throw new IndexOutOfBoundsException();
		}
		defaultToggles[index] = enabled;
	}

	/**
	 * Sets the status of the toggle.
	 * 
	 * @param index
	 *            The index of the toggle
	 * @param enabled
	 *            true to enable, false to disable
	 * @param keyPress
	 *            true if this change was initiated by an extended keypress, or false if
	 *            not. If true, the toggle message will print to the user's GUI.
	 */
	@SideOnly(Side.CLIENT)
	public void setToggleEnabled(int index, boolean enabled, boolean keyPress) {
		if (getNumToggleKeys() <= 0) {
			throw new UnsupportedOperationException();
		}
		if (index < 0 || index >= getNumToggleKeys()) {
			throw new IndexOutOfBoundsException();
		}
		toggles[index] = enabled;
		if (keyPress) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(getToggleMessageString(index, enabled)));
		}

	}

	/**
	 * Sets the extended keycode used to change the toggle. An extended keycode is either a mouse button or a keyboard key.
	 * 
	 * @param index
	 *            The index of the toggle
	 * @param keyCode
	 *            The extended keycode used to change the toggle at the specified index.
	 */
	@SideOnly(Side.CLIENT)
	public void setToggleKeyCode(int index, int keyCode) {
		if (getNumToggleKeys() <= 0) {
			throw new UnsupportedOperationException();
		}
		if (index < 0 || index >= getNumToggleKeys()) {
			throw new IndexOutOfBoundsException();
		}
		toggleKeyCodes[index] = keyCode;
	}
	
	/**
	 * Throws an exception in a controlled environment, and logs it to the debug
	 * logger.
	 * 
	 * @param info
	 *            String indicating a short amount of info about the Exception.
	 * @param exception
	 *            The Throwable to throw.
	 * @param fatal
	 *            if true, crash the client. If false, only log the exception.
	 */
	public void throwException(String info, Throwable exception, boolean fatal) {
		forceDebug(info);
		forceDebugException(exception);
		if (fatal) {
			ThebombzenAPI.sideSpecificUtilities.crash(info, exception);
		}
	}

	@Override
	public String toString(){
		return getLongName();
	}
	
	/**
	 * Writes the current data to the correct data memory file.
	 */
	@SideOnly(Side.CLIENT)
	public void writeToCorrectMemoryFile() {
		File file = getCorrectMemoryFile();
		if (file != null) {
			NBTTagCompound data = getCompoundFromCurrentData();
			writeToMemoryFile(file, data);
		}
	}
	
	/**
	 * Writes the given memory data compound to the given file.
	 * 
	 * @param file
	 *            The File to write to.
	 * @param config
	 *            The NBTTagCompound to write.
	 */
	@SideOnly(Side.CLIENT)
	public void writeToMemoryFile(File file, NBTTagCompound config) {
		try {
			file.delete();
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			CompressedStreamTools.writeCompressed(config, fos);
		} catch (IOException ioe) {
			throwException("Couldn't write to memory file.", ioe, false);
		}
	}
	
}
