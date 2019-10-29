package android.ext.image.transformer;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.content.res.XmlResources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Class <tt>ImageTransformer</tt> is an implementation of a {@link Transformer}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;ImageTransformer xmlns:android="http://schemas.android.com/apk/res/android" &gt;
 *     &lt;!-- The bitmap transformer, See transformer class --&gt;
 *     &lt;BitmapTransformer /&gt;
 *
 *     &lt;!-- The image transformer, See transformer class --&gt;
 *     &lt;GIFTransformer /&gt;
 * &lt;/ImageTransformer&gt</pre>
 * @author Garfield
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ImageTransformer implements Transformer {
    private static final int[] TRANSFORMER_ATTRS = {
        android.R.attr.src
    };

    private final Transformer mImageTransformer;
    private final Transformer mBitmapTransformer;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #ImageTransformer(Transformer, Transformer)
     */
    public ImageTransformer(Context context, AttributeSet attrs) {
        try {
            inflateAttributes(context, attrs);
            mBitmapTransformer = inflate(context, attrs);
            mImageTransformer  = inflate(context, attrs);
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't inflate transformer from xml", e);
        }
    }

    /**
     * Constructor
     * @param bitmapTransformer The {@link Transformer} used to transforms a <tt>Bitmap</tt> to a <tt>Drawable</tt>.
     * @param imageTransformer The {@link Transformer} used to transforms an image to a <tt>Drawable</tt>.
     * @see #ImageTransformer(Context, AttributeSet)
     */
    public ImageTransformer(Transformer bitmapTransformer, Transformer imageTransformer) {
        mImageTransformer  = imageTransformer;
        mBitmapTransformer = bitmapTransformer;
    }

    @Override
    public Drawable transform(Object image) {
        if (image instanceof Bitmap) {
            return mBitmapTransformer.transform(image);
        } else {
            return mImageTransformer.transform(image);
        }
    }

    /**
     * Inflates this transformer's attributes from an XML resource.
     * @param context The <tt>Context</tt>.
     * @param attrs The base set of attribute values.
     */
    protected void inflateAttributes(Context context, AttributeSet attrs) {
    }

    /**
     * Inflates a new {@link Transformer} object from a xml resource.
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @return The <tt>Transformer</tt>.
     */
    private static Transformer inflate(Context context, AttributeSet attrs) throws XmlPullParserException, IOException, ReflectiveOperationException {
        // Moves to the start tag position.
        int type;
        final XmlPullParser parser = (XmlPullParser)attrs;
        while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
            // Empty loop
        }

        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        if ("transformer".equals(parser.getName())) {
            final TypedArray a = context.obtainStyledAttributes(attrs, TRANSFORMER_ATTRS);
            final int id = a.getResourceId(0 /* android.R.attr.src */, 0);
            a.recycle();

            if (id != 0) {
                return XmlResources.loadTransformer(context, id);
            }
        }

        return XmlResources.inflateTransformer(context, parser);
    }
}
