package thebombzen.mods.thebombzenapi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This is the core of the API and contains many utility functions.
 * 
 * @author thebombzen
 */
@Mod(modid = "thebombzenapi", name = "ThebombzenAPI", version = Constants.VERSION, guiFactory = "thebombzen.mods.thebombzenapi.client.ConfigGuiFactory", acceptedMinecraftVersions = "[1.9.4, 1.11]")
public class ThebombzenAPI extends ThebombzenAPIBaseMod {

	/**
	 * Platform-dependent newline. Microsoft thinks it's so cool with its
	 * carriage returns. It's not. Real operating systems don't use carriage
	 * returns before every line feed. (Except when returning a carriage.)
	 */
	public static final String NEWLINE = String.format("%n");

	/**
	 * The mod instance
	 */
	@Instance("thebombzenapi")
	public static ThebombzenAPI instance;

	/**
	 * Various side-dependent routines
	 */
	@SidedProxy(clientSide = "thebombzen.mods.thebombzenapi.client.ClientSideSpecificUtilities", serverSide = "thebombzen.mods.thebombzenapi.server.ServerSideSpecificUtilities")
	public static SideSpecificUtlities sideSpecificUtilities;

	/**
	 * The set of ThebombzenAPIBaseMod. Note that each mod must individually
	 * register itself because it's easier for me than that fancy classpath
	 * probing schtuff.
	 */
	private static Set<ThebombzenAPIBaseMod> mods = new TreeSet<ThebombzenAPIBaseMod>();
	
	/**
	 * This is the System.identityHashCode of the previous client-side world,
	 * used to detect when a new world has opened. By storing the
	 * System.identityHashCode() rather than the World we
	 * don't have to worry about garbage collection issues, but we can still
	 * detect a world change.
	 */
	@SideOnly(Side.CLIENT)
	private static int prevWorld;
	
	/**
	 * The first tick to be executed is actually an "end" tick, not a start tick.
	 * This helps ensure we only care about "end" ticks if a "start" one has already occurred.
	 */
	@SideOnly(Side.CLIENT)
	private static boolean hasStart;
	
	/**
	 * The configuration for ThebombzenAPI itself.
	 */
	private static MetaConfiguration configuration = null; 

	/**
	 * checkNetwork accepts or rejects the mod list of the client or the server.
	 * ThebombzenAPI will always accept the mod list because it does require itself to be on either side.
	 */
	@NetworkCheckHandler
	public boolean checkNetwork(Map<String, String> modsList, Side remote){
		return true;
	}
	
