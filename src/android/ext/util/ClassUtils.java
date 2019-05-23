package android.ext.util;

import java.lang.reflect.Constructor;
import android.content.Context;

/**
 * Class ClassUtils
 * @author Garfield
 */
public final class ClassUtils {
    /**
     * Returns a {@link Constructor} object with the specified <em>clazz</em> and <em>parameterTypes</em>.
     * The returned <tt>Constructor</tt> object accessible flag is <tt>true</tt>.
     * @param clazz The <tt>Class</tt> which is declared the constructor.
     * @param parameterTypes The parameter types of the constructor or <em>(Class[])null</em> is equivalent
     * to the empty array.
     * @return A <tt>Constructor</tt> object.
     * @throws NoSuchMethodException if the requested constructor cannot be found.
     * @see #getConstructor(String, Class[])
     */
    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) throws NoSuchMethodException {
        final Constructor<T> result = clazz.getDeclaredConstructor(parameterTypes);
        result.setAccessible(true);
        return result;
    }

    /**
     * Equivalent to calling <tt>getConstructor(Class.forName(className), parameterTypes)</tt>.
     * @param className The name of the <tt>Class</tt> which is declared the constructor.
     * @param parameterTypes The parameter types of the constructor or <em>(Class[])null</em>
     * is equivalent to the empty array.
     * @return A <tt>Constructor</tt> object.
     * @throws ClassNotFoundException if the requested class cannot be found.
     * @throws NoSuchMethodException if the requested constructor cannot be found.
     * @see #getConstructor(Class, Class[])
     */
    public static Constructor<?> getConstructor(String className, Class<?>... parameterTypes) throws ClassNotFoundException, NoSuchMethodException {
        return getConstructor(Class.forName(className), parameterTypes);
    }

    /**
     * Returns the <em>package</em>.R.styleable.<em>name</em> field value.
     * @param context The <tt>Context</tt>.
     * @param name The name of field.
     * @return The value of the <em>name</em> field.
     */
    public static Object getFieldValue(Context context, String name) {
        try {
            return Class.forName(context.getPackageName() + ".R$styleable").getField(name).get(null);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private ClassUtils() {
    }
}
