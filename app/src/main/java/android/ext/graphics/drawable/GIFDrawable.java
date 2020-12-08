package android.ext.graphics.drawable;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.ext.graphics.GIFImage;
import android.ext.util.DebugUtils;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;
import android.widget.ImageView;

/**
 * Class GIFDrawable
 * @author Garfield
 */
public class GIFDrawable extends GIFBaseDrawable<GIFDrawable.GIFImageState> {
    /**
     * The default constructor used by {@link Resources#getDrawable(int)},
     * do not call this method directly.
     * @hide
     */
    @Keep
    public GIFDrawable() {
        super(new GIFImageState((GIFImage)null));
    }

    /**
     * Constructor
     * @param image The {@link GIFImage}. Never <tt>null</tt>.
     */
    public GIFDrawable(GIFImage image) {
        super(new GIFImageState(image));
        DebugUtils.__checkError(image == null, "Invalid parameter - image == null");
    }

    @Override
    protected ConstantState copyConstantState() {
        return new GIFImageState(mState);
    }

    /**
     * Constructor
     * <p>The constructor used by {@link ConstantState#newDrawable()}.</p>
     */
    /* package */ GIFDrawable(GIFImageState state) {
        super(state);
    }

    /**
     * Equivalent to calling <tt>setImage(view, image, true, false)</tt>.
     * @param view The <tt>ImageView</tt>. Never <tt>null</tt>.
     * @param image The <tt>GIFImage</tt> to set. Never <tt>null</tt>.
     * @see #setImage(ImageView, GIFImage, boolean, boolean)
     */
    public static void setImage(ImageView view, GIFImage image) {
        DebugUtils.__checkError(view == null || image == null, "Invalid parameters - view == null || image == null");
        setImage(view, image, true, false);
    }

    /**
     * Sets a {@link GIFImage} as the content of the {@link ImageView}. This
     * method will be reuse the <em>view's</em> original <tt>GIFDrawable</tt>.
     * Allows us to avoid allocating new <tt>GIFDrawable</tt> in many cases.
     * @param view The <tt>ImageView</tt>. Never <tt>null</tt>.
     * @param image The <tt>GIFImage</tt> to set. Never <tt>null</tt>.
     * @param autoStart <tt>true</tt> if the animation should auto play.
     * @param oneShot <tt>true</tt> if the animation should only play once.
     * @see #setImage(ImageView, GIFImage)
     */
    public static void setImage(ImageView view, GIFImage image, boolean autoStart, boolean oneShot) {
        DebugUtils.__checkError(view == null || image == null, "Invalid parameters - view == null || image == null");
        final Drawable origDrawable = view.getDrawable();
        final GIFDrawable drawable;
        if (origDrawable instanceof GIFDrawable) {
            drawable = (GIFDrawable)origDrawable;
            drawable.setImage(image);

            // Force update the ImageView's mDrawable.
            view.setImageDrawable(null);
        } else {
            drawable = new GIFDrawable(image);
        }

        drawable.setOneShot(oneShot);
        drawable.setAutoStart(autoStart);
        view.setImageDrawable(drawable);
    }

    /**
     * Class <tt>GIFImageState</tt> is an implementation of a {@link ConstantState}.
     */
    /* package */ static final class GIFImageState extends GIFBaseDrawable.GIFBaseState {
        /**
         * Constructor
         * @param image The {@link GIFImage}.
         * @see #GIFImageState(GIFImageState)
         */
        public GIFImageState(GIFImage image) {
            super(image);
        }

        /**
         * Copy constructor
         * @param state The {@link GIFImageState}.
         * @see #GIFImageState(GIFImage)
         */
        public GIFImageState(GIFImageState state) {
            super(state);
        }

        @Override
        public Drawable newDrawable() {
            return new GIFDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new GIFDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res, Theme theme) {
            return new GIFDrawable(this);
        }
    }
}