	/**
	 * Detects whether two collections of ItemStack contain
	 * the same items. It depends on multiplicity, but doesn't depend on order.
	 * Note that it will return false if comparing 2 Dirt + 2 Dirt to 1 Dirt + 3
	 * Dirt.
	 * 
	 * @param coll1
	 *            The first collection
	 * @param coll2
	 *            The first collection
	 * @return true if they contain the same items (multiplicity matters, order
	 *         doesn't), false otherwise
	 */
	public static boolean areItemStackCollectionsEqual(
			Collection<? extends ItemStack> coll1,
			Collection<? extends ItemStack> coll2) {
		
		if (coll1.size() != coll2.size()) {
			return false;
		}

		List<ItemStack> list1 = new ArrayList<ItemStack>(coll1);
		List<ItemStack> list2 = new ArrayList<ItemStack>(coll2);

		Iterator<ItemStack> iter1 = list1.iterator();

		outer: while (iter1.hasNext()) {
			ItemStack stack1 = iter1.next();
			Iterator<ItemStack> iter2 = list2.iterator();
			while (iter2.hasNext()) {
				ItemStack stack2 = iter2.next();
				if (ItemStack.areItemStacksEqual(stack1, stack2)) {
					iter2.remove();
					iter1.remove();
					continue outer;
				}
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Counts the occurrences of a particular character in a particular string.
	 * @param s The string to check
	 * @param c The character to count
	 * @return The number of occurrences of the character
	 */
	public static int countOccurrences(String s, char c){
		return s.length() - s.replace(Character.toString(c), "").length();
	}
	
	/**
	 * Get the "extended key index" of a particular keyboard key or mouse button name.
	 * @param name The name of the key or button.
	 * @return The key index if name is a key. Otherwise, -100 plus the mouse button index.
	 */
	public static int getExtendedKeyIndex(String name){
		int index = Mouse.getButtonIndex(name);
		if (index == -1){
			return Keyboard.getKeyIndex(name);
		} else {
			return index - 100;
		}
	}
	
	/**
	 * Get the set of ThebombzenAPIBaseMods. Mostly useful for ThebombzenAPI
	 * itself, because they're all FML mods anyway.
	 * 
	 * @return the list of registered ThebombzenAPIBaseMod.
	 */
	public static ThebombzenAPIBaseMod[] getMods() {
		return mods.toArray(new ThebombzenAPIBaseMod[mods.size()]);
	}

	/**
	 * Get the value of a private field. This one allows you to pass multiple
	 * field names (useful for obfuscation).
	 * 
	 * @param instance
	 *            The object whose field we're retrieving.
	 * @param declaringClass
	 *            The declaring class of the private field. Use a class literal
	 *            and not getClass(). This might be a superclass of the class of
	 *            the object.
	 * @param name
	 *            The multiple field names of the field to retrieve.
	 * @return The value of the field.
	 * @throws SecurityException If a security Error occurred
	 * @throws FieldNotFoundException If some other error occurred
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getPrivateField(Object instance, Class<?> declaringClass,
			String... names) throws FieldNotFoundException {
		for (String name : names) {
			try {
				Field field = declaringClass.getDeclaredField(name);
				field.setAccessible(true);
				try {
					return (T) field.get(instance);
				} catch (Exception e) {
					throw new FieldNotFoundException("Cannot get value of field", e);
				}
			} catch (NoSuchFieldException nsfe) {
				continue;
			}
		}
		throw new FieldNotFoundException("Names not found: " + Arrays.toString(names));
	}
	
	/**
	 * Get an InputStream reading a mod reasource. Gets the stream from the jar file if it's a jar file or filesystem if it's a directory.
	 * @param mod The mod
	 * @param resourceName The name of the resource
	 * @return A self-contained InputStream reading the resource.
	 * @throws IOException If an I/O error orccurs.
	 */
	public static InputStream getResourceAsStream(ThebombzenAPIBaseMod mod, String resourceName) throws IOException {
		File source = FMLCommonHandler.instance().findContainerFor(mod).getSource();
		return new ModResourceInputStream(source, resourceName);
	}

	/**
	 * Checks to see if the current world is a freshly loaded world.
	 * Uses the Identity HashCode as the metric for "new."
	 * @return true for the entire first tick of the new world and false otherwise.
	 */
	@SideOnly(Side.CLIENT)
	public static boolean hasWorldChanged(){
		if (Minecraft.getMinecraft().theWorld == null){
			return false;
		}
		int currWorld = System.identityHashCode(Minecraft.getMinecraft().theWorld);
		return currWorld != prevWorld;
	}
	
	/**
	 * Sometimes the client-side world loads before the integrated server.
	 * This tests to see if the integrated server world has actually loaded.
	 * @return true if the player's current dimension is loaded on the server and false otherwise
	 * @throws UnsupportedOperationException if this is not singleplayer
	 */
	@SideOnly(Side.CLIENT)
	public static boolean hasServerWorldLoaded(){
		if (!Minecraft.getMinecraft().isSingleplayer()){
			throw new UnsupportedOperationException("Only works for Singleplayer!");
		}
		if (Minecraft.getMinecraft().thePlayer == null){
			return false;
		}
		WorldServer world = DimensionManager.getWorld(Minecraft.getMinecraft().thePlayer.dimension);
		return world != null;
	}

	/**
	 * Turn a Collection of Integers into an array of int.
	 * Collection.toArray() doesn't let you do this.
	 * 
	 * @param coll
	 *            The Collection to convert.
	 * @return Some int[]
	 */
	public static int[] intArrayFromIntegerCollection(
			Collection<? extends Integer> coll) {
		int[] ret = new int[coll.size()];
		int i = 0;
		Iterator<? extends Integer> iter = coll.iterator();
		while (iter.hasNext()){
			ret[i++] = iter.next();
		}
		return ret;
	}

	/**
	 * Correctly converts an int[] to java.util.List<Integer>, because
	 * Arrays.asList() converts it to a java.util.List<int[]> with
	 * one element.
	 * 
	 * @param Array
	 *            of int
	 * @return The List<Integer> containing the ints.
	 */
	public static List<Integer> intArrayToIntegerList(int[] array) {
		List<Integer> ret = new ArrayList<Integer>(array.length);
		for (int i : array) {
			ret.add(i);
		}
		return ret;
	}
	
	/**
	 * Invokes a private method, arranged conveniently. Tip: Use class literals.
	 * 
	 * @param instance
	 *            This is the object whose method we're invoking.
	 * @param declaringClass
	 *            This is the declaring class of the method we want. Use a class
	 *            literal and not getClass(). This argument is necessary because
	 *            Class.getMethods() only returns public methods, and
	 *            Class.getDeclaredMethods() requires the declaring class.
	 * @param name
	 *            The name of the method we want to invoke
	 * @param parameterTypes
	 *            The types of the parameters of the method (because
	 *            overloading).
	 * @param args
	 *            The arguments we want to pass to the method.
	 * @return Whatever the method returns
	 * @throws SecurityException if a security error occurs
	 * @throws MethodNotFoundException if another error occurs
	 */
	public static <T> T invokePrivateMethod(Object instance, Class<?> declaringClass,
			String name, Class<?>[] parameterTypes, Object... args) throws MethodNotFoundException {
		return invokePrivateMethod(instance, declaringClass, new String[] { name },
				parameterTypes, args);
	}
	
	/**
	 * Invokes a private method, arranged conveniently. This one allows you to
	 * pass multiple possible method names, useful for obfuscation.
	 * 
	 * @param instance
	 *            This is the object whose method we're invoking.
	 * @param declaringClass
	 *            This is the declaring class of the method we want. Use a class
	 *            literal and not getClass(). This argument is necessary because
	 *            Class.getMethods() only returns public methods, and
	 *            Class.getDeclaredMethods() requires the declaring class.
	 * @param names
	 *            The multiple possible method names of the method we want to
	 *            invoke
	 * @param parameterTypes
	 *            The types of the parameters of the method (because
	 *            overloading).
	 * @param args
	 *            The arguments we want to pass to the method.
	 * @return Whatever the invoked method returns
	 * @throws SecurityException if a security error occurs
	 * @throws MethodNotFoundException if another error occurs
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invokePrivateMethod(Object instance, Class<?> declaringClass, String[] names, Class<?>[] parameterTypes, Object... args) throws MethodNotFoundException {
		for (String name : names) {
			try {
				Method method = declaringClass.getDeclaredMethod(name, parameterTypes);
				method.setAccessible(true);
				try {
					
					return (T) method.invoke(instance, args);
				} catch (Exception e) {
					throw new MethodNotFoundException("Error invoking private method", e);
				}
			} catch (NoSuchMethodException nsme) {
				continue;
			}
		}
		throw new MethodNotFoundException("Cannot find method: " + Arrays.toString(names));
	}

	/**
	 * Determines whether a method name is currently being executed. (That is,
	 * on the method stack.) This is useful for debugging and not much else.
	 * @param methodName The name of the method to check
	 * @return true if methodName is on the call stack, false otherwise.
	 */
	public static boolean isCurrentlyExecutingMethod(String classname, String methodName) {
		return isCurrentlyExecutingMethodRepeatedly(classname, methodName, 1);
	}
	
	/**
	 * Determines whether a method name is currently being executed at least n times. (That is, on the call stack.)
	 * This is useful for detecting infinite loops while debugging and not much else.
	 * @param methodName The name of the method to check
	 * @return true if methodName is on the call stack at least n times, false otherwise.
	 */
	public static boolean isCurrentlyExecutingMethodRepeatedly(String classname, String methodName, int n) {
		if (n < 0){
			return false;
		} else if (n == 0){
			return true;
		}
		int found = 0;
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : trace) {
			if (element.getClassName().equals(classname) && element.getMethodName().equals(methodName)) {
				if (++found >= n){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks to see whether the "extended key" with the given name is down.
	 * "Exentended Key" is a keyboard key or a mouse button.
	 * @param name The name of the key or mouse button
	 * @return true if and only if it is currently down
	 */
	public static boolean isExtendedKeyDown(String name){
		return isExtendedKeyDown(getExtendedKeyIndex(name));
	}
	
	/**
	 * Checks whether the extended key with the given key index is down.
	 * If the index is negative then it checks for the mouse button at 100 + index.
	 * Otherwise it checks the keyboard key at index.
	 * @param index The extended key index to check
	 * @return true if and only if it is currently down
	 */
	public static boolean isExtendedKeyDown(int index){
		if (index < 0){
			return Mouse.isButtonDown(index + 100);
		} else {
			return Keyboard.isKeyDown(index);
		}
	}
	
	/**
	 * Checks whether or not the separator at the provided index is a top-level separator.
	 * That is, whether or not the separator is inside the middle of parentheses.
	 * The separator itself isn't checked, which means that if it's a parenthesis,
	 * you could get the wrong answer.
	 * @param info The string to parse
	 * @param index The index of the separator.
	 * @return true if and only if the separator is at the top level (that is, not inside parentheses)
	 */
	public static boolean isSeparatorAtTopLevel(String info, int index){
		String before = info.substring(0, index);
		String after = info.substring(index + 1);
		int beforeLeftCount = countOccurrences(before, '(');
		int beforeRightCount = countOccurrences(before, ')');
		int afterLeftCount = countOccurrences(after, '(');
		int afterRightCount = countOccurrences(after, ')');
		return (beforeLeftCount == beforeRightCount && afterLeftCount == afterRightCount);
	}

	/**
	 * Checks to see if the current world is a freshly loaded world and is the first world loaded.
	 * @return true if is the first game tick of the first loaded world on the client, and false otherwise 
	 */
	@SideOnly(Side.CLIENT)
	public static boolean isWorldFirstLoadedWorld(){
		if (Minecraft.getMinecraft().theWorld == null){
			return false;
		}
		return prevWorld == 0;
	}

	/**
	 * Custom, more generous parseBoolean method. Strips whitespace and has more options.
	 * @param s The string to parse
	 * @return true if and only if the given string is "true", "yes", "on", or "y" and false otherwise.
	 */
	public static boolean parseBoolean(String s){
		String c = s.toLowerCase().replaceAll("\\s", "");
		return c.equals("true") || c.equals("yes") || c.equals("on") || c.equals("y");
	}

	/**
	 * Parse an integer literal the way java would, but also allow a ~ in front or a - in front.
	 * That is, a decimal number is parsed as is,
	 * A number starting with 0 is octal, 0x is hexadecimal, and 0b is binary.
	 * Negatives will only be parsed if the - sign comes BEFORE the 0x/0b/0.
	 * @param The string to parse
	 * @return The integer value
	 * @throws NumberFormatException if the number is invalid
	 */
	public static int parseInteger(String s) throws NumberFormatException {
		s = s.replace("_", "");
		boolean onecomp = false;
		if (s.charAt(0) == '~'){
			onecomp = true;
			s = s.substring(1);
		}
		boolean negative;
		if (s.charAt(0) == '-'){
			negative = true;
			s = s.substring(1);
		} else {
			negative = false;
			if (s.charAt(0) == '+'){
				s = s.substring(1);
			}
		}
		if (s.length() == 0){
			throw new NumberFormatException("Too short");
		}
		if (s.contains("-") || s.contains("+")){
			throw new NumberFormatException("Number should not contain + or - inside");
		}
		int i;
		if (s.charAt(0) != '0'){
			i = Integer.parseInt(s);
		} else if (s.length() == 1){
			i = 0;
		} else switch (s.charAt(1)){
		case 'x':
		case 'X':
			i = Integer.parseInt(s.substring(2), 16);
			break;
		case 'b':
		case 'B':
			i = Integer.parseInt(s.substring(2), 2);
			break;
		default:
			i = Integer.parseInt(s, 8);
			break;
		}
		if (negative){
			i = -i;
		}
		if (onecomp){
			i = ~i;
		}
		return i;
	}
	
	/**
	 * Registers the ThebombzenAPIBaseMod for use with ThebombzenAPI. Things will
	 * probably not work if you don't register the mod. Note that your mod still
	 * needs to be an FML mod. This won't load it for you.
	 * 
	 * @param mod
	 *            The mod to register
	 */
	public static void registerMod(ThebombzenAPIBaseMod mod) {
		mods.add(mod);
	}
	
	
	/**
	 * Set the value of a private field. This one allows you to pass multiple
	 * field names (useful for obfuscation).
	 * 
	 * @param arg
	 *            The object whose field we're setting.
	 * @param clazz
	 *            The declaring class of the private field. Use a class literal
	 *            and not getClass(). This might be a superclass of the class of
	 *            the object.
	 * @param value
	 *            The value we're assigning to the field.
	 * @param name
	 *            The field name.
	 * @throws SecurityException If a security error occurred
	 * @throws FieldNotFoundException If some other error occurred
	 */
	public static void setPrivateField(Object instance, Class<?> declaringClass, Object value, String... names){
		for (String name : names) {
			try {
				Field field = declaringClass.getDeclaredField(name);
				field.setAccessible(true);
				try {
					field.set(instance, value);
					return;
				} catch (Exception e) {
					throw new FieldNotFoundException("Error setting field", e);
				}
			} catch (NoSuchFieldException nsfe) {
				continue;
			}
		}
		throw new FieldNotFoundException("Names not found: " + Arrays.toString(names));
	}
	
	/**
	 * Send update reminders about the most recent version of the mods.
	 * Verifies that the Update Reminders option is enabled before checking.
	 * This method will not actually check for updates if Update Reminders is disabled.
	 */
	private void sendUpdateReminders(){
		if (this.getConfiguration().getBooleanProperty(MetaConfiguration.UPDATE_REMINDERS)){
			for (ThebombzenAPIBaseMod mod : mods) {
				String latestVersion = mod.getLatestVersion();
				if (!latestVersion.equals(mod.getLongVersionString())) {
					sideSpecificUtilities.addMessageToOwner(new TextComponentString(latestVersion + " is available. "));
					sideSpecificUtilities.addMessageToOwner(ITextComponent.Serializer.jsonToComponent("{\"text\": \"" + mod.getLongName() + ": " + mod.getDownloadLocationURLString() + "\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + mod.getDownloadLocationURLString() + "\"}}"));
				}
			}
		}
	}
	
	/**
	 * Handle events on server-start on the server side.
	 * Currently affects update reminders only.
	 */
	@SideOnly(Side.SERVER)
	@EventHandler
	public void serverStarted(FMLServerStartedEvent event){
		sendUpdateReminders();
	}
	
	/**
	 * Handle server-side tick events.
	 * Currently only affects reloading the configurations.
	 * @param event the ServerTickEvent that Forge passes
	 */
	@SideOnly(Side.SERVER)
	@SubscribeEvent
	public void serverTick(ServerTickEvent event){
		if (!event.phase.equals(Phase.START)){
			return;
		}
		reloadPropertiesIfChanged();
	}
	
	/**
	 * Reload the properties files for all the mods, provided that they've changed.
	 * Changed is done using the last modified date on the filesystem.
	 */
	private void reloadPropertiesIfChanged(){
		for (ThebombzenAPIBaseMod mod : mods) {
			try {
				boolean did = mod.getConfiguration().reloadPropertiesFromFileIfChanged();
				if (did){
					sideSpecificUtilities.addMessageToOwner(new TextComponentString("Reloaded " + mod.getLongName() + " configuration."));
				}
			} catch (IOException ioe) {
				mod.throwException("Could not read properties!", ioe, false);
			}
		}
	}

	/**
	 * Main client tick loop.
	 * 
	 * @param tickEvent
	 *            the ClientTickEvent that forge passes
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientTick(ClientTickEvent tickEvent) {

		Minecraft mc = Minecraft.getMinecraft();
		
		if (mc.theWorld == null) {
			return;
		}
		
		if (tickEvent.phase.equals(Phase.END)) {
			if (hasStart){
				prevWorld = System.identityHashCode(mc.theWorld);
			}
			hasStart = false;
			return;
		} else {
			hasStart = true;
		}

		if (isWorldFirstLoadedWorld()) {
			sendUpdateReminders();
		}

		if (hasWorldChanged()) {
			for (ThebombzenAPIBaseMod mod : mods) {
				mod.markMemoryDirty();
			}
		}
		
		for (ThebombzenAPIBaseMod mod : mods){
			if (mod.isMemoryDirty()){
				mod.readFromCorrectMemoryFile();
			}
		}

		reloadPropertiesIfChanged();
	}

	@SuppressWarnings("unchecked")
	@Override
	public MetaConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public String getDownloadLocationURLString() {
		return "http://is.gd/ThebombzensMods#ThebombzenAPI";
	}

	@Override
	public String getLongName() {
		return "ThebombzenAPI";
	}

	@Override
	public String getLongVersionString() {
		return "ThebombzenAPI, version " + Constants.VERSION + ", Minecraft " + Constants.MC_VERSION;
	}

	@Override
	public int getNumToggleKeys() {
		return 0;
	}

	@Override
	public String getShortName() {
		return "TBZAPI";
	}
	
	@Override
	protected String getToggleMessageString(int index, boolean enabled) {
		throw new UnsupportedOperationException("ThebombzenAPI has no toggles!");
	}
	
	@Override
	protected String getVersionFileURLString() {
		return "https://dl.dropboxusercontent.com/u/51080973/Mods/ThebombzenAPI/TBZAPIVersion-" + Constants.MC_VERSION +".txt";
	}

	/**
	 * Enable or disable the toggle keys associated with each mod, according to whether or not they have been pressed.
	 */
	private void handleToggles(){
		for (ThebombzenAPIBaseMod mod : mods){
			int num = mod.getNumToggleKeys();
			for (int i = 0; i < num; i++){
				int toggleKeyCode = mod.getToggleKeyCode(i);
				if (ThebombzenAPI.isExtendedKeyDown(toggleKeyCode) && (toggleKeyCode < 0 && Mouse.getEventButton() != -1 || toggleKeyCode >= 0 && !Keyboard.isRepeatEvent())){
					boolean enabled = mod.isToggleEnabled(i);
					mod.setToggleEnabled(i, !enabled, true);
					mod.writeToCorrectMemoryFile();
				}
			}
		}
	}
	
	/**
	 * This event handler causes toggles to be handled rather than every tick.
	 * @param event the KeyInputEvent passed by Forge
	 */
	@SubscribeEvent
	public void handleMouseClick(MouseInputEvent event){
		handleToggles();
	}
	
	/**
	 * This event handler causes toggles to be handled rather than every tick.
	 * @param event the KeyInputEvent passed by Forge
	 */
	@SubscribeEvent
	public void handleKeyPress(KeyInputEvent event){
		handleToggles();
	}

	/**
	 * FML load method. Does load stuff and calls init2 of the mods. Also loads the configuration of all the mods.
	 */
	@EventHandler
	public void load(FMLInitializationEvent event) {
		for (ThebombzenAPIBaseMod mod : mods){
			try {
				mod.getConfiguration().load();
			} catch (IOException ioe) {
				mod.throwException("Unable to open configuration!", ioe, true);
			}
			mod.init2(event);
		}
	}
	
	/**
	 * FML postInit method. Does postInit stuff and calls init3 of the mods.
	 */
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		for (ThebombzenAPIBaseMod mod : mods){
			mod.init3(event);
		}
	}
	
	/**
	 * FML preInitMethod. Does preInit stuff and calls init1 of the mods. Note that in the init1 state the configuration is not loaded.
	 */
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().findContainerFor(this).getMetadata().authorList = Arrays.asList("Thebombzen");
		configuration = new MetaConfiguration();
		for (Object mod : Loader.instance().getReversedModObjectList().keySet()){
			if (mod instanceof ThebombzenAPIBaseMod){
				mods.add((ThebombzenAPIBaseMod)mod);
			}
		}
		for (ThebombzenAPIBaseMod mod : mods){
			mod.initialize();
		}
		for (ThebombzenAPIBaseMod mod : mods){
			mod.init1(event);
		}
	}
	/**
	 * Refreshes the hasStart calculation upon loading a new world.
	 */
	@SubscribeEvent
	public void worldLoaded(WorldEvent.Load event){
		if (event.getWorld().isRemote){
			hasStart = false;
		}
	}

}
