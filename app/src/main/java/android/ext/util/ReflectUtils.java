package android.ext.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Class ReflectUtils
 * @author Garfield
 */
public final class ReflectUtils {
    /**
     * Returns a {@link Constructor} object with the specified <em>clazz</em> and <em>parameterTypes</em>.
     * The returned <tt>Constructor</tt> object accessible flag is <tt>true</tt>.
     * @param clazz The <tt>Class</tt> which is declared the constructor.
     * @param parameterTypes The parameter types of the constructor or <em>(Class[])null</em> is equivalent
     * to the empty array.
     * @return A <tt>Constructor</tt> object.
     * @throws NoSuchMethodException if the requested constructor cannot be found.
     */
    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) throws NoSuchMethodException {
        final Constructor<T> result = clazz.getDeclaredConstructor(parameterTypes);
        result.setAccessible(true);
        return result;
    }

    /**
     * Returns a {@link Method} object with the specified <em>clazz</em> and <em>name</em>.
     * The returned <tt>Method</tt> object accessible flag is <tt>true</tt>.
     * @param clazz The <tt>Class</tt> which is declared the method.
     * @param name The name of the method.
     * @param parameterTypes The parameter types of the method or <em>(Class[])null</em> is
     * equivalent to the empty array.
     * @return A <tt>Method</tt> object.
     * @throws NoSuchMethodException if the requested method cannot be found.
     */
    public static Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        final Method result = clazz.getDeclaredMethod(name, parameterTypes);
        result.setAccessible(true);
        return result;
    }

    /**
     * Returns a new instance with the specified <em>className</em> and <em>args</em>.
     * @param className The name of the <tt>Class</tt> which is declared the constructor.
     * @param parameterTypes The parameter types of the constructor.
     * @param args The arguments of the constructor.
     * @return A new instance.
     * @throws ReflectiveOperationException if an error occurs while creating an instance.
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String className, Class<?>[] parameterTypes, Object... args) throws ReflectiveOperationException {
        final Constructor<?> ctor = Class.forName(className).getDeclaredConstructor(parameterTypes);
        ctor.setAccessible(true);
        return (T)ctor.newInstance(args);
    }

    /**
     * Returns a new instance with the specified <em>clazz</em> and <em>args</em>.
     * @param clazz The <tt>Class</tt> which is declared the constructor.
     * @param parameterTypes The parameter types of the constructor.
     * @param args The arguments of the constructor.
     * @return A new instance.
     * @throws ReflectiveOperationException if an error occurs while creating an instance.
     */
    public static <T> T newInstance(Class<? extends T> clazz, Class<?>[] parameterTypes, Object... args) throws ReflectiveOperationException {
        final Constructor<? extends T> ctor = clazz.getDeclaredConstructor(parameterTypes);
        ctor.setAccessible(true);
        return ctor.newInstance(args);
    }

    /**
     * Returns the attributes, do not call this method directly.
     * @hide
     */
    public static int[] getAttributes(String packageName, String name) {
        try {
            return (int[])Class.forName(packageName + ".R$styleable").getField(name).get(null);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the index of attribute, do not call this method directly.
     * @hide
     */
    public static int getAttributeIndex(String packageName, String name) {
        try {
            return Class.forName(packageName + ".R$styleable").getField(name).getInt(null);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private ReflectUtils() {
    }
}
