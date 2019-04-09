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
import android.ext.image.transformer.BitmapTransformer;
import android.ext.image.transformer.ImageTransformer;
import android.ext.image.transformer.Transformer;
import android.ext.util.ClassUtils;
import android.graphics.drawable.Animatable;
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
 *     app:maxCacheSize="128"
 *     app:attribute1="value1"
 *     app:attribute2="value2"
 *     ... ... &gt;
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
            ((CacheTransformer)mTransformer).dump(context, printer);
        }
    }

    @Override
    public void bindValue(URI uri, Object[] params, Object target, Image value, int state) {
        final ImageView view = (ImageView)target;
        if (value == null) {
            view.setImageDrawable(mDefaultImage);
        } else {
            final Drawable drawable = mTransformer.transform(uri, value);
            view.setImageDrawable(drawable);
            if (drawable instanceof Animatable) {
                ((Animatable)drawable).start();
            }
        }
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
                transformer = ImageTransformer.create(transformer, imageTransformer);
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
     * Class <tt>CacheTransformer</tt> is an implementation of a {@link Transformer}.
     */
    private static final class CacheTransformer implements Transformer {
        private final Transformer mTransformer;
        private final Cache<Object, Drawable> mImageCache;

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
        public Drawable transform(Object uri, Object image) {
            Drawable drawable;
            if (image instanceof Drawable) {
                drawable = (Drawable)image;
            } else if ((drawable = mImageCache.get(uri)) == null) {
                mImageCache.put(uri, drawable = mTransformer.transform(uri, image));
            }

            return drawable;
        }

        public final void dump(Context context, Printer printer) {
            Caches.dumpCache(mImageCache, context, printer);
        }
    }
}
