package com.tencent.temp;

import android.content.ContentResolver;
import android.content.Context;
import android.ext.cache.BitmapPool;
import android.ext.database.DatabaseUtils;
import android.ext.image.decoder.BitmapDecoder;
import android.ext.util.Pools.Pool;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.provider.ContactsContract.Contacts.Photo;
import android.support.annotation.Keep;
import java.io.InputStream;

/**
 * Class <tt>ContactPhotoDecoder</tt> used to decode the contact photo data to a {@link Bitmap}.
 * @author Garfield
 */
public final class ContactPhotoDecoder extends BitmapDecoder<Bitmap> {
    private static final ThreadLocal<byte[]> sPhotoLocal = new ThreadLocal<byte[]>();

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param optionsPool The <tt>Options</tt> {@link Pool} to decode bitmap.
     * @param bitmapPool May be <tt>null</tt>. The {@link BitmapPool} to reuse
     * the bitmap when decoding bitmap.
     */
    @Keep
    public ContactPhotoDecoder(Context context, Pool<Options> optionsPool, BitmapPool bitmapPool) {
        super(context, optionsPool, bitmapPool);
    }

    @Override
    protected Bitmap decodeBitmap(Object uri, Options opts) throws Exception {
        final ContentResolver resolver = mContext.getContentResolver();
        if (opts.inJustDecodeBounds) {
            decodeContactPhoto(resolver, (Uri)uri, opts);
            if (opts.outWidth <= 0) {
                sPhotoLocal.set(null);  // Clear the thread local value.
                decodePhotoBounds(resolver, (Uri)uri, opts);
            }
        } else {
            final Bitmap photo = decodeContactPhoto(resolver, (Uri)uri, opts);
            if (photo != null) {
                return photo;
            }

            // Decode the contact's thumbnail photo.
            final byte[] data = sPhotoLocal.get();
            if (data != null) {
                if (opts.inBitmap == null) sPhotoLocal.set(null);
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
