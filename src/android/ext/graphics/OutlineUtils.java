package android.ext.graphics;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Outline;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * Class OutlineUtils
 * @author Garfield
 * @version 1.0
 */
@SuppressLint("NewApi")
public final class OutlineUtils {
    /**
     * Class <tt>RoundedRectBounds</tt> is an implementation of a {@link ViewOutlineProvider}.
     */
    public static final class RoundedRectBounds extends ViewOutlineProvider {
        private final float mRadius;

        /**
         * Constructor
         * @param radius The rounded rect corner radius.
         * @see #RoundedRectBounds(Resources, int)
         */
        public RoundedRectBounds(float radius) {
            mRadius = radius;
        }

        /**
         * Constructor
         * @param res The {@link Resources}.
         * @param id The resource id of the dimension of the radius.
         * @see #RoundedRectBounds(float)
         */
        public RoundedRectBounds(Resources res, int id) {
            mRadius = res.getDimension(id);
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mRadius);
        }
    }

    /**
     * The outline of the <tt>View</tt> to match its rectangular bounds.
     */
    public static final ViewOutlineProvider OVAL_BOUNDS = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setOval(0, 0, view.getWidth(), view.getHeight());
        }
    };

    /**
     * The outline of the <tt>View</tt> to match its rectangular bounds and
     * the corner radius is <code>Math.min(viewWidth, viewHeight) / 2</code>.
     */
    public static final ViewOutlineProvider ROUNDED_RECT_BOUNDS = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            final int width = view.getWidth(), height = view.getHeight();
            outline.setRoundRect(0, 0, width, height, Math.min((float)width, height) * 0.5f);
        }
    };

    /**
     * This utility class cannot be instantiated.
     */
    private OutlineUtils() {
    }
}
