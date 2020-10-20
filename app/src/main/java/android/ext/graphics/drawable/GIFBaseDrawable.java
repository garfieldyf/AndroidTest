package android.ext.graphics.drawable;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.ext.content.res.XmlResources;
import android.ext.graphics.GIFImage;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.os.SystemClock;
import android.util.AttributeSet;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Abstract class GIFBaseDrawable
 * @author Garfield
 */
public abstract class GIFBaseDrawable<T extends GIFBaseDrawable.GIFBaseState> extends ImageDrawable<T> implements Runnable, Animatable {
    /* ------------- mFlags 0x00FF0000 ------------- */
    private static final int FLAG_RUNNING = 0x00400000;
    private static final int FLAG_SCHED   = 0x00800000;

    /* ---------- mState.mFlags 0x00FF0000 ---------- */
    private static final int FLAG_ONESHOT    = 0x00400000;
    private static final int FLAG_AUTO_START = 0x00800000;

    /**
     * The current frame index to draw.
     */
    private int mFrameIndex;

    /**
     * The {@link AnimationCallback} of this drawable.
     */
    private AnimationCallback mCallback;

    /**
     * Constructor
     * @param state The {@link GIFBaseState}.
     */
    public GIFBaseDrawable(T state) {
        super(state);
    }

    /**
     * Returns the {@link GIFImage} used by this drawable to render.
     * @return The {@link GIFImage}.
     */
    public final GIFImage getImage() {
        return mState.mImage;
    }

    /**
     * Returns the number of frames of this drawable.
     * @return The frame count, must be >= 1.
     */
    public final int getFrameCount() {
        return mState.mImage.getFrameCount();
    }

    /**
     * Tests the animation should play once or repeat.
     * @return <tt>true</tt> if the animation will play once, <tt>false</tt> otherwise.
     * @see #setOneShot(boolean)
     */
    public boolean isOneShot() {
        return ((mState.mFlags & FLAG_ONESHOT) != 0);
    }

    /**
     * Sets whether the animation should play once or repeat.
     * @param oneShot <tt>true</tt> if the animation should only play once.
     * @see #isOneShot()
     */
    public void setOneShot(boolean oneShot) {
        mState.setFlags(oneShot, FLAG_ONESHOT);
    }

    /**
     * Tests the animation should auto play when this drawable to render.
     * @return <tt>true</tt> if the animation will auto play, <tt>false</tt> otherwise.
     * @see #setAutoStart(boolean)
     */
    public boolean isAutoStart() {
        return ((mState.mFlags & FLAG_AUTO_START) != 0);
    }

    /**
     * Sets whether the animation should auto play when this drawable to render.
     * @param autoStart <tt>true</tt> if the animation should auto play.
     * @see #isAutoStart()
     */
    public void setAutoStart(boolean autoStart) {
        mState.setFlags(autoStart, FLAG_AUTO_START);
    }

    public final void setAnimationCallback(AnimationCallback callback) {
        mCallback = callback;
    }

    @Override
    public void start() {
        if (!isRunning() && mState.mImage.getFrameCount() > 1) {
            mFlags = (mFlags | FLAG_RUNNING) & ~FLAG_SCHED;
            mFrameIndex = 0;
            invalidateSelf();

            // Dispatch the animation is start.
            if (mCallback != null) {
                mCallback.onAnimationStart(this);
            }
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            unscheduleSelf();
        }
    }

    @Override
    public boolean isRunning() {
        return ((mFlags & FLAG_RUNNING) != 0);
    }

    @Override
    public void run() {
        mFlags &= ~FLAG_SCHED;
        mFrameIndex = (mFrameIndex + 1) % mState.mImage.getFrameCount();
        invalidateSelf();
    }

    @Override
    public int getMinimumWidth() {
        return mState.mImage.width;
    }

    @Override
    public int getMinimumHeight() {
        return mState.mImage.height;
    }

    @Override
    public int getIntrinsicWidth() {
        return mState.mImage.width;
    }

    @Override
    public int getIntrinsicHeight() {
        return mState.mImage.height;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        final boolean changed = super.setVisible(visible, restart);
        if (visible) {
            if (isAutoStart()) start();
        } else {
            stop();
        }

        return changed;
    }

    @Override
    protected void draw(Canvas canvas, RectF bounds, Paint paint) {
        final boolean unschedule = ((mFlags & FLAG_SCHED) == 0);
        if (unschedule) {
            // Draws the GIF image current frame to bitmap canvas.
            DebugUtils.__checkStartMethodTracing();
            mState.mImage.draw(mState.mCanvas, mFrameIndex);
            DebugUtils.__checkStopMethodTracing(getClass().getName(), "nativeDraw frame = " + mFrameIndex);
        }

        // Draws the bitmap canvas to canvas.
        DebugUtils.__checkDebug(!unschedule, getClass().getName(), "Unschedule the GIF image frame " + mFrameIndex + " - do not call nativeDraw");
        drawFrame(canvas, mState.mCanvas, bounds, paint);

        // Schedules the GIF image next frame.
        if (isRunning()) {
            if (isOneShot() && mFrameIndex == mState.mImage.getFrameCount() - 1) {
                unscheduleSelf();
            } else if (unschedule) {
                mFlags |= FLAG_SCHED;
                scheduleSelf(this, SystemClock.uptimeMillis() + mState.mImage.getFrameDelay(mFrameIndex));
            }
        }
    }

