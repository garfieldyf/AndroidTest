package android.ext.graphics.drawable;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.ext.graphics.GIFImage;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;

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
