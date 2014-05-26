package thebombzen.mods.thebombzenapi.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import thebombzen.mods.thebombzenapi.ThebombzenAPIBaseMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This class represents a configuration for a mod.
 * 
 * @author thebombzen
 * @param <T>
 *            This must be a ThebombzenAPIConfigOption and an Enum.
 */
public abstract class ThebombzenAPIConfiguration {

	/**
	 * The mod this option is for.
	 */
	protected ThebombzenAPIBaseMod mod;

	/**
	 * Basic properties. Advanced config files should be mod-specific.
	 */
	protected Properties properties = new Properties();

	/**
	 * The file containing the properties, in property-file format.
	 */
	protected File propsFile;

	/**
	 * This is the Date Modified value of the properties file last time we
	 * checked. If the actual properties file differs in date modified, we
	 * reload the config from the disk, because this means the user edited the
	 * file while Minecraft was loaded.
	 */
	protected long lastCheckedConfigLastModified;

	/**
	 * Construct a configuration based on the provided mod and the provided
	 * Class.
	 * 
	 * @param baseMod
	 *            The mod this configuration is for.
	 * @param optionClass
	 *            The type object of the option class we're using. Necessary due
	 *            to type erasure.
	 */
	public ThebombzenAPIConfiguration(ThebombzenAPIBaseMod baseMod) {
		mod = baseMod;
		propsFile = new File(ThebombzenAPI.sideSpecificUtilities.getMinecraftDirectory().getPath() + File.separator + "config" + File.separator + mod.getLongName() + ".txt");
	}

	/**
	 * Returns an array of options permitted by this configuration.
	 */
	public abstract ConfigOption[] getAllOptions();

	/**
	 * The same as getProperty() but calls {Boolean.parseBoolean(String)} for
	 * convenience.
	 * 
	 * @param option
	 *            The config option we're trying to retrieve.
	 * @return A boolean for this BOOLEAN option.
	 */
	public boolean getBooleanProperty(ConfigOption option) {
		if (option.getOptionType() != ConfigOption.BOOLEAN){
			throw new UnsupportedOperationException("Can only work on a BOOLEAN!");
		}
		return ThebombzenAPI.parseBoolean(getStringProperty(option));
	}

	@SideOnly(Side.CLIENT)
	public int getKeyCodeProperty(ConfigOption option){
		if (option.getOptionType() != ConfigOption.KEY){
			throw new UnsupportedOperationException("Can only work on a KEY!");
		}
		return ThebombzenAPI.getExtendedKeyIndex(getStringProperty(option));
	}
	
	/**
	 * Returns the file storing this mod's basic properties.
	 */
	public File getPropertyFile() {
		return this.propsFile;
	}

	/**
	 * Get the value of a property that is either
	 * @param option
	 * @return
	 */
	@SideOnly(Side.CLIENT)
	public boolean getSingleMultiProperty(ConfigOption option){
		if (option.getOptionType() != ConfigOption.SINGLE_MULTI_BOOLEAN){
			throw new UnsupportedOperationException("Can only work on a SINGLE_MULTI_BOOLEAN!");
		}
		String value = getStringProperty(option);
		if (value.equalsIgnoreCase("always")){
			return true;
		} else if (value.equalsIgnoreCase("never")){
			return false;
		} else if (value.toLowerCase().contains("singleplayer")){
			return Minecraft.getMinecraft().isSingleplayer();
		} else if (value.toLowerCase().contains("multiplayer")){
			return !Minecraft.getMinecraft().isSingleplayer();
		} else {
			setProperty(option, option.getDefaultValue());
			return getSingleMultiProperty(option);
		}
	}

	/**
	 * Returns the String value of the property associated with this
	 * configuration.
	 * 
	 * @param option
	 *            The config option we're trying to retrieve.
	 * @return A String containing the property value (too bad it's low in
	 *         this recession), or an empty string if the property was not
	 *         found.
	 */
	public String getStringProperty(ConfigOption option) {
		String value = properties.getProperty(option.toString());
		return (value != null) ? value : "";
	}

