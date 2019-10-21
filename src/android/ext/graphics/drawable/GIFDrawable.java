package android.ext.graphics.drawable;

import java.io.InputStream;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.ext.graphics.GIFImage;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;

/**
 * Class GIFDrawable
 * @author Garfield
 */
public class GIFDrawable extends GIFBaseDrawable<GIFDrawable.GIFImageState> {
    /**
     * Decodes a {@link GIFDrawable} from the <tt>Resources</tt>.
     * @param res The resource containing the GIF data.
     * @param id The resource id to be decoded.
     * @return The <tt>GIFDrawable</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decode(byte[], int, int)
     * @see #decode(InputStream, byte[])
     * @see #decode(Context, Object, byte[])
     */
    public static GIFDrawable decode(Resources res, int id) {
        final GIFImage image = GIFImage.decode(res, id);
        return (image != null ? new GIFDrawable(image) : null);
    }

    /**
     * Decodes a {@link GIFDrawable} from the <tt>InputStream</tt>.
     * @param is The <tt>InputStream</tt> containing the GIF data.
     * @param tempStorage May be <tt>null</tt>. The temporary storage to use for decoding. Suggest 16K.
     * @return The <tt>GIFDrawable</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decode(Resources, int)
     * @see #decode(byte[], int, int)
     * @see #decode(Context, Object, byte[])
     */
    public static GIFDrawable decode(InputStream is, byte[] tempStorage) {
        final GIFImage image = GIFImage.decode(is, tempStorage);
        return (image != null ? new GIFDrawable(image) : null);
    }

    /**
     * Decodes a {@link GIFDrawable} from the byte array.
     * @param data The byte array containing the GIF data.
     * @param offset The starting offset of the <em>data</em>.
     * @param length The number of bytes of the <em>data</em>, beginning at offset.
     * @return The <tt>GIFDrawable</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decode(Resources, int)
     * @see #decode(InputStream, byte[])
     * @see #decode(Context, Object, byte[])
     */
    public static GIFDrawable decode(byte[] data, int offset, int length) {
        final GIFImage image = GIFImage.decode(data, offset, length);
        return (image != null ? new GIFDrawable(image) : null);
    }

    /**
     * Decodes a {@link GIFDrawable} from the specified <em>uri</em>.
     * <h3>Accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android.asset ({@link #SCHEME_ANDROID_ASSET})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param context The <tt>Context</tt>.
     * @param uri The uri to decode.
     * @param tempStorage May be <tt>null</tt>. The temporary storage to use for decoding. Suggest 16K.
     * @return The <tt>GIFDrawable</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decode(Resources, int)
     * @see #decode(byte[], int, int)
     * @see #decode(InputStream, byte[])
     */
    public static GIFDrawable decode(Context context, Object uri, byte[] tempStorage) {
        final GIFImage image = GIFImage.decode(context, uri, tempStorage);
        return (image != null ? new GIFDrawable(image) : null);
    }

    /**
     * Constructor
     * <p>The default constructor used by {@link Resources#getDrawable(int)}.</p>
     */
    @Keep
    public GIFDrawable() {
        super(new GIFImageState((GIFImage)null));
    }

    /**
     * Constructor
     * @param image The {@link GIFImage}. Never <tt>null</tt>.
     */
    public GIFDrawable(GIFImage image) {
        super(new GIFImageState(image));
    }

    @Override
    public Drawable mutate() {
        if ((mFlags & FLAG_MUTATED) == 0) {
            mFlags |= FLAG_MUTATED;
            mState = new GIFImageState(mState);
        }

        return this;
    }

    /**
     * Constructor
     * <p>The constructor used by {@link ConstantState#newDrawable()}.</p>
     */
    /* package */ GIFDrawable(GIFImageState state) {
        super(state);
    }

    /**
     * Class <tt>GIFImageState</tt> is an implementation of a {@link ConstantState}.
     */
    /* package */ static final class GIFImageState extends GIFBaseDrawable.GIFBaseState {
        /**
         * Constructor
         * @param image The {@link GIFImage}.
         * @see #GIFImageState(GIFImageState)
         */
        public GIFImageState(GIFImage image) {
            super(image);
        }

        /**
         * Copy constructor
         * @param state The {@link GIFImageState}.
         * @see #GIFImageState(GIFImage)
         */
        public GIFImageState(GIFImageState state) {
            super(state);
        }

        @Override
        public Drawable newDrawable() {
            return new GIFDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new GIFDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res, Theme theme) {
            return new GIFDrawable(this);
        }
    }
}
