package thebombzen.mods.thebombzenapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
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
@Mod(modid = "thebombzenapi", name = "ThebombzenAPI", version = "2.3.4")
public class ThebombzenAPI extends ThebombzenAPIBaseMod {

	/**
	 * Platform dependent newline. Microsoft thinks it's so cool with its
	 * carriage returns. It's not. Real operating systems don't use carriage
	 * returns before every line feed. (Except when returning a carriage.)
	 */
	public static final String newLine = String.format("%n");

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
	private static SortedSet<ThebombzenAPIBaseMod> mods = new TreeSet<ThebombzenAPIBaseMod>();
	
	/**
	 * This is the {System.identityHashCode} of the previous client-side world,
	 * used to detect when a new world has opened. By storing the
	 * {System.identityHashCode} rather than the {net.minecraft.world.World} we
	 * don't have to worry about garbage collection issues, but we can still
	 * detect a world change.
	 */
	@SideOnly(Side.CLIENT)
	public static int prevWorld;
	
	private static JarFile jarFile = null;

	/**
	 * Detects whether two collections of {net.minecraft.item.ItemStack} contain
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
	private static String getDescriptorName(Class<?> clazz){
		if (clazz == null){
			return null;
		}
		if (byte.class.equals(clazz)){
			return "B";
		} else if (short.class.equals(clazz)){
			return "S";
		} else if (int.class.equals(clazz)){
			return "I";
		} else if (long.class.equals(clazz)){
			return "J";
		} else if (double.class.equals(clazz)){
			return "D";
		} else if (float.class.equals(clazz)){
			return "F";
		} else if (char.class.equals(clazz)){
			return "C";
		} else if (boolean.class.equals(clazz)){
			return "Z";
		} else if (void.class.equals(clazz)){
			return "V";
		} else {
			String canonName = clazz.getCanonicalName();
			if (canonName == null){
				return null;
			}
			while (canonName.contains("]")){
				canonName = canonName.replaceAll("^(.*?)\\[\\]$", "\\[\\1");
			}
			return "L" + canonName.replace('.', '/') + ";";
		}
	}
	
	/**
	 * Get the set of {ThebombzenAPIBaseMod}. Mostly useful for ThebombzenAPI
	 * itself, because they're all FML mods anyway.
	 * 
	 * @return the list of registered {ThebombzenAPIBaseMod}.
	 */
	public static ThebombzenAPIBaseMod[] getMods() {
		return mods.toArray(new ThebombzenAPIBaseMod[mods.size()]);
	}
	
	/**
	 * Get the value of a private field.
	 * 
	 * @param arg
	 *            The object whose field we're retrieving.
	 * @param clazz
	 *            The declaring class of the private field. Use a class literal
	 *            and not getClass(). This might be a superclass of the class of
	 *            the object.
	 * @param name
	 *            The field name.
	 * @return The value of the field.
	 */
	public static <T, E> T getPrivateField(E instance, Class<? super E> declaringClass, String name) {
		return getPrivateField(instance, declaringClass, new String[] { name });
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
	 */
	public static <T, E> T getPrivateField(E instance, Class<? super E> declaringClass,
			String[] names) {
		return getPrivateField0(instance, declaringClass, ObfuscationReflectionHelper.remapFieldNames(declaringClass.getCanonicalName(), names));
	}

	@SuppressWarnings("unchecked")
	private static <T, E> T getPrivateField0(E object, Class<? super E> declaringClass, String[] names) {
		for (String name : names) {
			try {
				Field field = declaringClass.getDeclaredField(name);
				field.setAccessible(true);
				try {
					return (T) field.get(object);
				} catch (Exception e) {
					return null;
				}
			} catch (NoSuchFieldException nsfe) {
				continue;
			}
		}
		return null;
	}
	
	public static InputStream getResourceAsStream(ThebombzenAPIBaseMod mod, String resourceName) throws IOException {
		File source = FMLCommonHandler.instance().findContainerFor(mod).getSource();
		if (source.isDirectory()){
			return new FileInputStream(new File(source, resourceName));
		} else {
			try {
				jarFile.close();
			} catch (Exception e){
				
			}
			jarFile = new JarFile(source);
			JarEntry entry = jarFile.getJarEntry(resourceName);
			return jarFile.getInputStream(entry);
		}
	}

	/**
	 * Turn a {java.util.Collection} of {Integer}s into an array of {int}.
	 * {java.util.Collection.toArray()} doesn't let you do this.
	 * 
	 * @param coll
	 *            The {java.util.Collection} to convert.
	 * @return An array of {int}.
	 */
	public static int[] intArrayFromIntegerCollection(
			Collection<? extends Integer> coll) {
		List<Integer> ret = new ArrayList<Integer>();
		ret.addAll(coll);
		return intArrayFromIntegerList(ret);
	}

	/**
	 * Turn a {java.util.List} of {Integer}s into an array of {int}.
	 * {java.util.List.toArray()} doesn't let you do this.
	 * 
	 * @param list
	 *            The {java.util.List} to convert.
	 * @return An array of {int}.
	 */
	public static int[] intArrayFromIntegerList(List<? extends Integer> list) {
		int[] ret = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ret[i] = list.get(i);
		}
		return ret;
	}

