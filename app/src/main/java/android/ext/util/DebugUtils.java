package android.ext.util;

import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import java.lang.reflect.Modifier;

/**
 * Class DebugUtils
 * @author Garfield
 */
public final class DebugUtils {
    /**
     * Start method tracing. <p>This method can be used DEBUG mode.</p>
     * @see #stopMethodTracing(String, String)
     * @see #stopMethodTracing(String, String, char)
     */
    public static void startMethodTracing() {
        new TraceLocal(Thread.currentThread());
    }

    /**
     * Equivalent to calling <tt>stopMethodTracing(tag, prefix, 'm')</tt>.
     * @param tag Used to identify the source of a log message.
     * @param prefix The prefix to print. It usually identifies the calling method name.
     * @see #startMethodTracing()
     * @see #stopMethodTracing(String, String, char)
     */
    public static void stopMethodTracing(String tag, String prefix) {
        stopMethodTracing(tag, prefix, 'm');
    }

    /**
     * Print the method running time to logcat since call {@link #startMethodTracing()}.
     * <p>This method can be used DEBUG mode.</p>
     * @param tag Used to identify the source of a log message.
     * @param prefix The prefix to print. It usually identifies the calling method name.
     * @param timeUnit The time unit to print. Pass 'n' (nanoseconds), 'u' (microseconds)
     * or 'm' (milliseconds).
     * @see #startMethodTracing()
     * @see #stopMethodTracing(String, String)
     */
    public static void stopMethodTracing(String tag, String prefix, char timeUnit) {
        final long runningTime, nanoTime = TraceLocal.uptimeNanos();
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
     * Called on the DEBUG mode, do not call this method directly.
     */
    public static void __checkStartMethodTracing() {
        startMethodTracing();
    }

    /**
     * Called on the DEBUG mode, do not call this method directly.
     */
    public static void __checkStopMethodTracing(String tag, String prefix) {
        stopMethodTracing(tag, prefix, 'm');
    }

    /**
     * Called on the DEBUG mode, do not call this method directly.
     */
    public static void __checkUIThread(String method) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new AssertionError("The " + method + " method must be invoked on the UI thread.");
        }
    }

    /**
     * Called on the DEBUG mode, do not call this method directly.
     */
    public static void __checkError(boolean reportError, String message) {
        if (reportError) {
            throw new AssertionError(message);
        }
    }

    /**
     * Called on the DEBUG mode, do not call this method directly.
     */
    public static void __checkRange(int offset, int length, int arrayLength) {
        if ((offset | length) < 0 || arrayLength - offset < length) {
            throw new AssertionError("Index out of bounds - [ offset = " + offset + ", length = " + length + ", array length = " + arrayLength + " ]");
        }
    }

    /**
     * Called on the DEBUG mode, do not call this method directly.
     */
    public static void __checkDebug(boolean report, String tag, String message) {
        if (report) {
            Log.d(tag, message);
        }
    }

    /**
     * Called on the DEBUG mode, do not call this method directly.
     */
    public static void __checkWarning(boolean report, String tag, String message) {
        if (report) {
            Log.w(tag, "WARNING: " + message);
        }
    }

    /**
     * Called on the DEBUG mode, do not call this method directly.
     */
    public static void __checkWarning(boolean report, String tag, String message, Throwable e) {
        if (report) {
            Log.w(tag, "WARNING: " + message, e);
        }
    }

    /**
     * Called on the DEBUG mode, do not call this method directly.
     */
    public static void __checkLogError(boolean report, String tag, String message) {
        if (report) {
            Log.e(tag, "ERROR: " + message);
        }
    }

    /**
     * Called on the DEBUG mode, do not call this method directly.
     */
    public static void __checkLogError(boolean report, String tag, String message, Throwable e) {
        if (report) {
            Log.e(tag, "ERROR: " + message, e);
        }
    }

    /**
     * Called on the DEBUG mode, do not call this method directly.
     */
    public static void __checkMemoryLeaks(Class<?> clazz) {
        if ((clazz.isAnonymousClass() || clazz.isMemberClass()) && (clazz.getModifiers() & Modifier.STATIC) == 0) {
            final String className = clazz.getName();
            Log.e(className, "WARNING: The " + className + " class should be a static inner member class to avoid potential memory leaks.", new RuntimeException());
        }
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
            this.nanoTime = SystemClock.elapsedRealtimeNanos();
            sTraceLocal.set(this);
        }

        public static long uptimeNanos() {
            final TraceLocal local = sTraceLocal.get();
            DebugUtils.__checkError(local == null || local.thread != Thread.currentThread(), "Only the original thread that called startMethodTracing() can be call stopMethodTracing().");
            return SystemClock.elapsedRealtimeNanos() - local.nanoTime;
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private DebugUtils() {
    }
}
