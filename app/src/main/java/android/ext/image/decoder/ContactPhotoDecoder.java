package android.ext.image.decoder;

import android.content.ContentResolver;
import android.ext.cache.BitmapPool;
import android.ext.database.DatabaseUtils;
import android.ext.image.ImageModule;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.provider.ContactsContract.Contacts.Photo;
import java.io.InputStream;

/**
 * Class <tt>ContactPhotoDecoder</tt> used to decode the contact photo data to a {@link Bitmap}.
 * @param <Image> Must be <tt>Bitmap</tt> or <tt>Object</tt> that will be decode
 * the result type.
 * @author Garfield
 */
public class ContactPhotoDecoder<Image> extends BitmapDecoder<Image> {
    private static final ThreadLocal<byte[]> sPhotoLocal = new ThreadLocal<byte[]>();

    /**
     * Constructor
     * @param module The {@link ImageModule}.
     * @param bitmapPool May be <tt>null</tt>. The {@link BitmapPool}
     * to reuse the bitmap when decoding bitmap.
     */
    public ContactPhotoDecoder(ImageModule<?, ?> module, BitmapPool bitmapPool) {
        super(module, bitmapPool);
    }

    @Override
    protected Bitmap decodeBitmap(Object uri, Options opts) throws Exception {
        final ContentResolver resolver = mModule.mContext.getContentResolver();
        final Uri contactUri = (Uri)uri;
        if (opts.inJustDecodeBounds) {
            decodeContactPhoto(resolver, contactUri, opts);
            if (opts.outWidth <= 0) {
                sPhotoLocal.set(null);  // Clear the contact photo data.
                decodePhotoBounds(resolver, contactUri, opts);
            }
        } else {
            final Bitmap photo = decodeContactPhoto(resolver, contactUri, opts);
            if (photo != null) {
                return photo;
            }

            // Decode the contact's thumbnail photo.
            final byte[] data = sPhotoLocal.get();
            if (data != null) {
                return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            }
        }

        return null;
    }

    /**
     * Decode the contact's thumbnail photo bounds (including width, height, mimeType etc).
     */
    private static void decodePhotoBounds(ContentResolver resolver, Uri contactUri, Options opts) {
        final byte[] data = DatabaseUtils.simpleQueryBlob(resolver, Uri.withAppendedPath(contactUri, Photo.CONTENT_DIRECTORY), Photo.PHOTO, null, null);
        if (data != null) {
            BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            if (opts.outWidth > 0) {
                // Save the contact photo data.
                sPhotoLocal.set(data);
            }
        }
    }

    /**
     * Decode the contact's display photo.
     */
    private static Bitmap decodeContactPhoto(ContentResolver resolver, Uri contactUri, Options opts) throws Exception {
        try (final InputStream is = resolver.openInputStream(Uri.withAppendedPath(contactUri, Photo.DISPLAY_PHOTO))) {
            return (is != null ? BitmapFactory.decodeStream(is, null, opts) : null);
        }
    }
}