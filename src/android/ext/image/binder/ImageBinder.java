package android.ext.image.binder;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.cache.Cache;
import android.ext.cache.Caches;
import android.ext.cache.SimpleLruCache;
import android.ext.content.AsyncLoader.Binder;
import android.ext.content.XmlResources;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.ext.util.ClassUtils;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Printer;
import android.util.Xml;
import android.widget.ImageView;

/**
 * Class <tt>ImageBinder</tt> allows to bind the image to the {@link ImageView}.
 * <h2>Usage</h2>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;[ ImageBinder | TransitionBinder | BackgroundBinder | binder ]
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     class="classFullName"
 *     android:duration="@android:integer/config_longAnimTime"
 *     app:defaultImage="@drawable/ic_placeholder"
 *     app:maxCacheSize="128" &gt;
 *
 *     &lt;!-- Bitmap Transformer --&gt;
 *     &lt;[ BitmapTransformer | OvalTransformer | RoundedRectTransformer | transformer ]
 *         class="classFullName"
 *         android:src="@xml/transformer1"
 *         android:radius="20dp"
 *         android:topLeftRadius="20dp"
 *         android:topRightRadius="20dp"
 *         android:bottomLeftRadius="20dp"
 *         android:bottomRightRadius="20dp"
 *         app:attribute1="value1"
 *         app:attribute2="value2"
 *         ... ... /&gt;
 *
 *     &lt;!-- Image Transformer (Optional) --&gt;
 *     &lt;[ GIFTransformer | transformer ]
 *         class="classFullName"
 *         android:src="@xml/transformer2"
 *         app:attribute1="value1"
 *         app:attribute2="value2"
 *         ... ... /&gt;
 * &lt;/[ ImageBinder | TransitionBinder | BackgroundBinder | binder ]&gt;</pre>
 * @author Garfield
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ImageBinder<URI, Image> implements Binder<URI, Object, Image> {
    private static final int[] TRANSFORMER_ATTRS = {
        android.R.attr.src
    };

    /**
     * The <tt>Drawable</tt> to be used when the image is loading.
     */
    protected final Drawable mDefaultImage;

    /**
     * The {@link Transformer} to be used transforms an image to a <tt>Drawable</tt>.
     */
    protected final Transformer<URI, Image> mTransformer;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The base set of attribute values.
     * @see #ImageBinder(ImageBinder, Drawable)
     * @see #ImageBinder(Cache, Transformer, Drawable)
     * @see #inflateAttributes(Context, AttributeSet)
     */
    public ImageBinder(Context context, AttributeSet attrs) {
        inflateAttributes(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, (int[])ClassUtils.getAttributeValue(context, "ImageBinder"));
        final int maxSize  = a.getInt((int)ClassUtils.getAttributeValue(context, "ImageBinder_maxCacheSize"), 0);
        mDefaultImage = a.getDrawable((int)ClassUtils.getAttributeValue(context, "ImageBinder_defaultImage"));
        a.recycle();

        final Transformer transformer = inflateTransformer(context, (XmlPullParser)attrs);
        mTransformer = (maxSize > 0 ? new CacheTransformer(new SimpleLruCache(maxSize), transformer) : transformer);
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link ImageBinder} from the specified <em>binder</em>. The returned binder will be
     * share the drawable cache and transformer with the <em>binder</em>.</p>
     * @param binder The <tt>ImageBinder</tt> to copy.
     * @param defaultImage May be <tt>null</tt>. The <tt>Drawable</tt> to be used when the image is loading.
     * @see #ImageBinder(Context, AttributeSet)
     * @see #ImageBinder(Cache, Transformer, Drawable)
     */
    public ImageBinder(ImageBinder<URI, Image> binder, Drawable defaultImage) {
        mDefaultImage = defaultImage;
        mTransformer  = binder.mTransformer;
    }

    /**
     * Constructor
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the drawables.
     * @param transformer The {@link Transformer} to be used transforms an image to a <tt>Drawable</tt>.
     * @param defaultImage May be <tt>null</tt>. The <tt>Drawable</tt> to be used when the image is loading.
     * @see #ImageBinder(Context, AttributeSet)
     * @see #ImageBinder(ImageBinder, Drawable)
     */
    public ImageBinder(Cache<URI, Drawable> imageCache, Transformer<URI, Image> transformer, Drawable defaultImage) {
        mDefaultImage = defaultImage;
        mTransformer  = (imageCache != null ? new CacheTransformer(imageCache, transformer) : transformer);
    }

    /**
     * Returns the default image associated with this binder.
     * @return The <tt>Drawable</tt> or <tt>null</tt>.
     */
    public final Drawable getDefaultImage() {
        return mDefaultImage;
    }

    /**
     * Returns the {@link Transformer} associated with this binder.
     * @return The <tt>Transformer</tt>.
     */
    public final Transformer<URI, Image> getTransformer() {
        return mTransformer;
    }

    public void dump(Context context, Printer printer) {
        if (mTransformer instanceof CacheTransformer) {
            Caches.dumpCache(((CacheTransformer)mTransformer).mImageCache, context, printer);
        }
    }

    @Override
    public void bindValue(URI uri, Object[] params, Object target, Image value, int state) {
        ((ImageView)target).setImageDrawable(value != null ? mTransformer.transform(uri, target, value) : mDefaultImage);
    }

    /**
     * Inflates this binder's attributes from an XML resource.
     * @param context The <tt>Context</tt>.
     * @param attrs The base set of attribute values.
     */
    protected void inflateAttributes(Context context, AttributeSet attrs) {
    }

    /**
     * Inflates a new {@link Transformer} object from a xml resource.
     * @param context The <tt>Context</tt>.
     * @param parser The XML parser to parsing the XML data.
     * @return The <tt>Transformer</tt>.
     */
    protected Transformer<URI, Image> inflateTransformer(Context context, XmlPullParser parser) {
        try {
            // Inflates the bitmap transformer from XML parser.
            Transformer transformer = inflate(context, parser);
            if (transformer == null) {
                transformer = BitmapTransformer.getInstance(context);
            }

            // Inflates the image transformer from XML parser.
            final Transformer imageTransformer = inflate(context, parser);
            if (imageTransformer != null) {
                transformer = new ImageTransformer(transformer, imageTransformer);
            }

            return transformer;
        } catch (Exception e) {
            throw new IllegalArgumentException(parser.getPositionDescription() + ": Couldn't inflate transformer from xml", e);
        }
    }

    private static Transformer inflate(Context context, XmlPullParser parser) throws XmlPullParserException, IOException, ReflectiveOperationException {
        // Moves to the start tag position.
        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
            // Empty loop
        }

        if (type != XmlPullParser.START_TAG) {
            return null;
        }

        if ("transformer".equals(parser.getName())) {
            final TypedArray a = context.obtainStyledAttributes(Xml.asAttributeSet(parser), TRANSFORMER_ATTRS);
            final int id = a.getResourceId(0 /* android.R.attr.src */, 0);
            a.recycle();

            if (id != 0) {
                return XmlResources.loadTransformer(context, id);
            }
        }

        return XmlResources.inflateTransformer(context, parser);
    }

    /**
     * Called on the <tt>ImageLoader</tt> internal, do not call this method directly.
     */
    public static void __checkTransformer(Class<?> clazz, Cache<?, ?> imageCache, Binder<?, ?, ?> binder) {
        if (imageCache == null && binder instanceof ImageBinder && ((ImageBinder<?, ?>)binder).mTransformer instanceof CacheTransformer) {
            Log.e(clazz.getName(), "WARNING: The " + clazz.getSimpleName() + " has no memory cache, The binder should be no drawable cache!!!");
        }
    }

    /**
     * Class <tt>Transformer</tt> used to transforms an image to a {@link Drawable}.
     */
    public static interface Transformer<URI, Image> {
        /**
         * Transforms the <tt>Image</tt> to a <tt>Drawable</tt>.
         * @param uri The uri, passed earlier by <tt>ImageBinder.bindValue</tt>.
         * @param target The target, passed earlier by <tt>ImageBinder.bindValue</tt>.
         * @param image The image to convert, passed earlier by <tt>ImageBinder.bindValue</tt>.
         * @return The <tt>Drawable</tt>.
         */
        Drawable transform(URI uri, Object target, Image image);
    }

    /**
     * Class <tt>BitmapTransformer</tt> is an implementation of a {@link Transformer}.
     */
    public static final class BitmapTransformer implements Transformer<Object, Bitmap> {
        private static BitmapTransformer sInstance;
        private final Context mContext;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         */
        private BitmapTransformer(Context context) {
            mContext = context.getApplicationContext();
        }

        /**
         * Returns a type-safe {@link Transformer} to transforms a <tt>Bitmap</tt> to a {@link BitmapDrawable}.
         * @param context The <tt>Context</tt>.
         * @return The <tt>Transformer</tt>.
         */
        public static synchronized <URI> Transformer<URI, Bitmap> getInstance(Context context) {
            if (sInstance == null) {
                sInstance = new BitmapTransformer(context);
            }

            return (Transformer<URI, Bitmap>)sInstance;
        }

        @Override
        public Drawable transform(Object uri, Object target, Bitmap bitmap) {
            return new BitmapDrawable(mContext.getResources(), bitmap);
        }
    }

    /**
     * Class <tt>OvalTransformer</tt> is an implementation of a {@link Transformer}.
     */
    public static final class OvalTransformer implements Transformer<Object, Bitmap> {
        private static final OvalTransformer sInstance = new OvalTransformer();

        /**
         * Constructor
         */
        private OvalTransformer() {
        }

        /**
         * Returns a type-safe {@link Transformer} to transforms a <tt>Bitmap</tt> to an {@link OvalBitmapDrawable}.
         * @return The <tt>Transformer</tt>.
         */
        public static <URI> Transformer<URI, Bitmap> getInstance() {
            return (Transformer<URI, Bitmap>)sInstance;
        }

        @Override
        public Drawable transform(Object uri, Object target, Bitmap bitmap) {
            return new OvalBitmapDrawable(bitmap);
        }
    }

    /**
     * Class <tt>RoundedRectTransformer</tt> is an implementation of a {@link Transformer}.
     */
    public static class RoundedRectTransformer<URI> implements Transformer<URI, Bitmap> {
        /**
         * The corner radii, array of 8 values. Each corner receives two
         * radius values [X, Y]. The corners are ordered <tt>top-left</tt>,
         * <tt>top-right</tt>, <tt>bottom-right</tt>, <tt>bottom-left</tt>.
         */
        protected final float[] mRadii;

        /**
         * Constructor
         * @param radii The corner radii, array of 8 values. Each corner receives two radius values [X, Y]. The
         * corners are ordered <tt>top-left</tt>, <tt>top-right</tt>, <tt>bottom-right</tt>, <tt>bottom-left</tt>.
         * @see #RoundedRectTransformer(Context, AttributeSet)
         * @see #RoundedRectTransformer(float, float, float, float)
         */
        public RoundedRectTransformer(float[] radii) {
            mRadii = radii;
        }

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         * @param attrs The attributes of the XML tag that is inflating the data.
         * @see #RoundedRectTransformer(float[])
         * @see #RoundedRectTransformer(float, float, float, float)
         */
        public RoundedRectTransformer(Context context, AttributeSet attrs) {
            mRadii = XmlResources.loadCornerRadii(context.getResources(), attrs);
        }

        /**
         * Constructor
         * @param topLeftRadius The top-left corner radius.
         * @param topRightRadius The top-right corner radius.
         * @param bottomLeftRadius The bottom-left corner radius.
         * @param bottomRightRadius The bottom-right corner radius.
         * @see #RoundedRectTransformer(float[])
         * @see #RoundedRectTransformer(Context, AttributeSet)
         */
        public RoundedRectTransformer(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
            mRadii = new float[] { topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius };
        }

        @Override
        public Drawable transform(URI uri, Object target, Bitmap bitmap) {
            return new RoundedBitmapDrawable(bitmap, mRadii);
        }
    }

    /**
     * Class <tt>GIFTransformer</tt> is an implementation of a {@link Transformer}.
     */
    public static final class GIFTransformer implements Transformer<Object, GIFImage> {
        private static final GIFTransformer sInstance = new GIFTransformer();

        /**
         * Constructor
         */
        private GIFTransformer() {
        }

        /**
         * Returns a type-safe {@link Transformer} to transforms a {@link GIFImage} to a {@link GIFDrawable}.
         * @return The <tt>Transformer</tt>.
         */
        public static <URI> Transformer<URI, GIFImage> getInstance() {
            return (Transformer<URI, GIFImage>)sInstance;
        }

        @Override
        public Drawable transform(Object uri, Object target, GIFImage image) {
            return new GIFDrawable(image);
        }
    }

    /**
     * Class <tt>ImageTransformer</tt> is an implementation of a {@link Transformer}.
     */
    public static final class ImageTransformer implements Transformer {
        private final Transformer mImageTransformer;
        private final Transformer mBitmapTransformer;

        /**
         * Constructor
         * @param bitmapTransformer The {@link Transformer} used to transforms a <tt>Bitmap</tt> to a <tt>Drawable</tt>.
         * @param imageTransformer The {@link Transformer} used to transforms an image to a <tt>Drawable</tt>.
         */
        /* package */ ImageTransformer(Transformer bitmapTransformer, Transformer imageTransformer) {
            mImageTransformer  = imageTransformer;
            mBitmapTransformer = bitmapTransformer;
        }

        /**
         * Returns an image {@link Transformer} with the specified <em>bitmapTransformer</em> and <em>imageTransformer</em>.
         * @param bitmapTransformer The {@link Transformer} used to transforms a <tt>Bitmap</tt> to a <tt>Drawable</tt>.
         * @param imageTransformer The {@link Transformer} used to transforms an image to a <tt>Drawable</tt>.
         * @return An image {@link Transformer} object.
         */
        public static <URI, Image> Transformer<URI, Object> create(Transformer<URI, Bitmap> bitmapTransformer, Transformer<URI, Image> imageTransformer) {
            return new ImageTransformer(bitmapTransformer, imageTransformer);
        }

        @Override
        public Drawable transform(Object uri, Object target, Object image) {
            if (image instanceof Drawable) {
                return (Drawable)image;
            } else if (image instanceof Bitmap) {
                return mBitmapTransformer.transform(uri, target, image);
            } else {
                return mImageTransformer.transform(uri, target, image);
            }
        }
    }

    /**
     * Class <tt>CacheTransformer</tt> is an implementation of a {@link Transformer}.
     */
    /* package */ static final class CacheTransformer implements Transformer {
        /* package */ final Transformer mTransformer;
        /* package */ final Cache<Object, Drawable> mImageCache;

        /**
         * Constructor
         * @param imageCache The {@link Cache} to store the drawables.
         * @param transformer The {@link Transformer} used to transforms an image to a <tt>Drawable</tt>.
         */
        public CacheTransformer(Cache imageCache, Transformer transformer) {
            mImageCache  = imageCache;
            mTransformer = transformer;
        }

        @Override
        public Drawable transform(Object uri, Object target, Object image) {
            Drawable drawable;
            if (image instanceof Drawable) {
                drawable = (Drawable)image;
            } else if ((drawable = mImageCache.get(uri)) == null) {
                mImageCache.put(uri, drawable = mTransformer.transform(uri, target, image));
            }

            return drawable;
        }
    }
}
