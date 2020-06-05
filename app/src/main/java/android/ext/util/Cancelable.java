package android.ext.util;

/**
 * A <tt>Cancelable</tt> interface for classes that can be cancelled.
 * @author Garfield
 */
public interface Cancelable {
    /**
     * Returns <tt>true</tt> if the task was cancelled before it completed normally.
     * To ensure that the task is cancelled as quickly as possible, you should always
     * check the return value of this method, if possible (inside a loop for instance.)
     * @return <tt>false</tt> if the task could not be cancelled, typically because
     * it has already completed normally, <tt>true</tt> otherwise.
     * @see #cancel(boolean)
     */
    boolean isCancelled();

    /**
     * Attempts to cancel execution of the task. This attempt will fail if the task
     * has already completed, already been cancelled. If the task has already started,
     * then the <em>mayInterruptIfRunning</em> parameter determines whether the thread
     * executing the task should be interrupted in an attempt to stop the task.
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing the task should
     * be interrupted, <tt>false</tt> otherwise.
     * @return <tt>false</tt> if the task could not be cancelled, typically because it has
     * already completed normally, <tt>true</tt> otherwise.
     * @see #isCancelled()
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * Returns a {@link Cancelable} from the specified <em>cancelable</em>,
     * if non-null. Otherwise returns an {@link EmptyCancelable}.
     */
    public static Cancelable ofNullable(Cancelable cancelable) {
        return (cancelable != null ? cancelable : EmptyCancelable.sInstance);
    }

    /**
     * Class <tt>EmptyCancelable</tt> is an implementation of a {@link Cancelable}.
     */
    public static final class EmptyCancelable implements Cancelable {
        /* package */ static final Cancelable sInstance = new EmptyCancelable();

        /**
         * This class cannot be instantiated.
         */
        private EmptyCancelable() {
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }
    }
}
