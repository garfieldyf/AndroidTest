package android.ext.reference;

/**
 * Interface Referenceable
 * @author Garfield
 * @version 1.0
 */
public interface Referenceable {
    /**
     * Atomically increments by one the current reference count.
     * @see #release()
     * @see #referenceCount()
     */
    void addRef();

    /**
     * Atomically decrements by one the current reference count.
     * If the reference count <= 0, the resources associated with
     * this object will be released.
     * @see #addRef()
     * @see #referenceCount()
     */
    void release();

    /**
     * Returns the current reference count.
     * @return The current reference count.
     * @see #addRef()
     * @see #release()
     */
    int referenceCount();

    /**
     * Returns the size of this object in user-defined units.
     * @return The size of this object.
     */
    int sizeOf();

    /**
     * Checks this object is valid.
     * @return <tt>true</tt> if this object is valid, <tt>false</tt> otherwise.
     */
    boolean isValid();
}
