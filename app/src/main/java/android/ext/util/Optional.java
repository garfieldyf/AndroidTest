package android.ext.util;

import android.ext.cache.BitmapPool;
import android.ext.cache.Cache;
import android.ext.cache.Caches;

/**
 * Class Optional
 * @author Garfield
 */
public final class Optional {
    /**
     * Returns a {@link Cancelable} from the specified <em>cancelable</em>,
     * if non-null. Otherwise returns an empty <tt>Cancelable</tt>.
     */
    public static Cancelable ofNullable(Cancelable cancelable) {
        return (cancelable != null ? cancelable : EmptyCancelable.sInstance);
    }

    /**
     * Returns a {@link Cache} from the specified <em>cache</em>,
     * if non-null. Otherwise returns an empty <tt>Cache</tt>.
     * @see Caches#emptyCache()
     */
    public static <K, V> Cache<K, V> ofNullable(Cache<K, V> cache) {
        return (cache != null ? cache : Caches.emptyCache());
    }

    /**
     * Returns a {@link BitmapPool} from the specified <em>bitmapPool</em>,
     * if non-null. Otherwise returns an empty <tt>BitmapPool</tt>.
     * @see Caches#emptyBitmapPool()
     */
    public static BitmapPool ofNullable(BitmapPool bitmapPool) {
        return (bitmapPool != null ? bitmapPool : Caches.emptyBitmapPool());
    }

    /**
     * Class <tt>EmptyCancelable</tt> is an implementation of a {@link Cancelable}.
     */
    private static final class EmptyCancelable implements Cancelable {
        public static final Cancelable sInstance = new EmptyCancelable();

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }
    }

//    /**
//     * Class <tt>EmptyDrawable</tt> is an implementation of a {@link Drawable}.
//     */
//    private static final class EmptyDrawable extends Drawable {
//        public static final Drawable sInstance = new EmptyDrawable();
//
//        @Override
//        public int getOpacity() {
//            return PixelFormat.TRANSPARENT;
//        }
//
//        @Override
//        public void draw(Canvas canvas) {
//        }
//
//        @Override
//        public int getMinimumWidth() {
//            return 0;
//        }
//
//        @Override
//        public int getMinimumHeight() {
//            return 0;
//        }
//
//        @Override
//        public void setAlpha(int alpha) {
//        }
//
//        @Override
//        public boolean setState(int[] stateSet) {
//            return false;
//        }
//
//        @Override
//        public void invalidateSelf() {
//        }
//
//        @Override
//        public void unscheduleSelf(Runnable what) {
//        }
//
//        @Override
//        public void scheduleSelf(Runnable what, long when) {
//        }
//
//        @Override
//        public void setBounds(Rect bounds) {
//        }
//
//        @Override
//        public void setBounds(int left, int top, int right, int bottom) {
//        }
//
//        @Override
//        public void setTint(int tint) {
//        }
//
//        @Override
//        public void setTintList(ColorStateList tint) {
//        }
//
//        @Override
//        public void clearColorFilter() {
//        }
//
//        @Override
//        public void setColorFilter(ColorFilter filter) {
//        }
//
//        @Override
//        public void setColorFilter(int color, Mode mode) {
//        }
//
//        @Override
//        public boolean setVisible(boolean visible, boolean restart) {
//            return false;
//        }
//
//        @Override
//        public void inflate(Resources res, XmlPullParser parser, AttributeSet attrs) {
//        }
//
//        @Override
//        public void inflate(Resources res, XmlPullParser parser, AttributeSet attrs, Theme theme) {
//        }
//    }

    /**
     * This utility class cannot be instantiated.
     */
    private Optional() {
    }
}
