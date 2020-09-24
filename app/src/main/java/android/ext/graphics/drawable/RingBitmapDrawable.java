package android.ext.graphics.drawable;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.ext.content.res.XmlResources;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Path.FillType;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;
import android.util.AttributeSet;
import android.widget.ImageView;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Class RingBitmapDrawable
 * @author Garfield
 */
public class RingBitmapDrawable extends ShapeBitmapDrawable<RingBitmapDrawable.RingBitmapState> {
    /**
     * The default constructor used by {@link Resources#getDrawable(int)},
     * do not call this method directly.
     * @hide
     */
    @Keep
    public RingBitmapDrawable() {
        super(new RingBitmapState(null, 0));
    }

    /**
     * Constructor
     * @param bitmap The {@link Bitmap}. Never <tt>null</tt>.
     * @param innerRadius The inner radius of the ring.
     */
    public RingBitmapDrawable(Bitmap bitmap, float innerRadius) {
        super(new RingBitmapState(bitmap, innerRadius));
        invalidateSelf(mState.mShader, true);
    }

    /**
     * Return the inner radius of the ring of this drawable.
     * @return The inner radius of the ring.
     * @see #setInnerRadius(float)
     */
    public float getInnerRadius() {
        return mState.mInnerRadius;
    }

    /**
     * Sets the inner radius to be applied when drawing the bitmap.
     * @param innerRadius The inner radius of the ring.
     * @see #getInnerRadius()
     */
    public void setInnerRadius(float innerRadius) {
        if (mState.mInnerRadius != innerRadius) {
            mState.mInnerRadius = innerRadius;
            invalidateSelf(mState.mShader, true);
        }
    }

    @Override
    protected ConstantState copyConstantState() {
        return new RingBitmapState(mState);
    }

    @Override
    protected void getConvexPath(RectF bounds, Path outPath) {
        outPath.setFillType(FillType.EVEN_ODD);
        outPath.addOval(bounds, Direction.CW);
        final float outerRadius = Math.min(bounds.width(), bounds.height()) * 0.5f;
        if (mState.mInnerRadius > 0f && mState.mInnerRadius < outerRadius) {
            outPath.addCircle(bounds.centerX(), bounds.centerY(), mState.mInnerRadius, Direction.CW);
        }
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
        mState.mInnerRadius = XmlResources.loadInnerRadius(res, attrs);
        invalidateSelf(mState.mShader, true);
    }

    /**
     * Constructor
     * <p>The constructor used by {@link ConstantState#newDrawable()}.</p>
     */
    /* package */ RingBitmapDrawable(RingBitmapState state) {
        super(state);
    }

    /**
     * Equivalent to calling <tt>setBitmap(view, view.getDrawable(), bitmap, innerRadius)</tt>.
     * @param view The <tt>ImageView</tt>. Never <tt>null</tt>.
     * @param bitmap The <tt>Bitmap</tt> to set. Never <tt>null</tt>.
     * @param innerRadius The inner radius of the ring.
     * @see #setBitmap(ImageView, Drawable, Bitmap, float)
     */
    public static void setBitmap(ImageView view, Bitmap bitmap, float innerRadius) {
        DebugUtils.__checkError(view == null || bitmap == null, "Invalid parameters - view == null || bitmap == null");
        setBitmap(view, view.getDrawable(), bitmap, innerRadius);
    }

    /**
     * Sets a {@link Bitmap} as the content of the {@link ImageView}. This method
     * will be reuse the <em>view's</em> original <tt>RingBitmapDrawable</tt>.
     * Allows us to avoid allocating new <tt>RingBitmapDrawable</tt> in many cases.
     * @param view The <tt>ImageView</tt>. Never <tt>null</tt>.
     * @param origDrawable The <em>view's</em> original drawable or <tt>null</tt>.
     * @param bitmap The <tt>Bitmap</tt> to set. Never <tt>null</tt>.
     * @param innerRadius The inner radius of the ring.
     * @see #setBitmap(ImageView, Bitmap, float)
     */
    public static void setBitmap(ImageView view, Drawable origDrawable, Bitmap bitmap, float innerRadius) {
        DebugUtils.__checkError(view == null || bitmap == null, "Invalid parameters - view == null || bitmap == null");
        if (origDrawable instanceof RingBitmapDrawable) {
            final RingBitmapDrawable drawable = (RingBitmapDrawable)origDrawable;
            drawable.setBitmap(bitmap);
            drawable.setInnerRadius(innerRadius);

            // Force update the ImageView's mDrawable.
            view.setImageDrawable(null);
            view.setImageDrawable(drawable);
        } else {
            view.setImageDrawable(new RingBitmapDrawable(bitmap, innerRadius));
        }
    }

    /**
     * Class <tt>RingBitmapState</tt> is an implementation of a {@link ConstantState}.
     */
    /* package */ static final class RingBitmapState extends ShapeBitmapDrawable.BitmapState {
        /* package */ float mInnerRadius;

        /**
         * Constructor
         * @param bitmap The {@link Bitmap}.
         * @param innerRadius The inner radius of the ring.
         * @see #RingBitmapState(RingBitmapState)
         */
        public RingBitmapState(Bitmap bitmap, float innerRadius) {
            super(bitmap);
            mInnerRadius = innerRadius;
        }

        /**
         * Copy constructor
         * @param state The {@link RingBitmapState}.
         * @see #RingBitmapState(Bitmap, float)
         */
        public RingBitmapState(RingBitmapState state) {
            super(state);
            mInnerRadius = state.mInnerRadius;
        }

        @Override
        public Drawable newDrawable() {
            return new RingBitmapDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new RingBitmapDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res, Theme theme) {
            return new RingBitmapDrawable(this);
        }
    }
}
