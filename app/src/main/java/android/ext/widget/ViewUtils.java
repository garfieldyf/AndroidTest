package android.ext.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Class ViewUtils
 * @author Garfield
 */
public final class ViewUtils {
    /**
     * Returns an {@link Animator} object, which can be used to animate on the <em>view</em>.
     * @param view The {@link View} whose will be animate.
     * @param resId The resource id of the property animation.
     * @return The <tt>Animator</tt> associated with the <em>view</em>.
     */
    public static Animator animate(View view, int resId) {
        Animator animator = (Animator)view.getTag(resId);
        if (animator == null) {
            animator = AnimatorInflater.loadAnimator(view.getContext(), resId);
            animator.setTarget(view);
            view.setTag(resId, animator);
        }

        return animator;
    }

    /**
     * Start the specified animation now.
     * @param view The view whose play the animation.
     * @param resId The resource id of the property animation.
     * @param invalidateParent Whether the <em>view's</em> parent should be invalidated as well.
     */
    public static void startAnimation(View view, int resId, boolean invalidateParent) {
        if (invalidateParent) {
            final View parent = (View)view.getParent();
            if (parent != null) {
                parent.invalidate();
            }
        }

        ViewUtils.animate(view, resId).start();
    }

    /**
     * Returns the index of the child to draw for this iteration.
     * @param container The <tt>ViewGroup</tt> whose child to draw.
     * @param childCount The number of child to draw.
     * @param i The current iteration.
     * @return The index of the child to draw this iteration.
     */
    public static int getChildDrawingOrder(ViewGroup container, int childCount, int i) {
        final View focused = container.getFocusedChild();
        if (focused != null) {
            if (container.getChildAt(i) == focused) {
                // Move the focused child order to last.
                return childCount - 1;
            } else if (i == childCount - 1) {
                // Move the last child order to the focused child order.
                return container.indexOfChild(focused);
            }
        }

        return i;
    }

    /**
     * This utility class cannot be instantiated.
     */
    private ViewUtils() {
    }
}