	/**
	 * Correctly converts an {int[]} to {java.util.List<Integer>}, because
	 * {java.util.Arrays.asList()} converts it to a {java.util.List<int[]>} with
	 * one element.
	 * 
	 * @param Array
	 *            of {int}
	 * @return {java.util.List} of {Integer}
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
	 *            {Class.getMethods()} only returns public methods, and
	 *            {Class.getDeclaredMethods()} requires the declaring class.
	 * @param name
	 *            The name of the method we want to invoke
	 * @param parameterTypes
	 *            The types of the parameters of the method (because
	 *            overloading).
	 * @param args
	 *            The arguments we want to pass to the method.
	 * @return Whatever the method returns.
	 */
	public static <T, E> T invokePrivateMethod(E instance, Class<? super E> declaringClass,
			String name, Class<?>[] parameterTypes, Class<?> returnType, Object... args) {
		return invokePrivateMethod(instance, declaringClass, new String[] { name },
				parameterTypes, returnType, args);
	}
	
	/**
	 * Invokes a private method, arranged conveniently. This one allows you to
	 * pass multiple possible method names, useful for bbfuscation.
	 * 
	 * @param instance
	 *            This is the object whose method we're invoking.
	 * @param declaringClass
	 *            This is the declaring class of the method we want. Use a class
	 *            literal and not getClass(). This argument is necessary because
	 *            {Class.getMethods()} only returns public methods, and
	 *            {Class.getDeclaredMethods()} requires the declaring class.
	 * @param names
	 *            The multiple possible method names of the method we want to
	 *            invoke
	 * @param parameterTypes
	 *            The types of the parameters of the method (because
	 *            overloading).
	 * @param args
	 *            The arguments we want to pass to the method.
	 * @return Whatever the method returns.
	 */
	public static <T, E> T invokePrivateMethod(E instance, Class<? super E> declaringClass, String[] names, Class<?>[] parameterTypes, Class<?> returnType, Object... args){
		String internalClassName = declaringClass.getCanonicalName().replace('.', '/');
		StringBuilder descBuilder = new StringBuilder().append('(');
		
		for (Class<?> clazz : parameterTypes){
			descBuilder.append(getDescriptorName(clazz));
		}
		descBuilder.append(')').append(getDescriptorName(returnType));
		
		String desc = descBuilder.toString();
		String remappedDesc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(desc);
		
		String[] newNames = new String[names.length];
		for (int i = 0; i < names.length; i++){
			newNames[i] = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(internalClassName, names[i], remappedDesc);
		}
		return invokePrivateMethod0(instance, declaringClass, names, parameterTypes, args);
	}
	
	
	@SuppressWarnings("unchecked")
	private static <T, E> T invokePrivateMethod0(E instance, Class<? super E> declaringClass,
			String[] names, Class<?>[] parameterTypes, Object... args) {
		for (String name : names) {
			try {
				Method method = declaringClass.getDeclaredMethod(name, parameterTypes);
				method.setAccessible(true);
				try {
					return (T) method.invoke(instance, args);
				} catch (Exception e) {
					return null;
				}
			} catch (NoSuchMethodException nsme) {
				continue;
			}
		}
		return null;
	}

