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
import android.ext.content.res.XmlResources;
import android.ext.image.transformer.BitmapTransformer;
import android.ext.image.transformer.ImageTransformer;
import android.ext.image.transformer.Transformer;
import android.ext.util.ClassUtils;
import android.ext.util.DebugUtils;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Printer;
import android.util.Xml;
import android.widget.ImageView;

/**
 * Class <tt>ImageBinder</tt> allows to bind the image to the {@link ImageView}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;[ ImageBinder | binder ]
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     class="classFullName"
 *     app:defaultImage="@drawable/ic_placeholder"
 *     app:maxCacheSize="128"
 *     app:attribute1="value1"
 *     app:attribute2="value2"
 *     ... ... &gt;
 *
 *     &lt;!--
 *         The bitmap transformer (Optional), See transformer class
 *         Example: &lt;BitmapTransformer /&gt;
 *     --&gt;
 *
 *     &lt;!--
 *         The image transformer (Optional), See transformer class
 *         Example: &lt;GIFTransformer /&gt;
 *     --&gt;
 * &lt;/[ ImageBinder | binder ]&gt;</pre>
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
    protected Transformer<URI, Image> mTransformer;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The base set of attribute values.
     * @see #ImageBinder(Cache, Transformer, Drawable)
     * @see #inflateAttributes(Context, AttributeSet)
     */
    public ImageBinder(Context context, AttributeSet attrs) {
        inflateAttributes(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, (int[])ClassUtils.getFieldValue(context, "ImageBinder"));
        final int maxSize  = a.getInt((int)ClassUtils.getFieldValue(context, "ImageBinder_maxCacheSize"), 0);
        mDefaultImage = a.getDrawable((int)ClassUtils.getFieldValue(context, "ImageBinder_defaultImage"));
        a.recycle();

        final Transformer transformer = inflateTransformer(context, (XmlPullParser)attrs);
        mTransformer = (maxSize > 0 ? new CacheTransformer(new SimpleLruCache(maxSize), transformer) : transformer);
    }

    /**
     * Constructor
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the drawables.
     * @param transformer The {@link Transformer} to be used transforms an image to a <tt>Drawable</tt>.
     * @param defaultImage May be <tt>null</tt>. The <tt>Drawable</tt> to be used when the image is loading.
     * @see #ImageBinder(Context, AttributeSet)
     */
    public ImageBinder(Cache<URI, Drawable> imageCache, Transformer<URI, Image> transformer, Drawable defaultImage) {
        DebugUtils.__checkError(imageCache != null && transformer instanceof CacheTransformer, "Cannot create: the transformer has an image cache (an ImageBinder only has one image cache)");
        mDefaultImage = defaultImage;
        mTransformer  = (imageCache != null ? new CacheTransformer(imageCache, transformer) : transformer);
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link ImageBinder} from the specified <em>binder</em>. The returned binder will be
     * share the internal drawable cache and the transformer with the <em>binder</em>.</p>
     * @param binder The <tt>ImageBinder</tt> to copy.
     * @param defaultImage May be <tt>null</tt>. The <tt>Drawable</tt> to be used when the image is loading.
     * @see #ImageBinder(ImageBinder, Transformer)
     */
    public ImageBinder(ImageBinder<URI, Image> binder, Drawable defaultImage) {
        this(null, binder.mTransformer, defaultImage);
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link ImageBinder} from the specified <em>binder</em>. The returned binder will
     * be share the internal drawable cache and the default image with the <em>binder</em>.</p>
     * @param binder The <tt>ImageBinder</tt> to copy.
     * @param transformer The {@link Transformer} to be used transforms an image to a <tt>Drawable</tt>.
     * @see #ImageBinder(ImageBinder, Drawable)
     */
    public ImageBinder(ImageBinder<URI, Image> binder, Transformer<URI, Image> transformer) {
        this(binder.getImageCache(), transformer, binder.mDefaultImage);
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

    /**
     * Returns the image cache associated with this binder.
     * @return The {@link Cache} or <tt>null</tt>.
     */
    public final Cache<URI, Drawable> getImageCache() {
        return (mTransformer instanceof CacheTransformer ? ((CacheTransformer)mTransformer).mImageCache : null);
    }

    public void dump(Context context, Printer printer) {
        DebugUtils.dumpSummary(printer, new StringBuilder(120), 120, " Dumping Transformer ", (Object[])null);
        printer.println("  " + mTransformer.getClass().getName());

        if (mTransformer instanceof CacheTransformer) {
            Caches.dumpCache(((CacheTransformer)mTransformer).mImageCache, context, printer);
        }
    }

    @Override
    public void bindValue(URI uri, Object[] params, Object target, Image value, int state) {
        final ImageView view = (ImageView)target;
        if (value == null) {
            view.setImageDrawable(mDefaultImage);
        } else if (value instanceof Drawable) {
            view.setImageDrawable((Drawable)value);
        } else {
            view.setImageDrawable(mTransformer.transform(uri, value));
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
    public static Binder createImageBinder(Cache imageCache, Binder binder) {
        if (imageCache == null && binder instanceof ImageBinder) {
            final ImageBinder imageBinder = (ImageBinder)binder;
            if (imageBinder.mTransformer instanceof CacheTransformer) {
                imageBinder.mTransformer = ((CacheTransformer)imageBinder.mTransformer).mTransformer;
            }
        }

        return binder;
    }

    /**
     * Class <tt>CacheTransformer</tt> is an implementation of a {@link Transformer}.
     */
    private static final class CacheTransformer<URI> implements Transformer<URI, Object> {
        /* package */ final Transformer mTransformer;
        /* package */ final Cache<URI, Drawable> mImageCache;

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
        public Drawable transform(URI uri, Object image) {
            Drawable drawable = mImageCache.get(uri);
            if (drawable == null) {
                mImageCache.put(uri, drawable = mTransformer.transform(uri, image));
            }

            return drawable;
        }
    }
}
