package android.ext.graphics.drawable;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;
import android.util.AttributeSet;
import android.widget.ImageView;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Class OvalBitmapDrawable
 * @author Garfield
 */
public class OvalBitmapDrawable extends ShapeBitmapDrawable<OvalBitmapDrawable.OvalBitmapState> {
    /**
     * The default constructor used by {@link Resources#getDrawable(int)},
     * do not call this method directly.
     * @hide
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
        invalidateSelf(mState.mShader, true);
        DebugUtils.__checkError(bitmap == null, "Invalid parameter - bitmap == null");
    }

    @Override
    protected ConstantState copyConstantState() {
        return new OvalBitmapState(mState);
    }

    @Override
    protected void getConvexPath(RectF bounds, Path outPath) {
        outPath.addOval(bounds, Direction.CW);
    }

    @Override
    protected void draw(Canvas canvas, RectF bounds, Paint paint) {
        canvas.drawPath(mState.mPath, paint);
    }

    @Override
    @TargetApi(21)
    protected void getOutline(Outline outline, RectF bounds) {
        outline.setOval((int)bounds.left, (int)bounds.top, (int)bounds.right, (int)bounds.bottom);
    }

    @Override
    protected void inflateAttributes(Resources res, XmlPullParser parser, AttributeSet attrs, Theme theme, int id) throws XmlPullParserException, IOException {
        super.inflateAttributes(res, parser, attrs, theme, id);
        invalidateSelf(mState.mShader, true);
    }

    /**
     * Constructor
     * <p>The constructor used by {@link ConstantState#newDrawable()}.</p>
     */
    /* package */ OvalBitmapDrawable(OvalBitmapState state) {
        super(state);
    }

    /**
     * Equivalent to calling <tt>setBitmap(view, view.getDrawable(), bitmap)</tt>.
     * @param view The <tt>ImageView</tt>. Never <tt>null</tt>.
     * @param bitmap The <tt>Bitmap</tt> to set. Never <tt>null</tt>.
     * @see #setBitmap(ImageView, Drawable, Bitmap)
     */
    public static void setBitmap(ImageView view, Bitmap bitmap) {
        DebugUtils.__checkError(view == null || bitmap == null, "Invalid parameters - view == null || bitmap == null");
        setBitmap(view, view.getDrawable(), bitmap);
    }

    /**
     * Sets a {@link Bitmap} as the content of the {@link ImageView}. This method
     * will be reuse the <em>view's</em> original <tt>OvalBitmapDrawable</tt>.
     * Allows us to avoid allocating new <tt>OvalBitmapDrawable</tt> in many cases.
     * @param view The <tt>ImageView</tt>. Never <tt>null</tt>.
     * @param origDrawable The <em>view's</em> original drawable or <tt>null</tt>.
     * @param bitmap The <tt>Bitmap</tt> to set. Never <tt>null</tt>.
     * @see #setBitmap(ImageView, Bitmap)
     */
    public static void setBitmap(ImageView view, Drawable origDrawable, Bitmap bitmap) {
        DebugUtils.__checkError(view == null || bitmap == null, "Invalid parameters - view == null || bitmap == null");
        if (origDrawable instanceof OvalBitmapDrawable) {
            ((OvalBitmapDrawable)origDrawable).setBitmap(bitmap);

            // Force update the ImageView's mDrawable.
            view.setImageDrawable(null);
            view.setImageDrawable(origDrawable);
        } else {
            view.setImageDrawable(new OvalBitmapDrawable(bitmap));
        }
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
