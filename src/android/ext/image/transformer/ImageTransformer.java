package android.ext.image.transformer;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Class <tt>ImageTransformer</tt> is an implementation of a {@link Transformer}.
 * @author Garfield
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class ImageTransformer implements Transformer {
    private final Transformer mImageTransformer;
    private final Transformer mBitmapTransformer;

    /**
     * Constructor
     */
    private ImageTransformer(Transformer bitmapTransformer, Transformer imageTransformer) {
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
    public Drawable transform(Object uri, Object image) {
        if (image instanceof Drawable) {
            return (Drawable)image;
        } else if (image instanceof Bitmap) {
            return mBitmapTransformer.transform(uri, image);
        } else {
            return mImageTransformer.transform(uri, image);
        }
    }
}