	/**
	 * Initialize all options to their defaults (before loading from the disk).
	 */
	private void initializeDefaults() {
		for (ConfigOption option : getAllOptions()) {
			setPropertyWithoutSave(option, option.getDefaultValue());
		}
	}

	/**
	 * Loads the properties from the disk into memory.
	 * 
	 * @throws IOException
	 *             If an I/O error occurred when reading/writing to the disk.
	 */
	public void load() throws IOException {
		initializeDefaults();
		loadProperties();
		saveProperties();
	}
	
	/**
	 * Loads the properties file into the mod's configuration, sets the mod's
	 * default toggles from those options, and stores the last modified date of
	 * the properties file.
	 * 
	 * @throws IOException
	 *             if an I/O error occurred when disk reading.
	 */
	protected void loadProperties() throws IOException {
		if (!propsFile.exists()) {
			propsFile.createNewFile();
		}
		Reader reader = new BufferedReader(new FileReader(propsFile));
		properties.load(reader);
		reader.close();
		for (ConfigOption option : getAllOptions()) {
			if (option.getDefaultToggleIndex() >= 0) {
				mod.setToggleDefaultEnabled(option.getDefaultToggleIndex(),
						getBooleanProperty(option));
			}
			if (option.getOptionType() == ConfigOption.FINITE_STRING && !Arrays.asList(option.getFiniteStringOptions()).contains(getStringProperty(option))){
				setPropertyWithoutSave(option, option.getDefaultValue());
			}
		}
		lastCheckedConfigLastModified = getPropertyFile().lastModified();
		File oldPropsFile = new File(ThebombzenAPI.sideSpecificUtilities.getMinecraftDirectory().getPath() + File.separator + "config" + File.separator + mod.getLongName() + ".cfg");
		if (oldPropsFile.exists()){
			oldPropsFile.delete();
		}
		
	}

	/**
	 * Determines whether to refresh the properties file, based on date
	 * modified.
	 * 
	 * @return true if the properties were reloaded, false otherwise.
	 * @throws IOException
	 *             If an I/O error occurred.
	 */
	public boolean reloadPropertiesFromFileIfChanged() throws IOException {
		if (shouldRefreshConfig()) {
			loadProperties();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Save properties file to the disk, and store the last modified date.
	 */
	public void saveProperties() {
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new FileWriter(getPropertyFile())));
			properties.store(writer, mod.getLongName() + " basic properties");
			ConfigOption[] options = getAllOptions();
			StringBuilder builder = new StringBuilder();
			for (ConfigOption option : options) {
				builder.append("# ").append(option.toString())
						.append(ThebombzenAPI.NEWLINE);
				for (String info : option.getInfo()) {
					builder.append("#     ").append(info)
							.append(ThebombzenAPI.NEWLINE);
				}
			}
			writer.write(builder.toString());
			writer.flush();
			writer.close();
			lastCheckedConfigLastModified = getPropertyFile().lastModified();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set a config option, and save the properties file.
	 * 
	 * @param option
	 *            The config option to set
	 * @param value
	 *            The value of the config option we're setting
	 */
	public void setProperty(ConfigOption option, String value) {
		setPropertyWithoutSave(option, value);
		saveProperties();
	}

	/**
	 * Set a config option, but don't save the properties file. Used when loaded
	 * properties from the disk.
	 * 
	 * @param option
	 *            The config option to set
	 * @param value
	 *            The value of the config option we're setting
	 */
	protected void setPropertyWithoutSave(ConfigOption option,
			String value) {
		//System.out.println(option + value);
		properties.setProperty(option.toString(), value);
	}

	/**
	 * Determines whether the configuration should be reloaded from the disk.
	 * This compares the stored date modified to the file's date modified.
	 * 
	 * @return true if yes, false if no.
	 */
	protected boolean shouldRefreshConfig() {
		long configLastModified = getPropertyFile().lastModified();
		if (lastCheckedConfigLastModified != configLastModified) {
			return true;
		} else {
			return false;
		}
	}

}
