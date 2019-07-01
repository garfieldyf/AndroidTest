package android.ext.graphics.drawable;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.annotation.TargetApi;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Keep;
import android.util.AttributeSet;

/**
 * Class OvalBitmapDrawable
 * @author Garfield
 */
public class OvalBitmapDrawable extends ShapeBitmapDrawable<OvalBitmapDrawable.OvalBitmapState> {
    /**
     * Constructor
     * <p>The default constructor used by {@link Resources#getDrawable(int)}.</p>
     */
    @Keep
    public OvalBitmapDrawable() {
        super(new OvalBitmapState((Bitmap)null));
    }

    /**
     * Constructor
     * @param bitmap The {@link Bitmap}. Never <tt>null</tt>.
     */
    public OvalBitmapDrawable(Bitmap bitmap) {
        super(new OvalBitmapState(bitmap));
        invalidateSelf(true);
    }

    @Override
    public Drawable mutate() {
        if ((mFlags & FLAG_MUTATED) == 0) {
            mFlags |= FLAG_MUTATED;
            mState = new OvalBitmapState(mState);
        }

        return this;
    }

    @Override
    protected void getConvexPath(RectF bounds, Path outPath) {
        outPath.addOval(bounds, Direction.CW);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void getOutline(Outline outline, RectF bounds) {
        outline.setOval((int)bounds.left, (int)bounds.top, (int)bounds.right, (int)bounds.bottom);
    }

    @Override
    protected void inflateAttributes(Resources res, XmlPullParser parser, AttributeSet attrs, Theme theme, int id) throws XmlPullParserException, IOException {
        super.inflateAttributes(res, parser, attrs, theme, id);
        invalidateSelf(true);
    }

    /**
     * Constructor
     * <p>The constructor used by {@link ConstantState#newDrawable()}.</p>
     */
    /* package */ OvalBitmapDrawable(OvalBitmapState state) {
        super(state);
    }

    /**
     * Class <tt>OvalBitmapState</tt> is an implementation of a {@link ConstantState}.
     */
    /* package */ static final class OvalBitmapState extends ShapeBitmapDrawable.BitmapState {
        /**
         * Constructor
         * @param bitmap The {@link Bitmap}.
         * @see #OvalBitmapState(OvalBitmapState)
         */
        public OvalBitmapState(Bitmap bitmap) {
            super(bitmap);
        }

        /**
         * Copy constructor
         * @param state The {@link OvalBitmapState}.
         * @see #OvalBitmapState(Bitmap)
         */
        public OvalBitmapState(OvalBitmapState state) {
            super(state);
        }

        @Override
        public Drawable newDrawable() {
            return new OvalBitmapDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new OvalBitmapDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res, Theme theme) {
            return new OvalBitmapDrawable(this);
        }
    }
}
