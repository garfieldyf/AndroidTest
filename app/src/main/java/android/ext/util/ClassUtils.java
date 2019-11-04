package android.ext.util;

import android.content.Context;
import java.lang.reflect.Constructor;

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
     */
    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) throws NoSuchMethodException {
        final Constructor<T> result = clazz.getDeclaredConstructor(parameterTypes);
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
        final Constructor<?> constructor = Class.forName(className).getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return (T)constructor.newInstance(args);
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
