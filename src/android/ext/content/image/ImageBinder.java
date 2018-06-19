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
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.ext.util.DebugUtils;
import android.ext.util.StringUtils;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Printer;
import android.view.View;
import android.widget.ImageView;

/**
 * Class <tt>ImageBinder</tt> allows to bind the image to the {@link ImageView}.
 * <h2>Usage</h2>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;[ ImageBinder | TransitionBinder | binder ]
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:namespace="http://schemas.android.com/apk/res/<em>packageName</em>"
 *     class="classFullName"
 *     android:duration="@android:integer/config_longAnimTime"
 *     namespace:defaultImage="@drawable/ic_placeholder"
 *     namespace:maxCacheSize="128" &gt;
 *
 * &lt;Transformer
 *     android:name="[ rectangle | roundRect | drawable | oval | gif | classFullName ]"
 *     android:topLeftRadius="10dp"
 *     android:topRightRadius="10dp"
 *     android:bottomLeftRadius="10dp"
 *     android:bottomRightRadius="10dp"
 *     android:radius="20dp" /&gt;
 *
 * &lt;!-- Optional Transformer --&gt;
 * &lt;Transformer
 *     android:name="<em>classFullName</em>"
 *     namespace:attributes1="value1"
 *     namespace:attributes2="value2"
 *     ... ... /&gt;
 * &lt;/[ ImageBinder | TransitionBinder | binder ]&gt;</pre>
 * @author Garfield
 * @version 3.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ImageBinder<URI, Image> implements Binder<URI, Object, Image> {
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
     * @see #ImageBinder(Cache, Transformer, Drawable)
     * @see #inflateAttributes(Context, AttributeSet)
     */
    public ImageBinder(Context context, AttributeSet attrs) {
        inflateAttributes(context, attrs);
        final TypedArray a = context.obtainStyledAttributes(attrs, IMAGE_BINDER_ATTRS);
        final int maxSize  = a.getInt(1 /* R.styleable.ImageBinder_maxCacheSize */, 0);
        mDefaultImage = a.getDrawable(0 /* R.styleable.ImageBinder_defaultImage */);
        mTransformer  = inflateTransformer(context, (XmlPullParser)attrs, attrs, maxSize);
        a.recycle();
    }

    /**
     * Constructor
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the drawables.
     * @param transformer The {@link Transformer} to be used transforms an image to a <tt>Drawable</tt>.
     * @param defaultImage May be <tt>null</tt>. The <tt>Drawable</tt> to be used when the image is loading.
     * @see #ImageBinder(Context, AttributeSet)
     */
    public ImageBinder(Cache<URI, Drawable> imageCache, Transformer<URI, Image> transformer, Drawable defaultImage) {
        mDefaultImage = defaultImage;
        mTransformer  = (imageCache != null ? new CacheTransformer<URI, Image>(imageCache, transformer) : transformer);
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

    /**
     * Returns a copy of this binder. The returned binder will be share the drawable cache
     * with this binder.
     * @param defaultImage May be <tt>null</tt>. The default image of the returned binder.
     * @return A copy of this binder.
     */
    public ImageBinder<URI, Image> copy(Drawable defaultImage) {
        return new ImageBinder<URI, Image>(null, mTransformer, defaultImage);
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
     * @param attrs The base set of attribute values.
     * @param maxSize The maximum number of drawables to allow in the cache.
     * @return The <tt>Transformer</tt>.
     */
    protected Transformer<URI, Image> inflateTransformer(Context context, XmlPullParser parser, AttributeSet attrs, int maxSize) {
        try {
            // Inflates the bitmap transformer from XML parser.
            Transformer transformer = Transformer.inflate(context, parser, attrs);
            if (transformer == null) {
                transformer = BitmapTransformer.getInstance();
            }

            // Inflates the image transformer from XML parser.
            final Transformer imageTransformer = Transformer.inflate(context, parser, attrs);
            if (imageTransformer != null) {
                transformer = new ImageTransformer(transformer, imageTransformer);
            }

            return (Transformer<URI, Image>)(maxSize > 0 ? new CacheTransformer(new SimpleLruCache<Object, Drawable>(maxSize), transformer) : transformer);
        } catch (Exception e) {
            throw new IllegalArgumentException(parser.getPositionDescription() + ": Couldn't inflate transformer from xml", e);
        }
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
        DebugUtils.__checkError(IMAGE_BINDER_ATTRS != null, "The attributes has already initialized.");
        IMAGE_BINDER_ATTRS = attrs;
    }

    /* package */ final void dump(Context context, Printer printer) {
        if (mTransformer instanceof CacheTransformer) {
            Caches.dumpCache(((CacheTransformer)mTransformer).mImageCache, context, printer);
        }
    }

    /* package */ static void __checkBinder(Class clazz, Cache imageCache, Binder binder) {
        if (imageCache == null && binder instanceof ImageBinder) {
            final Transformer transformer = ((ImageBinder)binder).mTransformer;
            if (transformer instanceof CacheTransformer) {
                Log.e(clazz.getName(), "WARNING", new IllegalArgumentException("The " + clazz.getSimpleName() + " has no memory cache, The internal binder should be no drawable cache!!!"));
            }
        }
    }

    /**
     * Class <tt>Transformer</tt> used to transforms an image to a {@link Drawable}.
     */
    public static abstract class Transformer<URI, Image> {
        private static final int[] TRANSFORMER_ATTRS = new int[] { android.R.attr.name };

        /**
         * Transforms the <tt>Image</tt> to a <tt>Drawable</tt>.
         * @param uri The uri, passed earlier by {@link Binder#bindValue}.
         * @param target The target, passed earlier by {@link Binder#bindValue}.
         * @param image The image to convert, passed earlier by {@link Binder#bindValue}.
         * @return The <tt>Drawable</tt>.
         */
        public abstract Drawable transform(URI uri, Object target, Image image);

        /**
         * Inflates a new {@link Transformer} object from a xml resource.
         * @param context The <tt>Context</tt>.
         * @param parser The XML parser from which to inflate the transformer.
         * @param attrs The base set of attribute values.
         * @return The <tt>Transformer</tt>.
         */
        public static Transformer inflate(Context context, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException, ReflectiveOperationException {
            // Moves to the start tag position.
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
                // Empty loop
            }

            // No start tag found
            if (type != XmlPullParser.START_TAG) {
                return null;
            }

            if (!"Transformer".equals(parser.getName())) {
                throw new XmlPullParserException(parser.getPositionDescription() + ": The tag name must be 'Transformer'");
            }

            final TypedArray a = context.obtainStyledAttributes(attrs, TRANSFORMER_ATTRS);
            final String name  = a.getString(0 /* android.R.attr.name */);
            a.recycle();

            return createTransformer(context, attrs, name);
        }

        /**
         * Creates a new {@link Transformer} object from a xml resource.
         * @param context The <tt>Context</tt>.
         * @param attrs The base set of attribute values.
         * @param name May be <tt>null</tt>. The name of the <tt>Transformer</tt>.
         * @return The <tt>Transformer</tt>.
         */
        private static Transformer createTransformer(Context context, AttributeSet attrs, String name) throws ReflectiveOperationException {
            if (StringUtils.getLength(name) <= 0 || "rectangle".equals(name)) {
                return BitmapTransformer.sInstance;
            }

            switch (name) {
            case "gif":
                return GIFImageTransformer.sInstance;

            case "oval":
                return OvalBitmapTransformer.sInstance;

            case "drawable":
                return DrawableTransformer.sInstance;

            case "roundRect":
                return new RoundedBitmapTransformer(context, attrs);

            default:
                return (Transformer)Class.forName(name).getConstructor(Context.class, AttributeSet.class).newInstance(context, attrs);
            }
        }
    }

    /**
     * Class <tt>BitmapTransformer</tt> is an implementation of a {@link Transformer}.
     */
    public static final class BitmapTransformer extends Transformer<Object, Bitmap> {
        /* package */ static final BitmapTransformer sInstance = new BitmapTransformer();

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
     * Class <tt>OvalBitmapTransformer</tt> is an implementation of a {@link Transformer}.
     */
    public static final class OvalBitmapTransformer extends Transformer<Object, Bitmap> {
        /* package */ static final OvalBitmapTransformer sInstance = new OvalBitmapTransformer();

        /**
         * This class cannot be instantiated.
         */
        private OvalBitmapTransformer() {
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
     * Class <tt>RoundedBitmapTransformer</tt> is an implementation of a {@link Transformer}.
     */
    public static class RoundedBitmapTransformer<URI> extends Transformer<URI, Bitmap> {
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
         * @see #RoundedBitmapTransformer(Context, AttributeSet)
         * @see #RoundedBitmapTransformer(float, float, float, float)
         */
        public RoundedBitmapTransformer(float[] radii) {
            mRadii = radii;
        }

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         * @param attrs The attributes of the XML tag that is inflating the data.
         * @see #RoundedBitmapTransformer(float[])
         * @see #RoundedBitmapTransformer(float, float, float, float)
         */
        public RoundedBitmapTransformer(Context context, AttributeSet attrs) {
            mRadii = XmlResources.loadCornerRadii(context.getResources(), attrs);
        }

        /**
         * Constructor
         * @param topLeftRadius The top-left corner radius.
         * @param topRightRadius The top-right corner radius.
         * @param bottomLeftRadius The bottom-left corner radius.
         * @param bottomRightRadius The bottom-right corner radius.
         * @see #RoundedBitmapTransformer(float[])
         * @see #RoundedBitmapTransformer(Context, AttributeSet)
         */
        public RoundedBitmapTransformer(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
            mRadii = new float[] { topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius };
        }

        @Override
        public Drawable transform(URI uri, Object target, Bitmap bitmap) {
            return new RoundedBitmapDrawable(bitmap, mRadii);
        }
    }

    /**
     * Class <tt>GIFImageTransformer</tt> is an implementation of a {@link Transformer}.
     */
    public static final class GIFImageTransformer extends Transformer<Object, GIFImage> {
        /* package */ static final GIFImageTransformer sInstance = new GIFImageTransformer();

        /**
         * This class cannot be instantiated.
         */
        private GIFImageTransformer() {
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
    public static final class DrawableTransformer extends Transformer<Object, Drawable> {
        /* package */ static final DrawableTransformer sInstance = new DrawableTransformer();

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
    private static final class ImageTransformer extends Transformer {
        private final Transformer mImageTransformer;
        private final Transformer mBitmapTransformer;

        /**
         * Constructor
         * @param bitmapTransformer The {@link Transformer} used to transforms a <tt>Bitmap</tt> to a <tt>Drawable</tt>.
         * @param imageTransformer The {@link Transformer} used to transforms an image to a <tt>Drawable</tt>.
         */
        public ImageTransformer(Transformer bitmapTransformer, Transformer imageTransformer) {
            mImageTransformer  = imageTransformer;
            mBitmapTransformer = bitmapTransformer;
        }

        @Override
        public Drawable transform(Object uri, Object target, Object image) {
            return (image instanceof Bitmap ? mBitmapTransformer.transform(uri, target, image) : mImageTransformer.transform(uri, target, image));
        }
    }

    /**
     * Class <tt>CacheTransformer</tt> is an implementation of a {@link Transformer}.
     */
    private static final class CacheTransformer<URI, Image> extends Transformer<URI, Image> {
        public final Cache<URI, Drawable> mImageCache;
        public final Transformer<URI, Image> mTransformer;

        /**
         * Constructor
         * @param imageCache The {@link Cache} to store the drawables.
         * @param transformer The {@link Transformer} used to transforms an image to a <tt>Drawable</tt>.
         */
        public CacheTransformer(Cache<URI, Drawable> imageCache, Transformer<URI, Image> transformer) {
            mImageCache  = imageCache;
            mTransformer = transformer;
        }

        @Override
        public Drawable transform(URI uri, Object target, Image image) {
            Drawable drawable = mImageCache.get(uri);
            if (drawable == null) {
                mImageCache.put(uri, drawable = mTransformer.transform(uri, target, image));
            }

            return drawable;
        }
    }
}
