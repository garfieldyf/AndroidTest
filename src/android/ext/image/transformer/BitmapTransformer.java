package android.ext.image.transformer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Class <tt>BitmapTransformer</tt> used to transforms a {@link Bitmap} to a {@link BitmapDrawable}.
 * <h2>Usage</h2>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;BitmapTransformer /&gt;</pre>
 * @author Garfield
 */
public final class BitmapTransformer implements Transformer<Object, Bitmap> {
    private static BitmapTransformer sInstance;
    private final Context mContext;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     */
    private BitmapTransformer(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Returns a type-safe {@link Transformer} to transforms a {@link Bitmap} to a {@link BitmapDrawable}.
     * @param context The <tt>Context</tt>.
     * @return The <tt>Transformer</tt>.
     */
    @SuppressWarnings("unchecked")
    public static synchronized <URI> Transformer<URI, Bitmap> getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BitmapTransformer(context);
        }

        return (Transformer<URI, Bitmap>)sInstance;
    }

    @Override
    public Drawable transform(Object uri, Bitmap bitmap) {
        return new BitmapDrawable(mContext.getResources(), bitmap);
    }
}
