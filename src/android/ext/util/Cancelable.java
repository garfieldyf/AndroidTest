package android.ext.util;

/**
 * A <tt>Cancelable</tt> can be cancelled at any time by invoking {@link #cancel(boolean)}.
 * To ensure that the task is cancelled as quickly as possible, you should always check
 * the return value of {@link #isCancelled()}, if possible (inside a loop for instance.)
 * @author Garfield
 * @version 1.0
 */
public interface Cancelable {
    /**
     * Returns <tt>true</tt> if the task was cancelled before it completed normally.
     * @return <tt>false</tt> if the task could not be cancelled, typically because
     * it has already completed normally, <tt>true</tt> otherwise.
     * @see #cancel(boolean)
     */
    boolean isCancelled();

    /**
     * Attempts to stop execution of the task. This attempt will fail if the task
     * has already completed, or already been cancelled.
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing the task
     * should be interrupted, <tt>false</tt> otherwise.
     * @return <tt>false</tt> if the task could not be cancelled, <tt>true</tt> otherwise.
     * @see #isCancelled()
     */
    boolean cancel(boolean mayInterruptIfRunning);
}
