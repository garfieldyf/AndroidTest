package android.ext.reference;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract class AbstractReferenceable
 * @author Garfield
 */
public abstract class AbstractReferenceable implements Referenceable {
    /**
     * The reference count.
     */
    protected final AtomicInteger mRefCount = new AtomicInteger();

    @Override
    public void addRef() {
        mRefCount.incrementAndGet();
    }

    @Override
    public int referenceCount() {
        return mRefCount.get();
    }

    @Override
    public int sizeOf() {
        return (isValid() ? 1 : 0);
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
