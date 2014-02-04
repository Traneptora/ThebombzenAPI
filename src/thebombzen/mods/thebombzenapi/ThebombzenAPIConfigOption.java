package thebombzen.mods.thebombzenapi;

/**
 * This interface provides a general rule for config options. Note that this
 * interface represents a key and not a value. Values are {String} values.
 * 
 * @author thebombzen
 */

public interface ThebombzenAPIConfigOption {

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
	 * This option can be any {String}.
	 */
	public static final int ARBITRARY_STRING = 3;

	/**
	 * Returns the toggle index of this option, which represents the default
	 * value of a toggle.
	 * 
	 * @return The toggle index of this default-toggle config option, or a
	 *         negative value if this does not represent a default toggle.
	 */
	public int getDefaultToggleIndex();

	/**
	 * This is the default value of this option.
	 */
	public String getDefaultValue();

	/**
	 * This returns an array of String values specified by this config option.
	 * Only works if this is a FINITE_STRING.
	 * 
	 * @return The finite array of Strings this option make take.
	 * @throws UnsupportedOperationException
	 *             if this isn't a FINITE_STRING.
	 */
	public String[] getFiniteStringOptions();

	/**
	 * Returns an array of String values which comprise the long description of
	 * this option, separated into lines. This is used to generate tooltips.
	 * 
	 * @return
	 */
	public String[] getInfo();

	/**
	 * Returns the type of this option.
	 */
	public int getOptionType();

	/**
	 * Returns a one-line short description of this option.
	 */
	public String getShortInfo();

	/**
	 * Implementations must implement toString(), a compiler error will result
	 * if they do not.
	 * 
	 * @return The name of this config option, in human-readable but
	 *         computer-friendly format. (e.g. no #)
	 */
	@Override
	public String toString();

}
