package android.ext.temp;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.ext.util.DebugUtils;
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
     * Creates a new <b>fixed-size</b> {@link Pool}.
     * @param factory The {@link Factory} to create a new element
     * when the pool is empty.
     * @param maxSize The maximum number of elements in the pool.
     * @return An newly created <tt>Pool</tt>.
     * @see #synchronizedPool(Pool)
     */
    public static <T> Pool<T> newPool(Factory<T> factory, int maxSize) {
        DebugUtils.__checkError(factory == null, "factory == null");
        return new SimplePool<T>(factory, maxSize);
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
        return new ArrayPool<T>(maxSize, length, componentType);
    }

    /**
     * Creates a new <b>fixed-size</b> {@link Animator} {@link Pool}.
     * @param animation The initial property animation.
     * @param maxSize The maximum number of animators in the pool.
     * @return An newly <tt>Animator Pool</tt>.
     * @see #newPool(Context, int, int)
     */
    public static Pool<Animator> newPool(Animator animation, int maxSize) {
        return new AnimatorPool(animation, maxSize);
    }

    /**
     * Creates a new <b>fixed-size</b> {@link Animator} {@link Pool}.
     * @param context The <tt>Context</tt>.
     * @param resId The resource id of the property animation to load.
     * @param maxSize The maximum number of animators in the pool.
     * @return An newly <tt>Animator Pool</tt>.
     * @see #newPool(Animator, int)
     */
    public static Pool<Animator> newPool(Context context, int resId, int maxSize) {
        return new AnimatorPool(AnimatorInflater.loadAnimator(context, resId), maxSize);
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
        } else if (pool instanceof SimplePool) {
            ((SimplePool<?>)pool).dump(printer, null);
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
         * Retrieves a {@link Rect} from this <tt>Pool</tt>. Allows us to avoid
         * allocating new <tt>Rect</tt> in many cases. When the <tt>Rect</tt>
         * can no longer be used, The caller should be call {@link #recycle(Rect)}
         * to recycles the <tt>Rect</tt>.
         * @return The <tt>Rect</tt> object.
         * @see #obtain(int, int, int, int)
         */
        public final Rect obtain() {
            final Rect result = referent.getAndSet(null);
            return (result != null ? result : new Rect());
        }

        /**
         * Equivalent to calling<pre>
         * final Rect rect = obtain();
         * rect.set(left, top, right, bottom);</pre>
         * @see #obtain()
         */
        public final Rect obtain(int left, int top, int right, int bottom) {
            final Rect result = sInstance.obtain();
            result.set(left, top, right, bottom);
            return result;
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
         * Retrieves a {@link RectF} from this <tt>Pool</tt>. Allows us to avoid
         * allocating new <tt>RectF</tt> in many cases. When the <tt>RectF</tt>
         * can no longer be used, The caller should be call {@link #recycle(RectF)}
         * to recycles the <tt>RectF</tt>.
         * @return The <tt>RectF</tt> object.
         * @see #obtain(float, float, float, float)
         */
        public final RectF obtain() {
            final RectF result = referent.getAndSet(null);
            return (result != null ? result : new RectF());
        }

        /**
         * Equivalent to calling<pre>
         * final RectF rect = obtain();
         * rect.set(left, top, right, bottom);</pre>
         * @see #obtain()
         */
        public final RectF obtain(float left, float top, float right, float bottom) {
            final RectF result = sInstance.obtain();
            result.set(left, top, right, bottom);
            return result;
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

        /**
         * Retrieves a {@link Matrix} from this <tt>Pool</tt>. Allows us to avoid
         * allocating new <tt>Matrix</tt> in many cases. When the <tt>Matrix</tt>
         * can no longer be used, The caller should be call {@link #recycle(Matrix)}
         * to recycles the <tt>Matrix</tt>.
         * @return The <tt>Matrix</tt> object.
         */
        public final Matrix obtain() {
            final Matrix result = referent.getAndSet(null);
            return (result != null ? result : new Matrix());
        }
    }

    /**
     * Class <tt>ByteArrayPool</tt> for managing a pool of byte arrays.
     */
    public static final class ByteArrayPool {
        public static final Pool<byte[]> sInstance = new SynchronizedPool<byte[]>(new ArrayPool<byte[]>(2, 8192, byte.class));
    }

    /**
     * Class <tt>AbstractPool</tt> is an implementation of a {@link Pool}.
     */
    private static abstract class AbstractPool<T> {
        /* package */ final AtomicReference<T> referent;

        /**
         * Constructor
         */
        public AbstractPool() {
            this.referent = new AtomicReference<T>();
        }

        /**
         * Recycles the specified <em>element</em> to this <tt>Pool</tt>. After
         * calling this function you must not ever touch the <em>element</em> again.
         * @param element The element to recycle.
         */
        public final void recycle(T element) {
            referent.compareAndSet(null, element);
        }
    }

    /**
     * Class <tt>SimplePool</tt> is an implementation of a {@link Pool}.
     */
    private static class SimplePool<T> implements Pool<T>, Factory<T> {
        /* package */ int size;
        /* package */ final Object[] elements;
        /* package */ final Factory<T> factory;

        /**
         * Constructor
         * @param factory The {@link Factory} to create
         * a new element when this pool is empty.
         * @param maxSize The maximum number of elements
         * to allow in this pool.
         */
        public SimplePool(Factory<T> factory, int maxSize) {
            DebugUtils.__checkError(maxSize <= 0, "maxSize <= 0");
            this.elements = new Object[maxSize];
            this.factory  = (factory != null ? factory : this);
        }

        @Override
        public void clear() {
            if (size > 0) {
                Arrays.fill(elements, 0, size, null);
                size = 0;
            }
        }

        @Override
        public T newInstance() {
            throw new RuntimeException("Must be implementation!");
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
    }

    /**
     * Class <tt>ArrayPool</tt> is an implementation of a {@link Pool}.
     */
    private static final class ArrayPool<T> extends SimplePool<T> {
        private final int length;
        private final Class<?> componentType;

        /**
         * Constructor
         * @param maxSize The maximum number of arrays to allow in this pool.
         * @param length The maximum number of elements in the each array.
         * @param componentType The array's component type.
         */
        public ArrayPool(int maxSize, int length, Class<?> componentType) {
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
     * Class <tt>AnimatorPool</tt> is an implementation of a {@link Pool}.
     */
    private static final class AnimatorPool extends SimplePool<Animator> implements AnimatorListener {
        private final Animator animation;

        /**
         * Constructor
         * @param animation The initial property animation.
         * @param maxSize The maximum number of animators to allow in this pool.
         */
        public AnimatorPool(Animator animation, int maxSize) {
            super(null, maxSize);
            this.animation = animation;
        }

        @Override
        public Animator newInstance() {
            final Animator animation = this.animation.clone();
            animation.addListener(this);
            return animation;
        }

        @Override
        public void recycle(Animator animation) {
            animation.setTarget(null);
            super.recycle(animation);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            recycle(animation);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            recycle(animation);
        }

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
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
            if (pool instanceof SimplePool) {
                ((SimplePool<?>)pool).dump(printer, SynchronizedPool.class.getSimpleName());
            }
        }
    }

    /**
     * This class cannot be instantiated.
     */
    private Pools() {
    }
}