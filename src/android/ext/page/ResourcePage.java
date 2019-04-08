package android.ext.page;

import java.io.Closeable;

/**
 * Like as {@link Page}, but this class can be release any system resources it holds.
 * @author Garfield
 */
public interface ResourcePage<E> extends Page<E>, Closeable {
    /**
     * Closes this page and release any system resources it holds. If
     * the page already closed then invoking this method has no effect.
     */
    public void close();
}
