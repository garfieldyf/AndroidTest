package android.ext.image.transformer;

import android.graphics.drawable.Drawable;

/**
 * A <tt>Transformer</tt> interface used to transforms an image to a {@link Drawable}.
 * @author Garfield
 */
public interface Transformer<Image> {
    /**
     * Transforms an <tt>Image</tt> to a <tt>Drawable</tt>.
     * @param image The image to convert.
     * @return The <tt>Drawable</tt>.
     */
    public Drawable transform(Image image);
}
