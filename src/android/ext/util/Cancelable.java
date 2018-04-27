package android.ext.util;

/**
 * A <tt>Cancelable</tt> can be test the task is cancelled.
 * @author Garfield
 * @version 1.0
 */
public interface Cancelable {
    /**
     * Returns <tt>true</tt> if the task was cancelled before it completed normally.
     * To ensure that the task is cancelled as quickly as possible, you should always
     * check the return value of this method, if possible (inside a loop for instance.)
     * @return <tt>false</tt> if the task could not be cancelled, typically because
     * it has already completed normally, <tt>true</tt> otherwise.
     */
    boolean isCancelled();
}

/**
 * A dummy <tt>Cancelable</tt>.
 */
/* package */ final class DummyCancelable implements Cancelable {
    private static final Cancelable sInstance = new DummyCancelable();

    public static Cancelable obtain(Cancelable cancelable) {
        return (cancelable != null ? cancelable : sInstance);
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
