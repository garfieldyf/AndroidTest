package android.ext.content.image;

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
import android.ext.content.XmlResources.XmlTransformerInflater;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Printer;
import android.util.Xml;
import android.view.View;
import android.widget.ImageView;

/**
 * Class <tt>ImageBinder</tt> allows to bind the image to the {@link ImageView}.
 * <h2>Usage</h2>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;[ ImageBinder | TransitionBinder | binder ]
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
 *         android:topLeftRadius="10dp"
 *         android:topRightRadius="10dp"
 *         android:bottomLeftRadius="10dp"
 *         android:bottomRightRadius="10dp"
 *         app:attributes1="value1"
 *         app:attributes2="value2" /&gt;
 *
 *     &lt;!-- Image Transformer (Optional) --&gt;
 *     &lt;[ GIFTransformer | DrawableTransformer | transformer ]
 *         class="classFullName"
 *         android:src="@xml/transformer2"
 *         app:attributes3="value3"
 *         app:attributes4="value4"
 *         ... ... /&gt;
 * &lt;/[ ImageBinder | TransitionBinder | binder ]&gt;</pre>
 * @author Garfield
 * @version 3.5
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ImageBinder<URI, Image> implements Binder<URI, Object, Image> {
    private static final int[] TRANSFORMER_ATTRS = {
        android.R.attr.src
    };

    private static int[] IMAGE_BINDER_ATTRS;

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
        DebugUtils.__checkError(IMAGE_BINDER_ATTRS == null, "The " + getClass().getName() + " did not call ImageBinder.initAttrs()");
        inflateAttributes(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, IMAGE_BINDER_ATTRS);
        final int maxSize  = a.getInt(1 /* R.styleable.ImageBinder_maxCacheSize */, 0);
        mDefaultImage = a.getDrawable(0 /* R.styleable.ImageBinder_defaultImage */);
        a.recycle();

        final Transformer transformer = inflateTransformer(context, (XmlPullParser)attrs);
        mTransformer = (maxSize > 0 ? new CacheTransformer(new SimpleLruCache<Object, Drawable>(maxSize), transformer) : transformer);
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link ImageBinder} from the specified <em>binder</em>. The returned binder will be
     * share the drawable cache with the <em>binder</em>.</p>
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
        mTransformer  = (imageCache != null ? new CacheTransformer((Cache<Object, Drawable>)imageCache, transformer) : transformer);
    }

    /**
     * Returns the default image associated with this binder.
     * @return The <tt>Drawable</tt>.
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
                transformer = BitmapTransformer.getInstance();
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

        return XmlTransformerInflater.inflateTransformer(context, parser);
    }

    /**
     * Initialize the {@link ImageBinder} styleable. <p>Note: This method recommended call in the
     * <tt>Application's</tt> static constructor.</p>
     * <p>Includes the following attributes:
     * <table><colgroup align="left" /><colgroup align="left" /><colgroup align="center" />
     * <tr><th>Attribute</th><th>Type</th><th>Index</th></tr>
     * <tr><td><tt>defaultImage</tt></td><td>reference</td><td>0</td></tr>
     * <tr><td><tt>maxCacheSize</tt></td><td>integer</td><td>1</td></tr>
     * </table></p>
     * @param attrs The <tt>R.styleable.ImageBinder</tt> styleable, as generated by the aapt tool.
     */
    public static void initAttrs(int[] attrs) {
        IMAGE_BINDER_ATTRS = attrs;
    }

    /* package */ final void dump(Context context, Printer printer) {
        if (mTransformer instanceof CacheTransformer) {
            Caches.dumpCache(((CacheTransformer)mTransformer).mImageCache, context, printer);
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
        private static final BitmapTransformer sInstance = new BitmapTransformer();

        /**
         * This class cannot be instantiated.
         */
        private BitmapTransformer() {
        }

        /**
         * Returns a type-safe {@link Transformer} to transforms a <tt>Bitmap</tt> to a {@link BitmapDrawable}.
         * @return The <tt>Transformer</tt>.
         */
        public static <URI> Transformer<URI, Bitmap> getInstance() {
            return (Transformer<URI, Bitmap>)sInstance;
        }

        @Override
        public Drawable transform(Object uri, Object target, Bitmap bitmap) {
            return new BitmapDrawable(((View)target).getResources(), bitmap);
        }
    }

    /**
     * Class <tt>OvalTransformer</tt> is an implementation of a {@link Transformer}.
     */
    public static final class OvalTransformer implements Transformer<Object, Bitmap> {
        private static final OvalTransformer sInstance = new OvalTransformer();

        /**
         * This class cannot be instantiated.
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
         * This class cannot be instantiated.
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
     * Class <tt>DrawableTransformer</tt> is an implementation of a {@link Transformer}.
     */
    public static final class DrawableTransformer implements Transformer<Object, Drawable> {
        private static final DrawableTransformer sInstance = new DrawableTransformer();

        /**
         * This class cannot be instantiated.
         */
        private DrawableTransformer() {
        }

        /**
         * Returns a type-safe {@link Transformer} to transforms a {@link Drawable} to a {@link Drawable}.
         * @return The <tt>Transformer</tt>.
         */
        public static <URI> Transformer<URI, Drawable> getInstance() {
            return (Transformer<URI, Drawable>)sInstance;
        }

        @Override
        public Drawable transform(Object uri, Object target, Drawable drawable) {
            return drawable;
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
            return (image instanceof Bitmap ? mBitmapTransformer.transform(uri, target, image) : mImageTransformer.transform(uri, target, image));
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
        public CacheTransformer(Cache<Object, Drawable> imageCache, Transformer transformer) {
            mImageCache  = imageCache;
            mTransformer = transformer;
        }

        @Override
        public Drawable transform(Object uri, Object target, Object image) {
            Drawable drawable = mImageCache.get(uri);
            if (drawable == null) {
                mImageCache.put(uri, drawable = mTransformer.transform(uri, target, image));
            }

            return drawable;
        }
    }
}
