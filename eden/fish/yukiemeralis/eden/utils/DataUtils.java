package fish.yukiemeralis.eden.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.plugin.java.JavaPlugin;

import fish.yukiemeralis.eden.Eden;
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

    public static String fixColor(String in)
    {
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
     * <pre>
     * <code>
     * void methodA() {
     *    methodB();
     * }
     * 
     * void methodB() {
     *    StackTraceElement element = getPreviousCaller(Thread.currentThread());
     * 
     *    // Obtain the stacktrace element for the previous method
     *    assert element.getMethodName().equals("methodA");
     * }
     * </code>
     * </pre>
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
