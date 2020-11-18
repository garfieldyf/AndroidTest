package android.ext.image.decoder;

import android.content.ContentResolver;
import android.ext.image.ImageModule;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.provider.ContactsContract.Contacts.Photo;
import java.io.InputStream;

/**
 * Class <tt>ContactPhotoDecoder</tt> used to decode the contact photo data to a {@link Bitmap}.
 * @author Garfield
 */
public final class ContactPhotoDecoder extends BitmapDecoder<Bitmap> {
    private final ContentResolver mResolver;

    /**
     * Constructor
     * @param module The {@link ImageModule}.
     */
    public ContactPhotoDecoder(ImageModule module) {
        super(module);
        mResolver = mModule.mContext.getContentResolver();
    }

    @Override
    protected Bitmap decodeBitmap(Object uri, Options opts) throws Exception {
        final Uri photoUri = Uri.withAppendedPath((Uri)uri, Photo.DISPLAY_PHOTO);
        try (final InputStream is = mResolver.openInputStream(photoUri)) {
            return BitmapFactory.decodeStream(is, null, opts);
        }
    }
}
