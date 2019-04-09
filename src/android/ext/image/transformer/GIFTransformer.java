package android.ext.image.transformer;

import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
import android.graphics.drawable.Drawable;

/**
 * Class <tt>GIFTransformer</tt> used to transforms a {@link GIFImage} to a {@link GIFDrawable}.
 * <h2>Usage</h2>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;GIFTransformer /&gt;</pre>
 * @author Garfield
 */
public final class GIFTransformer implements Transformer<Object, GIFImage> {
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
    @SuppressWarnings("unchecked")
    public static <URI> Transformer<URI, GIFImage> getInstance() {
        return (Transformer<URI, GIFImage>)sInstance;
    }

    @Override
    public Drawable transform(Object uri, GIFImage image) {
        return new GIFDrawable(image);
    }
}
