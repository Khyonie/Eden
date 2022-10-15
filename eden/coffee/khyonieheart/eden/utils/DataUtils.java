package fish.yukiemeralis.eden.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.bukkit.plugin.java.JavaPlugin;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.utils.logging.Logger.InfoType;
import fish.yukiemeralis.eden.utils.map.IDataListMap;

/**
 * A collection of slightly random utilities involving classes and data.
 * @author Yuki_emeralis
 */
public class DataUtils 
{
	/**
	 * Checks if a class has a method with the given name and parameters.
	 * @param methodName The name of a method
	 * @param class_ The class that supposedly contains a method
	 * @param classParams The parameters for the method
	 * @return Whether or not the class has a method with the given name and parameters.
	 */
    public static boolean hasMethod(String methodName, Class<?> class_, Class<?>... classParams)
    {
        try {
            class_.getMethod(methodName, classParams);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Obtains a declared method from anywhere in a class hierarchy.
     * @param clazz
     * @param name
     * @param args
     * @return A declared method
     * @throws NoSuchMethodException The class hierarchy does not declare such a method 
     * @throws SecurityException Java security manager prohibits obtaining this method
     * @since 1.7.3
     */
    public static Method getRecursiveSuperMethod(Class<?> clazz, String name, Class<?> args) throws NoSuchMethodException, SecurityException
    {
        // Check if the given class declares such a method, otherwise recurse
        try {
            Method method = clazz.getDeclaredMethod(name, args);
            return method;
        } catch (NoSuchMethodException e) {}

        // Recurse down to java.lang.Object if need be
        return getRecursiveSuperMethod(clazz, clazz, name, args);
    }

    private static Method getRecursiveSuperMethod(Class<?> parent, Class<?> clazz, String name, Class<?>... args) throws NoSuchMethodException, SecurityException
    {
        if (name == null)
            throw new IllegalArgumentException("No method name was supplied");

        Method method;

        Class<?> superClass = clazz.getSuperclass();
        
        if (superClass == null) // Current class is java.lang.Object or related, with no superclass
            throw new NoSuchMethodException("Class hierarchy for " + parent.getName() + " does not declare a method named " + name + " with " + args.length + " parameters");

        try {
            method = superClass.getDeclaredMethod(name, args);
            return method;
        } catch (NoSuchMethodException e) {
            return getRecursiveSuperMethod(parent, superClass, name, args);
        }
    }

    /**
     * Obtains the plugin .jar for Eden.
     * @return The plugin .jar for Eden.
     */
    public static File getEdenJar()
    {
        JavaPlugin plugin = (JavaPlugin) Eden.getInstance().getServer().getPluginManager().getPlugin("Eden");

        // Reflection fun
        try {
            Method getFile = JavaPlugin.class.getDeclaredMethod("getFile");
            getFile.setAccessible(true);

            File file = (File) getFile.invoke(plugin);
            return file;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            PrintUtils.printPrettyStacktrace(e);
            return null;
        }
    }

    private static int cachedVersion = Integer.MIN_VALUE;
    private static String edenVersion;
    public static int getMetaVersion()
    {
        if (cachedVersion != Integer.MIN_VALUE)
            return cachedVersion;
        
        try {
            JarFile file = new JarFile(getEdenJar());
            Scanner scanner = new Scanner(file.getInputStream(file.getEntry("version.meta")));

            cachedVersion = Integer.parseInt(scanner.nextLine());
            edenVersion = scanner.nextLine();

            scanner.close();
            file.close();
        } catch (IOException e) {
            cachedVersion = Integer.MAX_VALUE;
            PrintUtils.log("<Failed to get Eden's internal version! Internal version is set to " + cachedVersion + ".>", InfoType.ERROR);
        }

        return cachedVersion;
    }

    public static String getEdenVersion()
    {
        getMetaVersion();

        return edenVersion;
    }

    /**
     * Throws and catches an error, which generates a stacktrace in the console.
     * @param message A message describing what is being checked.
     */
    public static void generateStackTrace(String message)
    {
        try {
            throw new IntentionalStackTrace(message);
        } catch (IntentionalStackTrace e) {
            PrintUtils.printPrettyStacktrace(e);
        }
    }

    private static class IntentionalStackTrace extends Exception
    {
		private static final long serialVersionUID = 2438785758452796394L;

		public IntentionalStackTrace(String message)
        {
            System.out.println(message);
        }
    }

    /**
     * Combines a variable number of collections of type T into one ArrayList.</p>
     * 
     * This ArrayList cannot be modified and is NOT backed by any of the collections given.
     * Please ensure you understand this principle before invoking this method.
     * 
     * @param <T> Type of all the given collections.
     * @param collections A variable argument list of collections of type T.
     * @return An unmodifiable list containing all the elements of all the collections given.
     */
    @SafeVarargs
    public static <T> List<T> concatMultipleCollections(Collection<T>... collections)
    {
        List<T> buffer = new ArrayList<>();

        for (Collection<T> coll : collections)
            buffer.addAll(coll);

        return Collections.unmodifiableList(buffer);
    }

    public static long estimatePasswordStrength(String input)
    {
        // Strength is calculated using the function f(x) = cumulativeValue^length 
        // All latin letters and numbers have a value of 1, common symbols [!@$_-()] have a value of 2, uncommon symbols [#%^&*{}[]?/=+\|"',.<>] have a value of 3

        Pattern value1 = Pattern.compile("[a-zA-Z0-9]");
        Pattern value2 = Pattern.compile("[!@$_\\-\\(\\)]");
        Pattern value3 = Pattern.compile("[#%^&*+={}',.<>?_|\\[\\]\\/\\-\\\\\"]");

        int value1Matches = (int) value1.matcher(input).results().count(); 
        int value2Matches = (int) value2.matcher(input).results().count();
        int value3Matches = (int) value3.matcher(input).results().count(); 

        return (long) Math.pow(value1Matches + value2Matches + value3Matches, input.length());
    }

    /**
     * Attempts to infer missing data for a 24 bit (#RRGGBB) hex color.<p>
     * - # -> #000000 (Black)<p>
     * - #D -> #DDDDDD<p>
     * - #DD -> #DDDDDD<p>
     * - #RGB -> #R*G*B*<p>
     * - #RRGB -> #RRG*B*<p>
     * - #RRGGB -> #RRGGB*
     * @param in Input hex color, starting with "#"
     * @return Input hex color, converted to a color with 6 hex digits
     */
    public static String fixColor(String in)
    {
        if (in == null)
            throw new IllegalArgumentException("Input cannot be null");
        if (in.length() == 0)
            in = "#";

        // Trim # 
        String buffer = in.substring(1);
        // We can try to make some assumptions:
        // 1) # can be expanded to #000000
        // 2) #R can be expanded to #RRRRRR
        // 3) #RR can be expanded to #RRRRRR
        // 4) #RGB can be expanded to #RRGGBB
        // 5) #RRGB can be expanded to #RRGGBB
        // 6) #RRGGB can be expanded to #RRGGBB

        switch (buffer.length())
        {
            case 0: // # -> #000000
                return "#000000";
            case 1: // #R -> #RRRRRR
                return "#" + buffer.repeat(6);
            case 2: // #RR -> #RRRRRR
                return "#" + buffer.repeat(3);
            case 3: // #RGB -> #RRGGBB
                return "#" + buffer.substring(0, 1).repeat(2) + buffer.substring(1, 2).repeat(2) + buffer.substring(2).repeat(2);
            case 4: // #RRGB -> #RRGGBB
                return "#" + buffer.substring(0, 2) + buffer.substring(2, 3).repeat(2) + buffer.substring(3).repeat(2);
            case 5: // #RRGGB -> #RRGGBB
                return "#" + buffer.substring(0, 5) + buffer.substring(4).repeat(1);
            default: // Probably fine to trim off anything extra, even though the regex "<#[a-fA-F0-9]{0,6}>" can't match anything extra
                return "#" + buffer.substring(0, 6);
        }
    }

    /**
     * Converts a Map<K, V> to a list of individual key/value pairs. List is populated in the order returned by the map's iterator.
     * <pre><code>
     *Map&lt;String, Integer&gt; mapData = Map.of(
     *   "a", 42, 
     *   "b", 7, 
     *   "c", 11 );
     *
     *List&lt;KeyValuePair&lt;String, Integer&gt;&gt; kvPairList = DataUtils.toKeyValuePairSet(mapData);
     *
     *assert kvPairList.get(0).getKey().equals("a");
     *assert kvPairList.get(0).getValue().equals(42);
     * </code></pre>
     * @param <K> Key type
     * @param <V> Value type
     * @param map Map input
     * @return A list containing the ordered pairs contained in the given map
     */
    public static <K, V> List<KeyValuePair<K, V>> toKeyValuePairSet(Map<K, V> map)
    {
        List<KeyValuePair<K, V>> buffer = new ArrayList<>();

        map.forEach((key, value) -> buffer.add(new KeyValuePair<K, V>(key, value)));

        return buffer;
    }

    public static class KeyValuePair<K, V>
    {
        private K key;
        private V value;

        public KeyValuePair(K key, V value)
        {
            this.key = key;
            this.value = value;
        }

        public K getKey()
        {
            return key;
        }

        public V getValue()
        {
            return value;
        }
    }

    private static int cachedJavaVersion = -1;
    /**
     * Obtains the current Java version running on this server.
     * @return JRE version in use by the server 
     */
    public static int getJavaVersion()
    {
        if (cachedJavaVersion != -1)
            return cachedJavaVersion;

        String version = System.getProperty("java.version");

        if (version.startsWith("1.")) // 8 or older
        {
            cachedJavaVersion = Integer.parseInt(version.substring(2, 3));
            return cachedJavaVersion;
        }
            
        cachedJavaVersion = Integer.parseInt(version.split("\\.")[0]);
        // 9 or newer
        return cachedJavaVersion;
    }

    /**
     * Converts a list of A into a list of B.
     * @param <A> Original type
     * @param <B> Final type
     * @param input A list of type A.
     * @return A list of type B.
     */
    public static <A, B> List<B> mapList(List<A> input, IDataListMap<A, B> conversionMethod)
    {
        List<B> data = new ArrayList<>();

        input.forEach(i -> data.add(conversionMethod.run(i)));

        return data;
    }

    /**
     * Gets the stacktrace element before the method that called this method.<p>
     * <p>
     * For example:
     * <pre><code>
     *void methodA() {
     *   methodB();
     *}
     * 
     *void methodB() {
     *   StackTraceElement element = getPreviousCaller(Thread.currentThread());
     *
     *   // Obtain the stacktrace element for the previous method
     *   assert element.getMethodName().equals("methodA");
     *}
     * </code></pre>
     * This method will fail and return {@code null} if there isn't a method called before the caller (such as {@code main} or {@link Thread#start()}).
     * 
     * @param thread The thread to obtain a stacktrace element from.
     * @return A stacktrace element corresponding to the method prior to the caller of this method. May return null if no such element exists.
     */
    public static StackTraceElement getPreviousCaller(Thread thread)
    {
        try {
            return thread.getStackTrace()[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
