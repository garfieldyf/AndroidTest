package android.ext.util;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicReference;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Printer;

/**
 * Class Pools
 * @author Garfield
 */
public final class Pools {
    /**
     * Creates a new <b>one-size</b> {@link Pool}.
     * @param factory The {@link Factory} to create
     * a new element when the pool is empty.
     * @return An newly created <tt>Pool</tt>.
     */
    public static <T> Pool<T> newSimplePool(Factory<T> factory) {
        DebugUtils.__checkError(factory == null, "factory == null");
        return new SimplePool<T>(factory);
    }

    /**
     * Creates a new <b>fixed-size</b> {@link Pool}.
     * @param factory The {@link Factory} to create a new element
     * when the pool is empty.
     * @param maxSize The maximum number of elements in the pool.
     * @return An newly created <tt>Pool</tt>.
     * @see #synchronizedPool(Pool)
     */
    public static <T> Pool<T> newPool(Factory<T> factory, int maxSize) {
        DebugUtils.__checkError(factory == null, "factory == null");
        return new ArrayPool<T>(factory, maxSize);
    }

    /**
     * Creates a new <b>fixed-size</b> array {@link Pool}.
     * @param maxSize The maximum number of arrays to allow in this pool.
     * @param length The maximum number of elements in the each array.
     * @param componentType The array's component type.
     * @return An newly array <tt>Pool</tt>.
     * @see #synchronizedPool(Pool)
     */
    public static <T> Pool<T> newPool(int maxSize, int length, Class<?> componentType) {
        return new ObjectArrayPool<T>(maxSize, length, componentType);
    }

    /**
     * Returns a wrapper on the specified <em>pool</em> which synchronizes
     * all access to the pool.
     * @param pool The {@link Pool} to wrap in a synchronized pool.
     * @return A synchronized <tt>Pool</tt>.
     */
    public static <T> Pool<T> synchronizedPool(Pool<T> pool) {
        return new SynchronizedPool<T>(pool);
    }

    public static void dumpPool(Pool<?> pool, Printer printer) {
        if (pool instanceof SynchronizedPool) {
            ((SynchronizedPool<?>)pool).dump(printer);
        } else if (pool instanceof ArrayPool) {
            ((ArrayPool<?>)pool).dump(printer, pool.getClass().getSimpleName());
        }
    }

    /**
     * The <tt>Pool</tt> interface for managing a pool of objects.
     */
    public static interface Pool<T> {
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
    public static final class RectPool extends SimplePool<Rect> {
        public static final Pool<Rect> sInstance = new RectPool();

        /**
         * Constructor
         */
        private RectPool() {
            super(null);
        }

        @Override
        public Rect newInstance() {
            return new Rect();
        }

        /**
         * Equivalent to calling<pre>
         * final Rect rect = RectPool.sInstance.obtain();
         * rect.set(left, top, right, bottom);</pre>
         */
        public static Rect obtain(int left, int top, int right, int bottom) {
            final Rect result = sInstance.obtain();
            result.set(left, top, right, bottom);
            return result;
        }
    }

    /**
     * Class <tt>RectFPool</tt> is an <b>one-size</b> {@link RectF} pool.
     */
    public static final class RectFPool extends SimplePool<RectF> {
        public static final Pool<RectF> sInstance = new RectFPool();

        /**
         * Constructor
         */
        private RectFPool() {
            super(null);
        }

        @Override
        public RectF newInstance() {
            return new RectF();
        }

        /**
         * Equivalent to calling<pre>
         * final RectF rect = RectFPool.sInstance.obtain();
         * rect.set(left, top, right, bottom);</pre>
         */
        public static RectF obtain(float left, float top, float right, float bottom) {
            final RectF result = sInstance.obtain();
            result.set(left, top, right, bottom);
            return result;
        }
    }

    /**
     * Class <tt>MatrixPool</tt> is an <b>one-size</b> {@link Matrix} pool.
     */
    public static final class MatrixPool extends SimplePool<Matrix> {
        public static final Pool<Matrix> sInstance = new MatrixPool();

        /**
         * Constructor
         */
        private MatrixPool() {
            super(null);
        }

        @Override
        public Matrix newInstance() {
            return new Matrix();
        }
    }

    /**
     * Class <tt>ByteArrayPool</tt> for managing a pool of byte arrays.
     */
    public static final class ByteArrayPool {
        public static final Pool<byte[]> sInstance = new SynchronizedPool<byte[]>(new ObjectArrayPool<byte[]>(2, 8192, byte.class));
    }

    /**
     * Class <tt>SimplePool</tt> is an implementation of a {@link Pool}.
     */
    private static class SimplePool<T> implements Pool<T>, Factory<T> {
        private final Factory<T> factory;
        private final AtomicReference<T> referent;

        /**
         * Constructor
         * <p>Creates a new <b>one</b> size pool.</p>
         * @param factory The {@link Factory} to create
         * a new element when this pool is empty.
         */
        public SimplePool(Factory<T> factory) {
            this.referent = new AtomicReference<T>();
            this.factory  = (factory != null ? factory : this);
        }

        @Override
        public T newInstance() {
            throw new IllegalStateException("Must be implementation!");
        }

        @Override
        public T obtain() {
            final T element = referent.getAndSet(null);
            return (element != null ? element : factory.newInstance());
        }

        @Override
        public void recycle(T element) {
            referent.compareAndSet(null, element);
        }
    }

    /**
     * Class <tt>ArrayPool</tt> is an implementation of a {@link Pool}.
     */
    private static class ArrayPool<T> implements Pool<T>, Factory<T> {
        /* package */ int size;
        /* package */ final Object[] elements;
        /* package */ final Factory<T> factory;

        /**
         * Constructor
         * <p>Creates a new pool.</p>
         * @param factory The {@link Factory} to create
         * a new element when this pool is empty.
         * @param maxSize The maximum number of elements
         * to allow in this pool.
         */
        public ArrayPool(Factory<T> factory, int maxSize) {
            DebugUtils.__checkError(maxSize <= 0, "maxSize <= 0");
            this.elements = new Object[maxSize];
            this.factory  = (factory != null ? factory : this);
        }

        @Override
        public T newInstance() {
            throw new IllegalStateException("Must be implementation!");
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
            if (size < elements.length) {
                elements[size++] = element;
            }
        }

        public final void dump(Printer printer, String className) {
            final StringBuilder result = new StringBuilder(96);
            DebugUtils.dumpSummary(printer, result, 80, " Dumping %s [ size = %d, maxSize = %d ] ", className, size, elements.length);
            for (int i = 0; i < size; ++i) {
                final Object element = elements[i];
                result.setLength(0);

                DebugUtils.toString(element, result.append("  "));
                if (element.getClass().isArray()) {
                    result.append(" { length = ").append(Array.getLength(element)).append(" }");
                }

                printer.println(result.toString());
            }
        }
    }

    /**
     * Class <tt>ObjectArrayPool</tt> is an implementation of a {@link Pool}.
     */
    private static final class ObjectArrayPool<T> extends ArrayPool<T> {
        private final int length;
        private final Class<?> componentType;

        /**
         * Constructor
         * @param maxSize The maximum number of arrays to allow in this pool.
         * @param length The maximum number of elements in the each array.
         * @param componentType The array's component type.
         */
        public ObjectArrayPool(int maxSize, int length, Class<?> componentType) {
            super(null, maxSize);
            this.length = length;
            this.componentType = componentType;
            DebugUtils.__checkError(length <= 0, "length <= 0");
        }

        @Override
        @SuppressWarnings("unchecked")
        public T newInstance() {
            return (T)Array.newInstance(componentType, length);
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