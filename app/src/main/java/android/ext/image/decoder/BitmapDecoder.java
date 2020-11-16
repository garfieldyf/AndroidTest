package android.ext.image.decoder;

import static android.ext.image.AbsImageLoader.FLAG_DUMP_OPTIONS;
import android.ext.cache.BitmapPool;
import android.ext.image.AbsImageDecoder;
import android.ext.image.ImageModule;
import android.ext.image.params.Parameters;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.util.Log;

/**
 * Class <tt>BitmapDecoder</tt> used to decode the image data to a {@link Bitmap}.
 * @param <Image> Must be <tt>Bitmap</tt> or <tt>Object</tt> that will be decode
 * the result type.
 * @author Garfield
 */
public class BitmapDecoder<Image> extends AbsImageDecoder<Image> {
    /**
     * Constructor
     * @param module The {@link ImageModule}.
     */
    public BitmapDecoder(ImageModule module) {
        super(module);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Image decodeImage(Object uri, Object target, Parameters parameters, int flags, Options opts) throws Exception {
        // Computes the sample size.
        parameters.computeSampleSize(target, opts);

        // Retrieves the bitmap from bitmap pool to reuse it.
        if (Build.VERSION.SDK_INT < 26 || opts.inPreferredConfig != Config.HARDWARE) {
            final BitmapPool bitmapPool = mModule.getBitmapPool();
            if (bitmapPool != null) {
                opts.inBitmap = bitmapPool.get(parameters.computeByteCount(opts));
                DebugUtils.__checkDebug(opts.inBitmap != null, "BitmapDecoder", "decodeBitmap will attempt to reuse the " + opts.inBitmap);
            }
        }

        // Decodes the image pixels.
        Bitmap bitmap = null;
        try {
            BitmapDecoder.__checkDumpOptions(opts, flags);
            bitmap = decodeBitmap(uri, opts);
        } catch (IllegalArgumentException e) {
            // Decodes the bitmap again, If decode the bitmap into inBitmap failed.
            if (opts.inBitmap != null) {
                DebugUtils.__checkLogError(true, "BitmapDecoder", "decodeBitmap failed - " + e.getMessage());
                opts.inBitmap = null;
                bitmap = decodeBitmap(uri, opts);
            }
        }

        BitmapDecoder.__checkBitmap(bitmap, opts);
        return (Image)bitmap;
    }

    private static void __checkBitmap(Bitmap bitmap, Options opts) {
        if (Build.VERSION.SDK_INT >= 26 && bitmap != null) {
            final Config config = bitmap.getConfig();
            if (config != null && opts.outConfig != null && config != opts.outConfig) {
                throw new AssertionError("The bitmap config = " + config + ", opts.outConfig = " + opts.outConfig + " are not equal.");
            }
        }
    }

    private static void __checkDumpOptions(Options opts, int flags) {
        if (opts.inBitmap != null && !opts.inBitmap.isMutable()) {
            throw new AssertionError("Only mutable bitmap can be reused - " + opts.inBitmap);
        }

        if ((flags & FLAG_DUMP_OPTIONS) != 0) {
            final StringBuilder builder = new StringBuilder(opts.toString()).append("\n{")
                .append("\n  inSampleSize = ").append(opts.inSampleSize)
                .append("\n  inJustDecodeBounds = ").append(opts.inJustDecodeBounds)
                .append("\n  inPreferredConfig  = ").append(opts.inPreferredConfig)
                .append("\n  inMutable = ").append(opts.inMutable)
                .append("\n  inScaled  = ").append(opts.inScaled)
                .append("\n  inDensity = ").append(opts.inDensity)
                .append("\n  inTargetDensity = ").append(opts.inTargetDensity)
                .append("\n  inScreenDensity = ").append(opts.inScreenDensity)
                .append("\n  inBitmap  = ").append(opts.inBitmap)
                .append("\n  outWidth  = ").append(opts.outWidth)
                .append("\n  outHeight = ").append(opts.outHeight);

            if (Build.VERSION.SDK_INT >= 26) {
                builder.append("\n  outConfig = ").append(opts.outConfig)
                    .append("\n  outMimeType = ").append(opts.outMimeType)
                    .append("\n  outColorSpace = ").append(opts.outColorSpace)
                    .append("\n  inPreferredColorSpace = ").append(opts.inPreferredColorSpace);
            } else {
                builder.append("\n  outMimeType = ").append(opts.outMimeType);
            }

            Log.d("BitmapDecoder", builder.append("\n  inTempStorage = ").append(opts.inTempStorage)
               .append(opts.inTempStorage != null ? " { length = " + opts.inTempStorage.length + " }" : "")
               .append("\n}").toString());
        }
    }
}
