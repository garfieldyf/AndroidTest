package android.ext.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import android.content.Context;
import dalvik.system.DexClassLoader;

/**
 * Class <tt>ClassFactory</tt> used to loads the DEX files that containing classes and
 * resources. This can be used to create a new instance that is declared in the DEX files.
 * @author Garfield
 * @version 1.0
 */
public class ClassFactory {
    private final ClassLoader mClassLoader;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param dexPath The list of jar/apk files containing classes and resources,
     * delimited by {@link File#pathSeparator}, which defaults to ":" on Android.
     * @param dexOutputDir The directory where optimized DEX files should be written.
     * This should be a writable directory.
     * @throws RuntimeException if an error occurs while loading libraries.
     * @see #ClassFactory(Context, String, String, String, String[])
     * @see #getCodeCacheDir(Context, String)
     */
    public ClassFactory(Context context, String dexPath, String dexOutputDir) {
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
     * @see #ClassFactory(Context, String, String)
     * @see #getCodeCacheDir(Context, String)
     */
    public ClassFactory(Context context, String dexPath, String dexOutputDir, String librarySearchPath, String... libraryNames) {
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
            final Method method = getDeclaredMethod(Runtime.class, "loadLibrary", String.class, ClassLoader.class);

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

    /**
     * Returns a {@link Constructor} object with the specified <em>clazz</em> and <em>parameterTypes</em>.
     * @param clazz The <tt>Class</tt> which is declared the constructor.
     * @param parameterTypes The parameter types of the constructor or <em>(Class[])null</em> is equivalent
     * to the empty array.
     * @throws NoSuchMethodException if the requested constructor cannot be found.
     * @see #getConstructor(String, Class[])
     */
    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) throws NoSuchMethodException {
        final Constructor<?> result = clazz.getDeclaredConstructor(parameterTypes);
        result.setAccessible(true);
        return result;
    }

    /**
     * Returns a {@link Constructor} object with the specified <em>className</em> and <em>parameterTypes</em>.
     * @param className The name of the <tt>Class</tt> which is declared the constructor.
     * @param parameterTypes The parameter types of the constructor or <em>(Class[])null</em> is equivalent
     * to the empty array.
     * @throws ClassNotFoundException if the requested class cannot be found.
     * @throws NoSuchMethodException if the requested constructor cannot be found.
     * @see #getConstructor(Class, Class[])
     */
    public static Constructor<?> getConstructor(String className, Class<?>... parameterTypes) throws ClassNotFoundException, NoSuchMethodException {
        final Constructor<?> result = Class.forName(className).getDeclaredConstructor(parameterTypes);
        result.setAccessible(true);
        return result;
    }

    /**
     * Returns a {@link Method} object with the specified <em>name</em> and <em>parameterTypes</em>.
     * @param clazz The <tt>Class</tt> which is declared the method.
     * @param name The requested method's name.
     * @param parameterTypes The parameter types of the method or <em>(Class[])null</em> is equivalent
     * to the empty array.
     * @throws NoSuchMethodException if the requested method cannot be found.
     */
    public static Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        final Method result = clazz.getDeclaredMethod(name, parameterTypes);
        result.setAccessible(true);
        return result;
    }
}