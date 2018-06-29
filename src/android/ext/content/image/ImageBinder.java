package android.ext.content.image;

import org.xmlpull.v1.XmlPullParser;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.cache.Cache;
import android.ext.cache.Caches;
import android.ext.cache.SimpleLruCache;
import android.ext.content.AsyncLoader.Binder;
import android.ext.content.image.Transformer.BitmapTransformer;
import android.ext.content.image.Transformer.CacheTransformer;
import android.ext.content.image.Transformer.ImageTransformer;
import android.ext.util.DebugUtils;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Printer;
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
        a.recycle();

        final Transformer transformer = inflateTransformer(context, (XmlPullParser)attrs, attrs);
        mTransformer = (maxSize > 0 ? new CacheTransformer(new SimpleLruCache<Object, Drawable>(maxSize), transformer) : transformer);
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
     * @return The <tt>Transformer</tt>.
     */
    protected Transformer<URI, Image> inflateTransformer(Context context, XmlPullParser parser, AttributeSet attrs) {
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

            return transformer;
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
}
