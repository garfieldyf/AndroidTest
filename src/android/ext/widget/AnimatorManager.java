package android.ext.widget;

import android.animation.Animator;
import android.content.Context;
import android.ext.util.Pools;
import android.ext.util.Pools.Pool;
import android.util.Printer;

/**
 * Class AnimatorManager
 * @author Garfield
 * @version 1.0
 */
public final class AnimatorManager {
    private final Pool<Animator> mInAnimations;
    private final Pool<Animator> mOutAnimations;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param inResId The enter resource id of the property animation to load.
     * @param outResId The exit resource id of the property animation to load.
     * @param maxSize The max size of the internal animation pool.
     * @see #AnimatorManager(Pool, Pool)
     * @see #AnimatorManager(Animator, Animator, int)
     */
    public AnimatorManager(Context context, int inResId, int outResId, int maxSize) {
        mInAnimations  = Pools.newPool(context, inResId, maxSize);
        mOutAnimations = Pools.newPool(context, outResId, maxSize);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param inAnimation The initial enter property animation.
     * @param outAnimation The initial exit property animation.
     * @param maxSize The max size of the internal animation pool.
     * @see #AnimatorManager(Pool, Pool)
     * @see #AnimatorManager(Context, int, int, int)
     */
    public AnimatorManager(Animator inAnimation, Animator outAnimation, int maxSize) {
        mInAnimations  = Pools.newPool(inAnimation, maxSize);
        mOutAnimations = Pools.newPool(outAnimation, maxSize);
    }

    /**
     * Constructor
     * @param inAnimations The enter animations.
     * @param outAnimations The exit animations.
     * @see #AnimatorManager(Context, int, int, int)
     * @see #AnimatorManager(Animator, Animator, int)
     */
    public AnimatorManager(Pool<Animator> inAnimations, Pool<Animator> outAnimations) {
        mInAnimations  = inAnimations;
        mOutAnimations = outAnimations;
    }

    /**
     * Starts the specified animation now.
     * @param target The object will be animated.
     * @param enter Whether to enters the screen.
     */
    public void startAnimation(Object target, boolean enter) {
        final Animator animation = (enter ? mInAnimations.obtain() : mOutAnimations.obtain());
        animation.setTarget(target);
        animation.start();
    }

    /**
     * Retrieves a new {@link Animator} from this manager.
     * @param enter Whether to retrieve the enter animation.
     * @return The newly <tt>Animator</tt>.
     */
    public Animator obtainAnimation(boolean enter) {
        return (enter ? mInAnimations.obtain() : mOutAnimations.obtain());
    }

    public final void dump(Printer printer) {
        Pools.dumpPool(mInAnimations, printer);
        Pools.dumpPool(mOutAnimations, printer);
    }
}
