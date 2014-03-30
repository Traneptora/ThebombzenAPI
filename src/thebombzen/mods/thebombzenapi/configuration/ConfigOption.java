package thebombzen.mods.thebombzenapi.configuration;

import org.lwjgl.input.Keyboard;

/**
 * This interface provides a general rule for config options. Note that this
 * interface represents a key and not a value. Values are {String} values.
 * 
 * @author thebombzen
 */

public class ConfigOption {

	/**
	 * This option is a boolean option. True or false.
	 */
	public static final int BOOLEAN = 0;

	/**
	 * This option is an OpenGL (lwjgl) keyCode. Used to set keys.
	 */
	public static final int KEY = 1;

	/**
	 * This option is a member of a finite set of strings.
	 */
	public static final int FINITE_STRING = 2;

	/**
	 * This option can be any String.
	 */
	public static final int ARBITRARY_STRING = 3;
	
	/**
	 * This option is either "Never", "Always", "Singleplayer Only", or "Multiplayer Only"
	 */
	public static final int SINGLE_MULTI_BOOLEAN = 4;
	

	protected int type;
	protected Object defaultValue;
	protected String shortInfo;
	protected String[] longInfo;
	protected int defaultToggleIndex;
	protected String[] finiteStringOptions;
	protected String name;
	
	public ConfigOption(boolean defaultValue, String name, String shortInfo, String... longInfo){
		this.type = BOOLEAN;
		this.defaultValue = defaultValue;
		this.shortInfo = shortInfo;
		this.longInfo = longInfo;
		this.defaultToggleIndex = -1;
		this.name = name;
	}
	
	public ConfigOption(int toggleIndex, boolean defaultValue, String name, String shortInfo, String... longInfo){
		this.type = BOOLEAN;
		this.defaultValue = defaultValue;
		this.shortInfo = shortInfo;
		this.longInfo = longInfo;
		this.defaultToggleIndex = toggleIndex;
		this.name = name;
	}
	
	public ConfigOption(int keyCode, String name, String shortInfo, String... longInfo){
		this.type = KEY;
		this.defaultValue = Keyboard.getKeyName(keyCode);
		this.shortInfo = shortInfo;
		this.longInfo = longInfo;
		this.defaultToggleIndex = -1;
		this.name = name;
	}
	
	public ConfigOption(SingleMultiBoolean defaultValue, String name, String shortInfo, String... longInfo){
		this.type = SINGLE_MULTI_BOOLEAN;
		this.defaultValue = defaultValue;
		this.shortInfo = shortInfo;
		this.longInfo = longInfo;
		this.defaultToggleIndex = -1;
		this.name = name;
	}
	
	public ConfigOption(String defaultValue, String name, String shortInfo, String... longInfo){
		this.type = ARBITRARY_STRING;
		this.defaultValue = defaultValue;
		this.shortInfo = shortInfo;
		this.longInfo = longInfo;
		this.defaultToggleIndex = -1;
		this.name = name;
	}
	
	public ConfigOption(String defaultValue, String[] finiteStringOptions, String name, String shortInfo, String... longInfo){
		this.type = FINITE_STRING;
		this.defaultValue = defaultValue;
		this.shortInfo = shortInfo;
		this.longInfo = longInfo;
		this.defaultToggleIndex = -1;
		this.finiteStringOptions = finiteStringOptions;
		this.name = name;
	}
	
	/**
	 * Returns the toggle index of this option, which represents the default
	 * value of a toggle.
	 * 
	 * @return The toggle index of this default-toggle config option, or a
	 *         negative value if this does not represent a default toggle.
	 */
	public int getDefaultToggleIndex(){
		return defaultToggleIndex;
	}
	
	public String getDefaultValue(){
		return defaultValue.toString();
	}

	/**
	 * This returns an array of String values specified by this config option.
	 * Only works if this is a FINITE_STRING.
	 * 
	 * @return The finite array of Strings this option make take.
	 * @throws UnsupportedOperationException
	 *             if this isn't a FINITE_STRING.
	 */
	public String[] getFiniteStringOptions(){
		if (finiteStringOptions == null){
			throw new UnsupportedOperationException("This is not a finite string!");
		}
		return finiteStringOptions;
	}

	/**
	 * Returns an array of String values which comprise the long description of
	 * this option, separated into lines. This is used to generate tooltips.
	 * 
	 * @return
	 */
	public String[] getInfo(){
		return longInfo;
	}

	/**
	 * Returns the type of this option.
	 */
	public int getOptionType(){
		return type;
	}

	/**
	 * Returns a one-line short description of this option.
	 */
	public String getShortInfo(){
		return shortInfo;
	}

	/**
	 * Implementations must implement toString(), a compiler error will result
	 * if they do not.
	 * 
	 * @return The name of this config option, in human-readable but
	 *         computer-friendly format. (e.g. no #)
	 */
	@Override
	public String toString(){
		return name;
	}

}
