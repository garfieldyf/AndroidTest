package android.ext.image.transformer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Class <tt>BitmapTransformer</tt> used to transforms a {@link Bitmap} to a {@link BitmapDrawable}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p>
 * <pre>&lt;BitmapTransformer /&gt;</pre>
 * @author Garfield
 */
public final class BitmapTransformer implements Transformer<Bitmap> {
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
     * Returns a {@link Transformer} to transforms a {@link Bitmap} to a {@link BitmapDrawable}.
     * @param context The <tt>Context</tt>.
     * @return The <tt>Transformer</tt>.
     */
    public static synchronized Transformer<Bitmap> getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BitmapTransformer(context);
        }

        return sInstance;
    }

    @Override
    public Drawable transform(Bitmap bitmap) {
        return new BitmapDrawable(mContext.getResources(), bitmap);
    }
}
