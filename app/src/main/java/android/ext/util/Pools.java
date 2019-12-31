package android.ext.util;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Printer;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class Pools
 * @author Garfield
 */
public final class Pools {
    /**
     * Creates a new <b>fixed-size</b> {@link Pool}.
     * @param factory The {@link Factory} to create a new element
     * when the pool is empty.
     * @param maxSize The maximum number of elements in the pool.
     * @return An newly created <tt>Pool</tt>.
     * @see #synchronizedPool(Pool)
     */
    public static <T> Pool<T> newPool(Factory<T> factory, int maxSize) {
        return new ArrayPool<T>(factory, maxSize);
    }

    /**
     * Returns a wrapper on the specified <em>pool</em> which synchronizes
     * all access to the pool.
     * @param pool The {@link Pool} to wrap in a synchronized pool.
     * @return A synchronized <tt>Pool</tt>.
     * @see #newPool(Factory, int)
     */
    public static <T> Pool<T> synchronizedPool(Pool<T> pool) {
        return new SynchronizedPool<T>(pool);
    }

    public static void dumpPool(Pool<?> pool, Printer printer) {
        if (pool instanceof SynchronizedPool) {
            ((SynchronizedPool<?>)pool).dump(printer);
        } else if (pool instanceof ArrayPool) {
            ((ArrayPool<?>)pool).dump(printer, null);
        }
    }

    /**
     * The <tt>Pool</tt> interface for managing a pool of objects.
     */
    public static interface Pool<T> {
        /**
         * Removes all elements from this <tt>Pool</tt>, leaving it empty.
         */
        void clear();

        /**
         * Retrieves an element from this <tt>Pool</tt>. Allows us to avoid
         * allocating new elements in many cases. When the element can no
         * longer be used, The caller should be call {@link #recycle(T)} to
         * recycles the element. When this <tt>Pool</tt> is empty, should be
         * call {@link Factory#newInstance()} to create a new element.
         * @return The element.
         */
        T obtain();

        /**
         * Recycles the specified <em>element</em> to this <tt>Pool</tt>. After
         * calling this function you must not ever touch the <em>element</em> again.
         * @param element The element to recycle.
         */
        void recycle(T element);
    }

    /**
     * The <tt>Factory</tt> interface used to create a new object.
     */
    public static interface Factory<T> {
        /**
         * Creates a new instance.
         * @return A new instance.
         */
        T newInstance();
    }

    /**
     * Class <tt>RectPool</tt> is an <b>one-size</b> {@link Rect} pool.
     */
    public static final class RectPool extends AbstractPool<Rect> {
        public static final RectPool sInstance = new RectPool();

        /**
         * Constructor
         */
        private RectPool() {
        }

        /**
         * Equivalent to calling<pre>
         * final Rect rect = obtain();
         * rect.set(left, top, right, bottom);</pre>
         * @see #obtain()
         */
        public final Rect obtain(int left, int top, int right, int bottom) {
            final Rect result = obtain();
            result.set(left, top, right, bottom);
            return result;
        }

        @Override
        /* package */ Rect newInstance() {
            return new Rect();
        }
    }

    /**
     * Class <tt>RectFPool</tt> is an <b>one-size</b> {@link RectF} pool.
     */
    public static final class RectFPool extends AbstractPool<RectF> {
        public static final RectFPool sInstance = new RectFPool();

        /**
         * Constructor
         */
        private RectFPool() {
        }

        /**
         * Equivalent to calling<pre>
         * final RectF rect = obtain();
         * rect.set(left, top, right, bottom);</pre>
         * @see #obtain()
         */
        public final RectF obtain(float left, float top, float right, float bottom) {
            final RectF result = obtain();
            result.set(left, top, right, bottom);
            return result;
        }

        @Override
        /* package */ RectF newInstance() {
            return new RectF();
        }
    }

    /**
     * Class <tt>MatrixPool</tt> is an <b>one-size</b> {@link Matrix} pool.
     */
    public static final class MatrixPool extends AbstractPool<Matrix> {
        public static final MatrixPool sInstance = new MatrixPool();

        /**
         * Constructor
         */
        private MatrixPool() {
        }

        @Override
        /* package */ Matrix newInstance() {
            return new Matrix();
        }
    }

