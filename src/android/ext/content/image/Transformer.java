package android.ext.content.image;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.cache.Cache;
import android.ext.content.XmlResources;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

/**
 * Class <tt>Transformer</tt> used to transforms an image to a {@link Drawable}.
 * @author Garfield
 * @version 1.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class Transformer<URI, Image> {
    private static final int[] TRANSFORMER_ATTRS = new int[] { android.R.attr.name };

    /**
     * Transforms the <tt>Image</tt> to a <tt>Drawable</tt>.
     * @param uri The uri, passed earlier by <tt>ImageBinder.bindValue</tt>.
     * @param target The target, passed earlier by <tt>ImageBinder.bindValue</tt>.
     * @param image The image to convert, passed earlier by <tt>ImageBinder.bindValue</tt>.
     * @return The <tt>Drawable</tt>.
     */
    public abstract Drawable transform(URI uri, Object target, Image image);

    /**
     * Inflates a new {@link Transformer} object from a xml resource.
     * @param context The <tt>Context</tt>.
     * @param parser The XML parser from which to inflate the transformer.
     * @param attrs The base set of attribute values.
     * @return The <tt>Transformer</tt> or <tt>null</tt>.
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
        if (TextUtils.isEmpty(name) || "rectangle".equals(name)) {
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
    public static final class ImageTransformer extends Transformer {
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
    /* package */ static final class CacheTransformer extends Transformer {
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
