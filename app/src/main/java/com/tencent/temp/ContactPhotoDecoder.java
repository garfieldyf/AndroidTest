package com.tencent.temp;

import android.content.Context;
import android.ext.cache.BitmapPool;
import android.ext.image.decoder.BitmapDecoder;
import android.ext.util.Pools.Pool;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class <tt>ContactPhotoDecoder</tt> used to decode the contact photo data to a {@link Bitmap}.
 * @author Garfield
 */
public final class ContactPhotoDecoder extends BitmapDecoder<Bitmap> {
    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param optionsPool The <tt>Options</tt> {@link Pool} to decode bitmap.
     * @param bitmapPool May be <tt>null</tt>. The {@link BitmapPool} to reuse
     * the bitmap when decoding bitmap.
     */
    public ContactPhotoDecoder(Context context, Pool<Options> optionsPool, BitmapPool bitmapPool) {
        super(context, optionsPool, bitmapPool);
    }

    protected Bitmap decodeBitmap(Object uri, Options opts) throws Exception {
        final Bitmap bitmap = decodeContactPhoto((Uri)uri, opts, true);
        return (bitmap != null ? bitmap : decodeContactPhoto((Uri)uri, opts, false));
    }

    private Bitmap decodeContactPhoto(Uri contactUri, Options opts, boolean preferHighres) throws IOException {
        try (final InputStream is = Contacts.openContactPhotoInputStream(mContext.getContentResolver(), contactUri, preferHighres)) {
            return (is != null ? BitmapFactory.decodeStream(is, null, opts) : null);
        }
    }
}