    /**
     * Class <tt>ByteArrayPool</tt> for managing a pool of byte arrays.
     */
    public static final class ByteArrayPool {
        public static final Pool<byte[]> sInstance = new SynchronizedPool<byte[]>(new ArrayPool<byte[]>(() -> new byte[8192], 2));
    }

    /**
     * Class <tt>AbstractPool</tt> is an implementation of a {@link Pool}.
     */
    private static abstract class AbstractPool<T> {
        private final AtomicReference<T> referent;

        /**
         * Constructor
         */
        public AbstractPool() {
            this.referent = new AtomicReference<T>();
        }

        /**
         * Retrieves an element from this <tt>Pool</tt>. Allows us to avoid
         * allocating new elements in many cases. When the element can no
         * longer be used, The caller should be call {@link #recycle(T)} to
         * recycles the element.
         * @return The element.
         * @see #recycle(T)
         */
        public final T obtain() {
            final T result = referent.getAndSet(null);
            return (result != null ? result : newInstance());
        }

        /**
         * Recycles the specified <em>element</em> to this <tt>Pool</tt>. After
         * calling this function you must not ever touch the <em>element</em> again.
         * @param element The element to recycle.
         * @see #obtain()
         */
        public final void recycle(T element) {
            referent.compareAndSet(null, element);
        }

        /**
         * Creates a new instance.
         * @return A new instance.
         */
        /* package */ abstract T newInstance();
    }

    /**
     * Class <tt>ArrayPool</tt> is an implementation of a {@link Pool}.
     */
    private static final class ArrayPool<T> implements Pool<T> {
        private int size;
        private final Object[] elements;
        private final Factory<T> factory;

        /**
         * Constructor
         * @param factory The {@link Factory} to create
         * a new element when this pool is empty.
         * @param maxSize The maximum number of elements
         * to allow in this pool.
         */
        public ArrayPool(Factory<T> factory, int maxSize) {
            DebugUtils.__checkError(maxSize <= 0, "maxSize <= 0");
            DebugUtils.__checkError(factory == null, "factory == null");
            this.factory  = factory;
            this.elements = new Object[maxSize];
        }

        @Override
        public void clear() {
            if (size > 0) {
                Arrays.fill(elements, 0, size, null);
                size = 0;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public T obtain() {
            final T element;
            if (size > 0) {
                element = (T)elements[--size];
                elements[size] = null;
            } else {
                element = factory.newInstance();
            }

            return element;
        }

        @Override
        public void recycle(T element) {
            __checkInPool(element);
            if (size < elements.length) {
                elements[size++] = element;
            }
        }

        public final void dump(Printer printer, String className) {
            final StringBuilder result = new StringBuilder(96);
            for (int i = 0; i < size; ++i) {
                final Object element = elements[i];
                if (i == 0) {
                    if (className == null) {
                        className = element.getClass().getSimpleName() + "Pool";
                    }

                    DebugUtils.dumpSummary(printer, result, 80, " Dumping %s [ size = %d, maxSize = %d ] ", className, size, elements.length);
                }

                result.setLength(0);
                DebugUtils.toString(element, result.append("  "));
                if (element.getClass().isArray()) {
                    result.append(" { length = ").append(Array.getLength(element)).append(" }");
                }

                printer.println(result.toString());
            }
        }

        private void __checkInPool(Object element) {
            for (int i = 0; i < size; ++i) {
                if (elements[i] == element) {
                    throw new AssertionError("The " + element + " is already in the pool!");
                }
            }
        }
    }

    /**
     * Class <tt>SynchronizedPool</tt> is an implementation of a {@link Pool}.
     */
    private static final class SynchronizedPool<T> implements Pool<T> {
        private final Pool<T> pool;

        /**
         * Constructor
         * <p>Creates a new synchronized pool.</p>
         * @param pool The {@link Pool}.
         */
        public SynchronizedPool(Pool<T> pool) {
            this.pool = pool;
            DebugUtils.__checkError(pool == null, "pool == null");
        }

        @Override
        public synchronized void clear() {
            pool.clear();
        }

        @Override
        public synchronized T obtain() {
            return pool.obtain();
        }

        @Override
        public synchronized void recycle(T element) {
            pool.recycle(element);
        }

        public synchronized final void dump(Printer printer) {
            if (pool instanceof ArrayPool) {
                ((ArrayPool<?>)pool).dump(printer, SynchronizedPool.class.getSimpleName());
            }
        }
    }

    /**
     * This class cannot be instantiated.
     */
    private Pools() {
    }
}