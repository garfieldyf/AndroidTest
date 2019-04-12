package android.ext.graphics.drawable;

import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.ext.graphics.GIFImage;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.Keep;
import android.util.AttributeSet;

/**
 * Class GIFDrawable
 * @author Garfield
 */
public class GIFDrawable extends AbsBitmapDrawable<GIFDrawable.GIFImageState> implements Runnable, Animatable {
    private static final int[] GIF_DRAWABLE_ATTRS = {
        android.R.attr.oneshot,
    };

    private static final int FLAG_RUNNING = 0x01;   // mFlags
    private static final int FLAG_SCHED   = 0x02;   // mFlags
    private static final int FLAG_ONESHOT = 0x01;   // mState.mFlags

    /**
     * The current frame index to draw.
     */
    private int mFrameIndex;

    /**
     * The {@link AnimationCallback} of this drawable.
     */
    private AnimationCallback mCallback;

    /**
     * Decodes a {@link GIFDrawable} from the <tt>Resources</tt>.
     * @param res The resource containing the GIF data.
     * @param id The resource id to be decoded.
     * @return The <tt>GIFDrawable</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decode(byte[], int, int)
     * @see #decode(InputStream, byte[])
     * @see #decode(Context, Object, byte[])
     */
    public static GIFDrawable decode(Resources res, int id) {
        final GIFImage image = GIFImage.decode(res, id);
        return (image != null ? new GIFDrawable(image) : null);
    }

    /**
     * Decodes a {@link GIFDrawable} from the <tt>InputStream</tt>.
     * @param is The <tt>InputStream</tt> containing the GIF data.
     * @param tempStorage May be <tt>null</tt>. The temporary storage to use for decoding. Suggest 16K.
     * @return The <tt>GIFDrawable</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decode(Resources, int)
     * @see #decode(byte[], int, int)
     * @see #decode(Context, Object, byte[])
     */
    public static GIFDrawable decode(InputStream is, byte[] tempStorage) {
        final GIFImage image = GIFImage.decode(is, tempStorage);
        return (image != null ? new GIFDrawable(image) : null);
    }

    /**
     * Decodes a {@link GIFDrawable} from the byte array.
     * @param data The byte array containing the GIF data.
     * @param offset The starting offset of the <em>data</em>.
     * @param length The number of bytes of the <em>data</em>, beginning at offset.
     * @return The <tt>GIFDrawable</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decode(Resources, int)
     * @see #decode(InputStream, byte[])
     * @see #decode(Context, Object, byte[])
     */
    public static GIFDrawable decode(byte[] data, int offset, int length) {
        final GIFImage image = GIFImage.decode(data, offset, length);
        return (image != null ? new GIFDrawable(image) : null);
    }

    /**
     * Decodes a {@link GIFDrawable} from the specified <em>uri</em>.
     * <h3>Accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android_asset ({@link #SCHEME_FILE})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param context The <tt>Context</tt>.
     * @param uri The uri to decode.
     * @param tempStorage May be <tt>null</tt>. The temporary storage to use for decoding. Suggest 16K.
     * @return The <tt>GIFDrawable</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decode(Resources, int)
     * @see #decode(byte[], int, int)
     * @see #decode(InputStream, byte[])
     */
    public static GIFDrawable decode(Context context, Object uri, byte[] tempStorage) {
        final GIFImage image = GIFImage.decode(context, uri, tempStorage);
        return (image != null ? new GIFDrawable(image) : null);
    }

    /**
     * Constructor
     * <p>The default constructor used by {@link Resources#getDrawable(int)}.</p>
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
     * @return <tt>true</tt> if the animation will play
     * once, <tt>false</tt> otherwise.
     * @see #setOneShot(boolean)
     */
    public boolean isOneShot() {
        return ((mState.mFlags & FLAG_ONESHOT) != 0);
    }

