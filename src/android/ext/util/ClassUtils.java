package android.ext.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Class ClassUtils
 * @author Garfield
 * @version 1.0
 */
public final class ClassUtils {
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
        return getConstructor(Class.forName(className), parameterTypes);
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

    /**
     * This utility class cannot be instantiated.
     */
    private ClassUtils() {
    }
}