    @Override
    protected void inflateAttributes(Resources res, XmlPullParser parser, AttributeSet attrs, Theme theme, int id) throws XmlPullParserException, IOException {
        final boolean[] results = XmlResources.loadAnimationAttrs(res, attrs);
        if (results[0] /* android.R.attr.oneshot */) {
            mState.mFlags |= FLAG_ONESHOT;
        }

        mState.initialize(GIFImage.decode(res, id));
        mState.setFlags(results[1] /* android.R.attr.autoStart */, FLAG_AUTO_START);
        DebugUtils.__checkError(mState.mImage == null, parser.getPositionDescription() + ": The <" + parser.getName() + "> tag requires a valid 'src' attribute");
    }

    /**
     * Draws the current frame of this GIF drawable in the <em>bounds</em>.
     * @param canvas The canvas to draw into.
     * @param frame The current frame to be drawn.
     * @param bounds The frame of the contents of this drawable.
     * @param paint The paint used to draw the frame of this drawable.
     */
    protected void drawFrame(Canvas canvas, Bitmap frame, RectF bounds, Paint paint) {
        canvas.drawBitmap(frame, null, bounds, paint);
    }

    /**
     * Sets the specified {@link GIFImage} to this drawable.
     */
    /* package */ final void setImage(GIFImage image) {
        if (mState.mImage != image) {
            mFlags = (mFlags | FLAG_BOUNDS) & ~FLAG_RUNNING;
            mState.setImage(image);
            mFrameIndex = 0;
            unscheduleSelf(this);
            DebugUtils.__checkDebug(true, getClass().getName(), "setImage() - " + image);
        }
    }

    private void unscheduleSelf() {
        mFlags &= ~FLAG_RUNNING;
        final Callback callback = getCallback();
        if (callback != null) {
            callback.invalidateDrawable(this);
            callback.unscheduleDrawable(this, this);
        }

        // Dispatch the animation is end.
        DebugUtils.__checkDebug(true, getClass().getName(), "The " + this + " is end.");
        if (mCallback != null) {
            mCallback.onAnimationEnd(this);
        }
    }

    /**
     * Class <tt>GIFBaseState</tt> is an implementation of a {@link ConstantState}.
     */
    /* package */ static abstract class GIFBaseState extends ImageDrawable.ImageState {
        /* package */ Bitmap mCanvas;
        /* package */ GIFImage mImage;

        /**
         * Constructor
         * @param image The {@link GIFImage}.
         * @see #GIFBaseState(GIFBaseState)
         */
        public GIFBaseState(GIFImage image) {
            mFlags = FLAG_AUTO_START;
            if (image != null) {
                initialize(image);
            }
        }

        /**
         * Copy constructor
         * @param state The {@link GIFBaseState}.
         * @see #GIFBaseState(GIFImage)
         */
        public GIFBaseState(GIFBaseState state) {
            super(state);
            initialize(state.mImage);
        }

        /* package */ void initialize(GIFImage image) {
            DebugUtils.__checkError(image == null, "Invalid parameter - image == null");
            mImage  = image;
            mCanvas = image.createBitmapCanvas();
        }

        /* package */ void setImage(GIFImage image) {
            DebugUtils.__checkError(image == null, "Invalid parameter - image == null");
            DebugUtils.__checkError(mCanvas == null, "mCanvas == null");
            mImage = image;
            if (image.getBitmapCanvasBytes() > mCanvas.getAllocationByteCount()) {
                mCanvas = image.createBitmapCanvas();
            } else {
                mCanvas.reconfigure(image.width, image.height, mCanvas.getConfig());
                mCanvas.eraseColor(Color.TRANSPARENT);
                DebugUtils.__checkDebug(true, getClass().getName(), "The bitmap canvas resize - width = " + image.width + ", height = " + image.height);
            }
        }
    }

    /**
     * The <tt>AnimationCallback</tt> interface used to listen to the animation events.
     */
    public static interface AnimationCallback {
        /**
         * Called when the animation starts.
         * @param drawable The {@link GIFBaseDrawable} started the animation.
         */
        void onAnimationStart(GIFBaseDrawable<?> drawable);

        /**
         * Called when the animation ends.
         * @param drawable The {@link GIFBaseDrawable} finished the animation.
         */
        void onAnimationEnd(GIFBaseDrawable<?> drawable);
    }
}
