package android.ext.image.transformer;

import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Class <tt>OvalTransformer</tt> used to transforms a {@link Bitmap} to a {@link OvalBitmapDrawable}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p>
 * <pre>&lt;OvalTransformer /&gt;</pre>
 * @author Garfield
 */
public final class OvalTransformer implements Transformer<Bitmap> {
    public static final Transformer<Bitmap> sInstance = new OvalTransformer();

    /**
     * Constructor
     */
    private OvalTransformer() {
    }

    @Override
    public Drawable transform(Bitmap bitmap) {
        return new OvalBitmapDrawable(bitmap);
    }
}