	/**
	 * Determines whether a method name is currently being executed. (That is,
	 * on the method stack.) This is useful for debugging and not much else.
	 * 
	 * @param methodName
	 * @return true if methodName is on the method stack, false otherwise.
	 */
	@SideOnly(Side.CLIENT)
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
	 * Parse an integer literal the way java would.
	 * That is, a decimal number is parsed as is,
	 * A number starting with 0 is octal, 0x is hexadecimal, and 0b is binary.
	 * Negatives will only be parsed if the minus sign comes AFTER the 0x/0b/0.
	 * @param The string to parse
	 * @return The integer value
	 * @throws NumberFormatException if the number is invalid.
	 */
	public static int parseInteger(String s){
		s = s.replace("_", "");
		boolean onecomp = s.charAt(0) == '~';
		if (onecomp){
			s = s.substring(1);
		}
		if (s.length() == 0){
			throw new NumberFormatException();
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
	 * Set the value of a private field.
	 * 
	 * @param arg
	 *            The object whose field we're setting.
	 * @param clazz
	 *            The declaring class of the private field. Use a class literal
	 *            and not getClass(). This might be a superclass of the class of
	 *            the object.
	 * @param name
	 *            The field name.
	 * @param set
	 *            The value we're assigning to the field.
	 */
	public static <E> void setPrivateField(E instance, Class<? super E> clazz, String name,
			Object set) {
		setPrivateField(instance, clazz, new String[] { name }, set);
	}

	public static <E> void setPrivateField(E instance, Class<? super E> declaringClass, String[] names, Object set){
		setPrivateField0(instance, declaringClass, ObfuscationReflectionHelper.remapFieldNames(declaringClass.getCanonicalName(), names), set);
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
	 * @param name
	 *            The field name.
	 * @param set
	 *            The value we're assigning to the field.
	 */
	private static <E> void setPrivateField0(E instance, Class<? super E> declaringClass,
			String[] names, Object set) {
		for (String name : names) {
			try {
				Field field = declaringClass.getDeclaredField(name);
				field.setAccessible(true);
				try {
					field.set(instance, set);
					return;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} catch (NoSuchFieldException nsfe) {
				continue;
			}
		}
		throw new RuntimeException("Field not found!");
	}

	private ThebombzenAPIMetaConfiguration dummyConfig = null;
	
	/**
	 * Main client tick loop.
	 * 
	 * @param tickEvent
	 *            the ClientTickEvent that forge passes.
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientTick(ClientTickEvent tickEvent) {

		if (tickEvent.phase.equals(Phase.END)) {
			return;
		}

		Minecraft mc = Minecraft.getMinecraft();

		if (mc.theWorld == null) {
			return;
		}

		int currWorld = System.identityHashCode(mc.theWorld);

		if (prevWorld == 0) {
			for (ThebombzenAPIBaseMod mod : mods) {
				String latestVersion = mod.getLatestVersion();
				if (!latestVersion.equals(mod.getLongVersionString())) {
					mc.thePlayer.addChatMessage(new ChatComponentText(
							latestVersion + " is available. "));
					mc.thePlayer.addChatMessage(IChatComponent.Serializer.func_150699_a("{\"text\": \"" + mod.getLongName() + ": " + mod.getDownloadLocationURLString() + "\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",value=\"" + mod.getDownloadLocationURLString() + "\"}}"));
					
				}
			}
		}

		if (prevWorld != currWorld) {
			for (ThebombzenAPIBaseMod mod : mods) {
				mod.readFromCorrectMemoryFile();
			}
		}

		for (ThebombzenAPIBaseMod mod : mods) {
			try {
				mod.getConfiguration().reloadPropertiesFromFileIfChanged();
			} catch (IOException ioe) {
				mod.throwException("Could not read properties!", ioe, false);
			}
		}

		prevWorld = currWorld;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		jarFile.close();
	}

	@Override
	public ThebombzenAPIConfiguration<?> getConfiguration() {
		if (dummyConfig == null){
			dummyConfig = new ThebombzenAPIMetaConfiguration();
		}
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
		return "ThebombzenAPI, version 2.3.4, Minecraft 1.7.2";
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
		return "https://dl.dropboxusercontent.com/u/51080973/Mods/ThebombzenAPI/TBZAPIVersion.txt";
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
	 * FML load method. Does load stuff.
	 * 
	 * @param event
	 */
	@EventHandler
	public void load(FMLInitializationEvent event) {
		for (ThebombzenAPIBaseMod mod : mods){
			try {
				mod.getConfiguration().load();
			} catch (IOException ioe) {
				throwException("Unable to open configuration!", ioe, true);
			}
			mod.init2(event);
		}
	}

	/**
	 * FML postInit method. Does postInit stuff.
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
	 * FML preInitMethod. Does preInit stuff.
	 * 
	 * @param event
	 */
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		initialize();
		FMLCommonHandler.instance().bus().register(this);
		for (ThebombzenAPIBaseMod mod : mods){
			mod.init1(event);
		}
	}

}