    /**
     * Sets whether the animation should play once or repeat.
     * @param oneShot <tt>true</tt> if the animation should
     * only play once.
     * @see #isOneShot()
     */
    public void setOneShot(boolean oneShot) {
        if (oneShot) {
            mState.mFlags |= FLAG_ONESHOT;
        } else {
            mState.mFlags &= ~FLAG_ONESHOT;
        }
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
            mFrameIndex = 0;
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
        return mState.mImage.getWidth();
    }

    @Override
    public int getMinimumHeight() {
        return mState.mImage.getHeight();
    }

    @Override
    public int getIntrinsicWidth() {
        return mState.mImage.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mState.mImage.getHeight();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        final boolean changed = super.setVisible(visible, restart);
        if (changed && !visible) {
            unscheduleSelf();
        }

        return changed;
    }

    @Override
    protected GIFImageState copyConstantState() {
        return new GIFImageState(mState);
    }

    @Override
    protected void draw(Canvas canvas, RectF bounds, Paint paint) {
        if (mState.mImage.draw(mState.mCanvas, mFrameIndex)) {
            // Draws the GIF image current frame.
            canvas.drawBitmap(mState.mCanvas, null, bounds, paint);

            // Schedules the GIF image next frame.
            if (isRunning()) {
                if (isOneShot() && mFrameIndex == mState.mImage.getFrameCount() - 1) {
                    unscheduleSelf();
                } else if ((mFlags & FLAG_SCHED) == 0) {
                    mFlags |= FLAG_SCHED;
                    scheduleSelf(this, SystemClock.uptimeMillis() + mState.mImage.getFrameDelay(mFrameIndex));
                }
            }
        }
    }

    @Override
    protected void inflateAttributes(Resources res, XmlPullParser parser, AttributeSet attrs, Theme theme, int id) throws XmlPullParserException, IOException {
        final TypedArray a = res.obtainAttributes(attrs, GIF_DRAWABLE_ATTRS);
        if (a.getBoolean(0 /* android.R.attr.oneshot */, false)) {
            mState.mFlags |= FLAG_ONESHOT;
        }

        mState.setImage(GIFImage.decode(res, id));
        DebugUtils.__checkError(mState.mImage == null, parser.getPositionDescription() + ": The <" + parser.getName() + "> tag requires a valid 'src' attribute");
        a.recycle();
    }

    private void unscheduleSelf() {
        mFlags &= ~FLAG_RUNNING;
        final Callback callback = getCallback();
        if (callback != null) {
            callback.invalidateDrawable(this);
            callback.unscheduleDrawable(this, this);
        }

        // Dispatch the animation is end.
        if (mCallback != null) {
            mCallback.onAnimationEnd(this, mFrameIndex);
        }
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
    /* package */ static final class GIFImageState extends AbsBitmapDrawable.BaseConstantState {
        /* package */ Bitmap mCanvas;
        /* package */ GIFImage mImage;

        /**
         * Constructor
         * @param image The {@link GIFImage}.
         * @see #GIFImageState(GIFImageState)
         */
        public GIFImageState(GIFImage image) {
            setImage(image);
        }

        /**
         * Copy constructor
         * @param state The {@link GIFImageState}.
         * @see #GIFImageState(GIFImage)
         */
        public GIFImageState(GIFImageState state) {
            super(state);
            mImage  = state.mImage;
            mCanvas = state.mImage.createBitmapCanvas();
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

        /* package */ final void setImage(GIFImage image) {
            if (image != null) {
                mImage  = image;
                mCanvas = image.createBitmapCanvas();
            }
        }
    }

    /**
     * The <tt>AnimationCallback</tt> interface used to listen to the animation events.
     */
    public static interface AnimationCallback {
        /**
         * Called when the animation starts.
         * @param drawable The {@link GIFDrawable} started the animation.
         */
        void onAnimationStart(GIFDrawable drawable);

        /**
         * Called when the animation ends.
         * @param drawable The {@link GIFDrawable} finished the animation.
         * @param frameIndex The end frame index.
         */
        void onAnimationEnd(GIFDrawable drawable, int frameIndex);
    }
}
