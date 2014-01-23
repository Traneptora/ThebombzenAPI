package thebombzen.mods.thebombzenapi;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

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
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = "thebombzenapi", name = "ThebombzenAPI", version = "2.3.0")
public class ThebombzenAPI {

	/**
	 * Platform dependent newline. Microsoft thinks it's so cool with its
	 * carriage returns. It's not. Real operating systems don't use carriage
	 * returns before every line feed. (Except when returning a carriage.)
	 */
	public static final String newLine = String.format("%n");

	/**
	 * The mod instance, if you couldn't figure that out yourself.
	 */
	@Instance(value = "ThebombzenAPI")
	public static ThebombzenAPI instance;

	/**
	 * Various side-dependent routines
	 */
	@SidedProxy(clientSide = "thebombzen.mods.thebombzenapi.client.ClientProxy", serverSide = "thebombzen.mods.thebombzenapi.CommonProxy")
	public static CommonProxy proxy;

	/**
	 * The set of ThebombzenAPIBaseMod. Note that each mod must individually
	 * register itself because it's easier for me than that fancy classpath
	 * probing schtuff.
	 */
	private static Set<ThebombzenAPIBaseMod> mods = new HashSet<ThebombzenAPIBaseMod>();

	/**
	 * This is the {System.identityHashCode} of the previous client-side world,
	 * used to detect when a new world has opened. By storing the
	 * {System.identityHashCode} rather than the {net.minecraft.world.World} we
	 * don't have to worry about garbage collection issues, but we can still
	 * detect a world change.
	 */
	@SideOnly(Side.CLIENT)
	public static int prevWorld = 0;

	/**
	 * Keycodes used for ThebombzenAPI (should be empty after forge config
	 * windows in mc1.7.2).
	 */
	@SideOnly(Side.CLIENT)
	private static int[] thebombzenAPIKeyCodes;

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

		List<ItemStack> list1 = new ArrayList<>(coll1);
		List<ItemStack> list2 = new ArrayList<>(coll2);

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
	public static <T> T getPrivateField(Object arg, Class<?> clazz, String name) {
		return getPrivateField(arg, clazz, new String[] { name });
	}

	/**
	 * Get the value of a private field. This one allows you to pass multiple
	 * field names (useful for obfuscation).
	 * 
	 * @param arg
	 *            The object whose field we're retrieving.
	 * @param clazz
	 *            The declaring class of the private field. Use a class literal
	 *            and not getClass(). This might be a superclass of the class of
	 *            the object.
	 * @param name
	 *            The multiple field names of the field to retrieve.
	 * @return The value of the field.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getPrivateField(Object arg, Class<?> clazz,
			String[] names) {
		for (String name : names) {
			try {
				Field field = clazz.getDeclaredField(name);
				field.setAccessible(true);
				try {
					return (T) field.get(arg);
				} catch (Exception e) {
					return null;
				}
			} catch (NoSuchFieldException nsfe) {
				continue;
			}
		}
		return null;
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
		List<Integer> ret = new ArrayList<>();
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
		List<Integer> ret = new ArrayList<>(array.length);
		for (int i : array) {
			ret.add(i);
		}
		return ret;
	}

	/**
	 * Invokes a private method, arranged conveniently. Tip: Use class literals.
	 * 
	 * @param arg
	 *            This is the object whose method we're invoking.
	 * @param clazz
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
	public static <T> T invokePrivateMethod(Object arg, Class<?> clazz,
			String name, Class<?>[] parameterTypes, Object... args) {
		return invokePrivateMethod(arg, clazz, new String[] { name },
				parameterTypes, args);
	}

	/**
	 * Invokes a private method, arranged conveniently. This one allows you to
	 * pass multiple possible method names, useful for bbfuscation.
	 * 
	 * @param arg
	 *            This is the object whose method we're invoking.
	 * @param clazz
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
	@SuppressWarnings("unchecked")
	public static <T> T invokePrivateMethod(Object arg, Class<?> clazz,
			String[] names, Class<?>[] parameterTypes, Object... args) {
		for (String name : names) {
			try {
				Method method = clazz.getDeclaredMethod(name, parameterTypes);
				method.setAccessible(true);
				try {
					return (T) method.invoke(arg, args);
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
	 * Registers the ThebombzenAPIBaseMod for use with ThebombzenAPI Things will
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
	public static void setPrivateField(Object arg, Class<?> clazz, String name,
			Object set) {
		setPrivateField(arg, clazz, new String[] { name }, set);
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
	public static void setPrivateField(Object arg, Class<?> clazz,
			String[] names, Object set) {
		for (String name : names) {
			try {
				Field field = clazz.getDeclaredField(name);
				field.setAccessible(true);
				try {
					field.set(arg, set);
				} catch (Exception e) {
					return;
				}
			} catch (NoSuchFieldException nsfe) {
				continue;
			}
		}
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
					mc.thePlayer.func_146105_b(new ChatComponentText(
							latestVersion + " is available."));
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

	/**
	 * FML load method. Does load stuff.
	 * 
	 * @param event
	 */
	@EventHandler
	public void load(FMLInitializationEvent event) {

	}

	/**
	 * FML postInit method. Does postInit stuff.
	 * 
	 * @param event
	 */
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

	/**
	 * FML preInitMethod. Does preInit stuff.
	 * 
	 * @param event
	 */
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		if (event.getSide().equals(Side.CLIENT)) {
			thebombzenAPIKeyCodes = new int[] { Keyboard.KEY_B };
		}
	}

}
