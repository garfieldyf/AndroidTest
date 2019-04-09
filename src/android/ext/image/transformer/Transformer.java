package android.ext.image.transformer;

import android.ext.image.binder.ImageBinder;
import android.graphics.drawable.Drawable;

/**
 * A <tt>Transformer</tt> interface used to transforms an image to a {@link Drawable}.
 * @author Garfield
 */
public interface Transformer<URI, Image> {
    /**
     * Transforms the <tt>Image</tt> to a <tt>Drawable</tt>.
     * @param uri The uri, passed earlier by {@link ImageBinder#bindValue}.
     * @param image The image to convert, passed earlier by {@link ImageBinder#bindValue}.
     * @return The <tt>Drawable</tt>.
     */
    public Drawable transform(URI uri, Image image);
}
