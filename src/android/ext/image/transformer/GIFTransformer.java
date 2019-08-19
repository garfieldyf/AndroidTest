package android.ext.image.transformer;

import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
import android.graphics.drawable.Drawable;

/**
 * Class <tt>GIFTransformer</tt> used to transforms a {@link GIFImage} to a {@link GIFDrawable}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p>
 * <pre>&lt;GIFTransformer /&gt;</pre>
 * @author Garfield
 */
public final class GIFTransformer implements Transformer<GIFImage> {
    public static final Transformer<GIFImage> sInstance = new GIFTransformer();

    /**
     * Constructor
     */
    private GIFTransformer() {
    }

    @Override
    public Drawable transform(GIFImage image) {
        return new GIFDrawable(image);
    }
}
