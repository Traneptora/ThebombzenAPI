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
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This is the core of the API and contains many utility functions.
 * 
 * @author thebombzen
 */
@Mod(modid = "thebombzenapi", name = "ThebombzenAPI", version = "2.4.0pre2")
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
	public static int prevWorld;
	
	public static final String[] singleMultiStrings = new String[]{"Never", "Singleplayer Only", "Multiplayer Only", "Always"};
	
	private static MetaConfiguration dummyConfig = null;
	//private static Map<ThebombzenAPIBaseMod, Map<Integer, Boolean>> keysPreviouslyDown = new HashMap<ThebombzenAPIBaseMod, Map<Integer, Boolean>>(); 

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
	
	public static int countOccurrences(String s, char c){
		return s.length() - s.replace(Character.toString(c), "").length();
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
	public static <T, E> T getPrivateField(E instance, Class<? super E> declaringClass,
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
	public static <T, E> T invokePrivateMethod(E instance, Class<? super E> declaringClass,
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
	public static <T, E> T invokePrivateMethod(E instance, Class<? super E> declaringClass, String[] names, Class<?>[] parameterTypes, Object... args) throws MethodNotFoundException {
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
	 * 
	 * @param methodName
	 * @return true if methodName is on the method stack, false otherwise.
	 */
	public static boolean isCurrentlyExecutingMethod(String methodName) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : trace) {
			if (element.getMethodName().equals(methodName)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Checks to see if the current world is a freshly loaded world and is the first world loaded.
	 * @return
	 */
	@SideOnly(Side.CLIENT)
	public static boolean isWorldFirstLoadedWorld(){
		if (Minecraft.getMinecraft().theWorld == null){
			return false;
		}
		return prevWorld == 0;
	}

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
	 * @throws NumberFormatException if the number is invalid.
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
			throw new NumberFormatException("Too short!");
		}
		if (s.contains("-") || s.contains("+")){
			throw new NumberFormatException("Should not contain + or -!");
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
		//keysPreviouslyDown.put(mod, new HashMap<Integer, Boolean>());
		//for (int i = 0; i < mod.getNumToggleKeys(); i++){
		//	keysPreviouslyDown.get(mod).put(i, false);
		//}
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
	public static <E> void setPrivateField(E instance, Class<? super E> declaringClass, Object value, String... names){
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
	 * Main client tick loop.
	 * 
	 * @param tickEvent
	 *            the ClientTickEvent that forge passes.
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientTick(ClientTickEvent tickEvent) {

		if (!tickEvent.phase.equals(Phase.START)) {
			return;
		}

		Minecraft mc = Minecraft.getMinecraft();

		if (mc.theWorld == null) {
			return;
		}
		if (isWorldFirstLoadedWorld()) {
			for (ThebombzenAPIBaseMod mod : mods) {
				String latestVersion = mod.getLatestVersion();
				if (!latestVersion.equals(mod.getLongVersionString())) {
					mc.thePlayer.addChatMessage(new ChatComponentText(latestVersion + " is available. "));
					mc.thePlayer.addChatMessage(IChatComponent.Serializer.func_150699_a("{\"text\": \"" + mod.getLongName() + ": " + mod.getDownloadLocationURLString() + "\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",value=\"" + mod.getDownloadLocationURLString() + "\"}}"));
				}
			}
		}

		if (hasWorldChanged()) {
			for (ThebombzenAPIBaseMod mod : mods) {
				mod.readFromCorrectMemoryFile();
			}
		}

		for (ThebombzenAPIBaseMod mod : mods) {
			try {
				boolean did = mod.getConfiguration().reloadPropertiesFromFileIfChanged();
				if (did){
					mc.thePlayer.addChatMessage(new ChatComponentText("Reloaded " + mod.getLongName() + " configuration."));
				}
			} catch (IOException ioe) {
				mod.throwException("Could not read properties!", ioe, false);
			}
		}
		prevWorld = System.identityHashCode(mc.theWorld);
	}

	@SuppressWarnings("unchecked")
	@Override
	public MetaConfiguration getConfiguration() {
		return dummyConfig;
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
		return "ThebombzenAPI, version 2.4.0pre2, Minecraft 1.7.2";
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
		//return "https://dl.dropboxusercontent.com/u/51080973/Mods/ThebombzenAPI/TBZAPIVersion.txt";
		return "";
	}
	
	@SubscribeEvent
	public void handleKeyPress(KeyInputEvent event){
		for (ThebombzenAPIBaseMod mod : mods){
			int num = mod.getNumToggleKeys();
			for (int i = 0; i < num; i++){
				if (Keyboard.isKeyDown(mod.getToggleKeyCode(i)) && !Keyboard.isRepeatEvent()){
					boolean enabled = mod.isToggleEnabled(i);
					mod.setToggleEnabled(i, !enabled, true);
					mod.writeToCorrectMemoryFile();
				}
			}
		}
	}

	/**
	 * FML load method. Does load stuff and calls init2 of the mods. Also loads the configuration of all the mods.
	 * 
	 * @param event
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
	 * 
	 * @param event
	 */
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		for (ThebombzenAPIBaseMod mod : mods){
			mod.init3(event);
		}
	}
	
	/**
	 * FML preInitMethod. Does preInit stuff and calls init1 of the mods. Note that in the init1 state the configuration is not loaded.
	 * 
	 * @param event
	 */
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		initialize();
		FMLCommonHandler.instance().bus().register(this);
		FMLCommonHandler.instance().findContainerFor(this).getMetadata().authorList = Arrays.asList("Thebombzen");
		dummyConfig = new MetaConfiguration();
		for (ThebombzenAPIBaseMod mod : mods){
			mod.init1(event);
		}
	}

}
