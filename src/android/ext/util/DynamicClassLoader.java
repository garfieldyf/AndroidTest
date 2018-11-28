package android.ext.util;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import android.content.Context;
import dalvik.system.DexClassLoader;

/**
 * Class <tt>DynamicClassLoader</tt> used to loads the DEX files that containing classes
 * and resources. This can be used to execute code not installed as part of an application.
 * @author Garfield
 */
public class DynamicClassLoader {
    private final ClassLoader mClassLoader;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param dexPath The list of jar/apk files containing classes and resources,
     * delimited by {@link File#pathSeparator}, which defaults to ":" on Android.
     * @param dexOutputDir The directory where optimized DEX files should be written.
     * This should be a writable directory.
     * @throws RuntimeException if an error occurs while loading libraries.
     * @see #DynamicClassLoader(Context, String, String, String, String[])
     * @see #getCodeCacheDir(Context, String)
     */
    public DynamicClassLoader(Context context, String dexPath, String dexOutputDir) {
        this(context, dexPath, dexOutputDir, null, (String[])null);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param dexPath The list of jar/apk files containing classes and resources,
     * delimited by {@link File#pathSeparator}, which defaults to ":" on Android.
     * @param dexOutputDir The directory where optimized DEX files should be written.
     * This should be a writable directory.
     * @param librarySearchPath The list of directories containing native libraries,
     * delimited by {@link File#pathSeparator}; may be <tt>null</tt>.
     * @param libraryNames The list of names containing the native libraries to load.
     * If no native libraries to load, you can pass <em>(String[])null</em> instead of
     * allocating an empty array.
     * @throws RuntimeException if an error occurs while loading libraries.
     * @see #DynamicClassLoader(Context, String, String)
     * @see #getCodeCacheDir(Context, String)
     */
    public DynamicClassLoader(Context context, String dexPath, String dexOutputDir, String librarySearchPath, String... libraryNames) {
        mClassLoader = new DexClassLoader(dexPath, dexOutputDir, librarySearchPath, context.getClassLoader());
        try {
            load(libraryNames);
        } catch (Throwable e) {
            throw new RuntimeException("Couldn't load libraries - " + Arrays.toString(libraryNames), e);
        }
    }

    /**
     * Loads the class with the specified <tt>className</tt>.
     * @param className The name of the class to load.
     * @return The <tt>Class</tt> object.
     * @throws ClassNotFoundException if the class can not be found.
     */
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return mClassLoader.loadClass(className);
    }

    /**
     * Loads and links the shared libraries with the specified <em>libraryNames</em>.
     * @param libraryNames The list of names containing the shared libraries to load.
     * @throws Exception if the shared libraries can not be loaded.
     */
    public void load(String... libraryNames) throws Exception {
        if (ArrayUtils.getSize(libraryNames) > 0) {
            final Runtime runtime = Runtime.getRuntime();
            final Method method = ClassUtils.getDeclaredMethod(Runtime.class, "loadLibrary", String.class, ClassLoader.class);

            for (String libraryName : libraryNames) {
                method.invoke(runtime, libraryName, mClassLoader);
            }
        }
    }

    /**
     * Returns a new instance with the specified <em>className</em>.
     * @param className The name of the class.
     * @return A new instance.
     * @throws ReflectiveOperationException if the instance cannot be created.
     * @see #newInstance(String, Class[], Object[])
     */
    @SuppressWarnings("unchecked")
    public <T> T newInstance(String className) throws ReflectiveOperationException {
        return (T)mClassLoader.loadClass(className).newInstance();
    }

    /**
     * Returns a new instance with the specified <em>className,
     * parameterTypes</em> and <em>args</em>.
     * @param className The name of the class.
     * @param parameterTypes May be <tt>null</tt>. The parameter types of the
     * requested constructor.
     * @param args The arguments to the constructor. If no arguments, you can
     * pass <em>(Object[])null</em> instead of allocating an empty array.
     * @return A new instance.
     * @throws ReflectiveOperationException if the instance cannot be created.
     * @see #newInstance(String)
     */
    @SuppressWarnings("unchecked")
    public <T> T newInstance(String className, Class<?>[] parameterTypes, Object... args) throws ReflectiveOperationException {
        return (T)mClassLoader.loadClass(className).getConstructor(parameterTypes).newInstance(args);
    }

    /**
     * Returns the absolute path to the application specific cache directory on
     * the filesystem designed for storing cached code. <p>The result path such
     * as <tt>"/data/data/packagename/cache/name"</tt></p>
     * @param context The <tt>Context</tt>.
     * @param name The name of the directory to retrieve.
     * @return The path of the cached code directory.
     */
    public static String getCodeCacheDir(Context context, String name) {
        final String result = FileUtils.buildPath(context.getCacheDir().getPath(), name);
        FileUtils.mkdirs(result, 0);
        return result;
    }
}
