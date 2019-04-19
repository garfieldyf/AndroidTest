package android.ext.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.ext.util.DebugUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Class ViewAnimatorUtils
 * @author Garfield
 */
public final class ViewAnimatorUtils {
    /**
     * Returns an {@link Animator} object, which can be used to animate on the <em>view</em>.
     * @param view The {@link View} whose will be animate.
     * @param resId The resource id of the property animation to load.
     * @return The <tt>Animator</tt> associated with the <em>view</em>.
     */
    public static Animator animate(View view, int resId) {
        Animator animator = (Animator)view.getTag(resId);
        if (animator == null) {
            animator = AnimatorInflater.loadAnimator(view.getContext(), resId);
            animator.setTarget(view);
            view.setTag(resId, animator);
            DebugUtils.__checkDebug(true, ViewAnimatorUtils.class.getSimpleName(), "loadAnimator resource id #0x" + Integer.toHexString(resId));
        }

        return animator;
    }

    /**
     * Returns an {@link Animation} object, which can be used to animate on the <em>view</em>.
     * @param view The {@link View} whose will be animate.
     * @param resId The resource id of the animation to load.
     * @return The <tt>Animation</tt> associated with the <em>view</em>.
     */
    public static Animation animation(View view, int resId) {
        Animation animation = (Animation)view.getTag(resId);
        if (animation == null) {
            animation = AnimationUtils.loadAnimation(view.getContext(), resId);
            view.setTag(resId, animation);
            DebugUtils.__checkDebug(true, ViewAnimatorUtils.class.getSimpleName(), "loadAnimation resource id #0x" + Integer.toHexString(resId));
        }

        return animation;
    }

    /**
     * This utility class cannot be instantiated.
     */
    private ViewAnimatorUtils() {
    }
}
