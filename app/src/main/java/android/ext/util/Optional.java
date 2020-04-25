package android.ext.util;

/**
 * Class Optional
 * @author Garfield
 */
public final class Optional {
    /**
     * Returns a {@link Cancelable} from the specified <em>cancelable</em>,
     * if non-null. Otherwise returns an empty <tt>Cancelable</tt>.
     */
    public static Cancelable ofNullable(Cancelable cancelable) {
        return (cancelable != null ? cancelable : EmptyCancelable.sInstance);
    }

    /**
     * Class <tt>EmptyCancelable</tt> is an implementation of a {@link Cancelable}.
     */
    private static final class EmptyCancelable implements Cancelable {
        public static final Cancelable sInstance = new EmptyCancelable();

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private Optional() {
    }
}
