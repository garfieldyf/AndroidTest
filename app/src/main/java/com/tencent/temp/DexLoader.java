package com.tencent.temp;

import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.FileUtils;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Class <tt>DexLoader</tt> used to loads the DEX files that containing classes and
 * resources. This can be used to execute code not installed as part of an application.
 * @author Garfield
 */
public class DexLoader {
    private final ClassLoader mClassLoader;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param dexPath The list of jar/apk files containing classes and resources,
     * delimited by {@link File#pathSeparator}, which defaults to ":" on Android.
     * @param dexOutputDir The directory where optimized DEX files should be written.
     * This should be a writable directory.
     * @throws RuntimeException if an error occurs while loading libraries.
     * @see #DexLoader(Context, String, String, String[])
     * @see #DexLoader(Context, String, String, String, String[])
     */
    public DexLoader(Context context, String dexPath) {
        this(context, dexPath, getCodeCacheDir(context, dexPath), null, (String[])null);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param dexPath The list of jar/apk files containing classes and resources,
     * delimited by {@link File#pathSeparator}, which defaults to ":" on Android.
     * @param librarySearchPath The list of directories containing native libraries,
     * delimited by {@link File#pathSeparator}; may be <tt>null</tt>.
     * @param libraryNames The list of names containing the native libraries to load;
     * may be <tt>null</tt>.
     * @throws RuntimeException if an error occurs while loading libraries.
     * @see #DexLoader(Context, String)
     * @see #DexLoader(Context, String, String, String, String[])
     */
    public DexLoader(Context context, String dexPath, String librarySearchPath, String[] libraryNames) {
        this(context, dexPath, getCodeCacheDir(context, dexPath), librarySearchPath, libraryNames);
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
     * @see #DexLoader(Context, String)
     * @see #DexLoader(Context, String, String, String[])
     */
    public DexLoader(Context context, String dexPath, String dexOutputDir, String librarySearchPath, String... libraryNames) {
        mClassLoader = new DexClassLoader(dexPath, dexOutputDir, librarySearchPath, context.getClassLoader());
        try {
            loadLibraries(libraryNames);
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
    public void loadLibraries(String... libraryNames) throws Exception {
        if (ArrayUtils.getSize(libraryNames) > 0) {
            final Runtime runtime = Runtime.getRuntime();
            final Method method = Runtime.class.getDeclaredMethod("loadLibrary", String.class, ClassLoader.class);
            method.setAccessible(true);

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
     * Returns a directory of the optimized DEX files.
     * The result path such as <tt>"dexPath.optdex"</tt>
     */
    private static String getCodeCacheDir(Context context, String dexPath) {
        final String result = dexPath + ".optdex";
        FileUtils.mkdirs(result, 0);
        return result;
    }
}
