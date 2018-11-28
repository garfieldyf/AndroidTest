package android.ext.util;

import java.util.concurrent.atomic.AtomicReference;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorInflater;
import android.content.Context;
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
     * @return The newly created <tt>Pool</tt>.
     * @see #newPool(Factory, int)
     * @see #synchronizedPool(Pool)
     */
    public static <T> Pool<T> newSimplePool(Factory<T> factory) {
        return new SimplePool<T>(factory);
    }

    /**
     * Creates a new <b>fixed-size</b> {@link Pool}.
     * @param factory The {@link Factory} to create a new element
     * when the pool is empty.
     * @param maxSize The maximum number of elements in the pool.
     * @return The newly created <tt>Pool</tt>.
     * @see #newSimplePool(Factory)
     * @see #synchronizedPool(Pool)
     */
    public static <T> Pool<T> newPool(Factory<T> factory, int maxSize) {
        return new ArrayPool<T>(factory, maxSize);
    }

    /**
     * Creates a new <b>fixed-size</b> {@link Animator} {@link Pool}.
     * @param animation The initial property animation.
     * @param maxSize The maximum number of animators in the pool.
     * @return The newly created <tt>Pool</tt>.
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
     * @return The newly created <tt>Pool</tt>.
     * @see #newPool(Animator, int)
     */
    public static Pool<Animator> newPool(Context context, int resId, int maxSize) {
        return new AnimatorPool(AnimatorInflater.loadAnimator(context, resId), maxSize);
    }

    /**
     * Returns a wrapper on the specified {@link Pool} which synchronizes
     * all access to the pool.
     * @param pool The <tt>Pool</tt> to wrap in a synchronized pool.
     * @return A synchronized <tt>Pool</tt>.
     * @see #newSimplePool(Factory)
     * @see #newPool(Factory, int)
     */
    public static <T> Pool<T> synchronizedPool(Pool<T> pool) {
        return new SynchronizedPool<T>(pool);
    }

    /**
     * The <tt>Pool</tt> interface for managing a pool of objects.
     */
    public static interface Pool<T> {
        /**
         * Retrieves a new element from this <tt>Pool</tt>. Allows us to
         * avoid allocating new elements in many cases. When the element
         * can no longer be used, The caller should be call {@link #recycle(T)}
         * to recycles the element. When this <tt>Pool</tt> is empty, should
         * be call {@link Factory#newInstance()} to create a new element.
         * @return A newly element.
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
     * Class <tt>SimplePool</tt> is an implementation of a {@link Pool}.
     */
    private static class SimplePool<T> implements Pool<T> {
        private final Factory<T> factory;
        private final AtomicReference<T> referent;

        /**
         * Constructor
         * <p>Creates a new <b>one</b> size pool.</p>
         * @param factory The {@link Factory} to create
         * a new element when this pool is empty.
         */
        public SimplePool(Factory<T> factory) {
            this.factory  = factory;
            this.referent = new AtomicReference<T>();
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
         * @param maxSize The maximum number
         * of elements to allow in this pool.
         * @see #ArrayPool(Factory, int)
         */
        public ArrayPool(int maxSize) {
            DebugUtils.__checkError(maxSize <= 0, "maxSize <= 0");
            this.factory  = this;
            this.elements = new Object[maxSize];
        }

        /**
         * Constructor
         * <p>Creates a new pool.</p>
         * @param factory The {@link Factory} to create
         * a new element when this pool is empty.
         * @param maxSize The maximum number of elements
         * to allow in this pool.
         * @see #ArrayPool(int)
         */
        public ArrayPool(Factory<T> factory, int maxSize) {
            DebugUtils.__checkError(maxSize <= 0, "maxSize <= 0");
            this.factory  = factory;
            this.elements = new Object[maxSize];
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

        @Override
        public String toString() {
            return toString(getClass().getSimpleName());
        }

        public final String toString(String className) {
            return toString(new StringBuilder(96).append(className)
                .append(" [ size = ").append(size)
                .append(", maxSize = ").append(elements.length))
                .append(" ]").toString();
        }

        public final void dump(Printer printer, String className) {
            final StringBuilder result = new StringBuilder(96);
            DebugUtils.dumpSummary(printer, result, 80, " Dumping %s [ size = %d, maxSize = %d ] ", className, size, elements.length);
            for (int i = 0; i < size; ++i) {
                result.setLength(0);
                printer.println(result.append("  ").append(elements[i]).toString());
            }
        }

        private StringBuilder toString(StringBuilder result) {
            if (size > 0) {
                result.append(", elementType = ").append(elements[0].getClass().getSimpleName());
            }

            return result;
        }
    }

    /**
     * Class <tt>AnimatorPool</tt> is an implementation of a {@link Pool}.
     */
    private static final class AnimatorPool extends ArrayPool<Animator> implements AnimatorListener {
        private final Animator animation;

        /**
         * Constructor
         * @param animation The initial property animation.
         * @param maxSize The maximum number of animators to allow in this pool.
         */
        public AnimatorPool(Animator animation, int maxSize) {
            super(maxSize);
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

        public final void dump(Printer printer) {
            final StringBuilder result = new StringBuilder(96);
            DebugUtils.dumpSummary(printer, result, 80, " Dumping AnimatorPool [ size = %d, maxSize = %d ] ", size, elements.length);
            for (int i = 0; i < size; ++i) {
                result.setLength(0);
                printer.println(DebugUtils.toString(elements[i], result.append("  ")).toString());
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
        }

        @Override
        public synchronized T obtain() {
            return pool.obtain();
        }

        @Override
        public synchronized void recycle(T element) {
            pool.recycle(element);
        }

        @Override
        public synchronized String toString() {
            return (pool instanceof ArrayPool ? ((ArrayPool<?>)pool).toString("SynchronizedPool") : super.toString());
        }

        public synchronized final void dump(Printer printer) {
            if (pool instanceof ArrayPool) {
                ((ArrayPool<?>)pool).dump(printer, "SynchronizedPool");
            }
        }
    }

    public static void dumpPool(Pool<?> pool, Printer printer) {
        if (pool instanceof AnimatorPool) {
            ((AnimatorPool)pool).dump(printer);
        } else if (pool instanceof ArrayPool) {
            ((ArrayPool<?>)pool).dump(printer, "ArrayPool");
        } else if (pool instanceof SynchronizedPool) {
            ((SynchronizedPool<?>)pool).dump(printer);
        }
    }

    /**
     * This class cannot be instantiated.
     */
    private Pools() {
    }
}