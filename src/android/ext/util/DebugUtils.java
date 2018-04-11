package android.ext.util;

import java.lang.reflect.Modifier;
import android.os.Looper;
import android.util.Log;
import android.util.Printer;

/**
 * Class DebugUtils
 * @author Garfield
 * @version 1.5
 */
public final class DebugUtils {
    /**
     * Start method tracing.
     * @see #stopMethodTracing(String, String)
     * @see #stopMethodTracing(String, String, char)
     */
    public static void startMethodTracing() {
        new TraceLocal(Thread.currentThread());
    }

    /**
     * Stop method tracing.
     * @return The method running time since call {@link #startMethodTracing()}
     * in nanoseconds.
     * @see #startMethodTracing()
     * @see #printMethodTracing(String, String, long, char)
     */
    public static long stopMethodTracing() {
        return TraceLocal.uptimeNanos();
    }

    /**
     * Print the method running time to logcat since call {@link #startMethodTracing()}.
     * @param tag Used to identify the source of a log message.
     * @param prefix The prefix to print. It usually identifies the method name.
     * @see #startMethodTracing()
     * @see #stopMethodTracing(String, String, char)
     */
    public static void stopMethodTracing(String tag, String prefix) {
        printMethodTracing(tag, prefix, TraceLocal.uptimeNanos(), 'm');
    }

    /**
     * Print the method running time to logcat since call {@link #startMethodTracing()}.
     * @param tag Used to identify the source of a log message.
     * @param prefix The prefix to print. It usually identifies the method name.
     * @param timeUnit The time unit to print. Pass 'n', 'u' or 'm'.
     * @see #startMethodTracing()
     * @see #stopMethodTracing(String, String)
     */
    public static void stopMethodTracing(String tag, String prefix, char timeUnit) {
        printMethodTracing(tag, prefix, TraceLocal.uptimeNanos(), timeUnit);
    }

    /**
     * Print the method running time to logcat since call {@link #startMethodTracing()}.
     * @param tag Used to identify the source of a log message.
     * @param prefix The prefix to print. It usually identifies the method name.
     * @param nanoTime The method running time returned by {@link #stopMethodTracing()}
     * in nanoseconds.
     * @param timeUnit The time unit to print. Pass 'n', 'u' or 'm'.
     * @see #startMethodTracing()
     * @see #stopMethodTracing()
     */
    public static void printMethodTracing(String tag, String prefix, long nanoTime, char timeUnit) {
        final long runningTime;
        switch (timeUnit) {
        // nanoseconds
        case 'n':
            runningTime = nanoTime;
            break;

        // microseconds
        case 'u':
            runningTime = nanoTime / 1000L;
            break;

        // milliseconds
        default:
            runningTime = nanoTime / 1000000L;
            break;
        }

        Log.d(tag, String.format("%s running time = %d%cs", prefix, runningTime, timeUnit));
    }

    /**
     * This ant task is imported by the project build file. It can be delete the
     * <b>_checkXXX</b> method invocation.<pre>
     * &lt;replaceregexp match="(.*)_check(.*);" replace="" flags="g" byline="true" &gt;
     *     &lt;fileset dir="${src.dir}" includes="**\*.java" /&gt;
     * &lt;/replaceregexp&gt;</pre>
     */
    public static void _checkMemoryLeaks(Class<?> clazz) {
        if ((clazz.isAnonymousClass() || clazz.isMemberClass()) && (clazz.getModifiers() & Modifier.STATIC) == 0) {
            Log.w(clazz.getName(), "WARNING", new IllegalStateException(new StringBuilder("The ").append(clazz.getName()).append(" class should be a static inner member class to avoid memory leaks").toString()));
        }
    }

    public static void _checkError(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    public static void _checkWarning(boolean condition, String tag, String message) {
        if (condition) {
            Log.w(tag, "WARNING", new RuntimeException(message));
        }
    }

    public static void _checkUIThread(String method) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new AssertionError("The " + method + " method must be invoked on the UI thread.");
        }
    }

    public static void dumpSummary(Printer printer, StringBuilder result, int maxLength, String format, Object... args) {
        final String summary = String.format(format, args);
        final int length = (maxLength - summary.length()) / 2;

        result.setLength(0);
        for (int i = 0; i < length; ++i) {
            result.append('=');
        }

        result.append(summary);
        for (int i = 0; i < length; ++i) {
            result.append('=');
        }

        printer.println(result.toString());
    }

    public static StringBuilder toString(Object object, StringBuilder result) {
        return result.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
    }

    public static StringBuilder toSimpleString(Object object, StringBuilder result) {
        return result.append(object.getClass().getSimpleName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
    }

    /**
     * Class <tt>TraceLocal</tt> to save the method start running time.
     */
    private static final class TraceLocal {
        private static final ThreadLocal<TraceLocal> sTraceLocal = new ThreadLocal<TraceLocal>();
        private final Thread thread;
        private final long nanoTime;

        public TraceLocal(Thread thread) {
            this.thread   = thread;
            this.nanoTime = System.nanoTime();
            sTraceLocal.set(this);
        }

        public static long uptimeNanos() {
            final TraceLocal local = sTraceLocal.get();
            if (local == null || local.thread != Thread.currentThread()) {
                throw new IllegalStateException("Only the original thread that called startMethodTracing() can be call stopMethodTracing().");
            }

            return System.nanoTime() - local.nanoTime;
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private DebugUtils() {
    }
}
