package android.ext.image;

import static android.ext.image.ImageModule.CONFIG;
import static android.ext.image.ImageModule.PARAMETERS;
import static android.ext.image.ImageModule.PARAMS_LENGTH;
import static android.ext.image.ImageModule.PLACEHOLDER;
import android.annotation.UiThread;
import android.ext.cache.Cache;
import android.ext.content.AsyncLoader;
import android.ext.content.AsyncLoader.Binder;
import android.ext.image.params.Parameters;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import java.io.File;
import java.util.Arrays;

/**
 * An abstract class that performs asynchronous loading of images.
 * @author Garfield
 */
public abstract class AbsImageLoader<Image> extends AsyncLoader<Object, Object, Image> implements Binder<Object, Object, Image> {
    /**
     * If set the image loader will be dump the {@link Options} when it
     * will be decode image.<p>This flag can be used in DEBUG mode.</p>
     */
    public static final int FLAG_DUMP_OPTIONS = 0x04000000;    /* flags 0x0F000000 */

    /**
     * The {@link LoadRequest}.
     */
    private final LoadRequest mRequest;

    /**
     * The {@link ImageModule}.
     */
    protected final ImageModule mModule;

    /**
     * Constructor
     * @param module The {@link ImageModule}.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the loaded image.
     */
    protected AbsImageLoader(ImageModule module, Cache<Object, Image> imageCache) {
        super(module.mExecutor, imageCache, module.mTaskPool);

        mModule  = module;
        mRequest = new LoadRequest();
    }

    /**
     * Loads the image from the specified <em>uri</em>, bind it to the target. If the image
     * is already cached, it is bind immediately. Otherwise loads the image on a background
     * thread. <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * <h3>The default implementation accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>{@link File} (no scheme)</li>
     * <li>ftp ({@link #SCHEME_FTP})</li>
     * <li>http ({@link #SCHEME_HTTP})</li>
     * <li>https ({@link #SCHEME_HTTPS})</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android.asset ({@link #SCHEME_ANDROID_ASSET})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param uri May be <tt>null</tt>. The uri to load.
     * @return The {@link LoadRequest}.
     */
    @UiThread
    public final LoadRequest load(Object uri) {
        DebugUtils.__checkUIThread("load");
        mRequest.mUri = resolveUri(uri);
        mRequest.mFlags  = 0;
        mRequest.mBinder = this;
        mRequest.mParams = mModule.mParamsPool.obtain();
        return mRequest;
    }

    /**
     * Equivalent to calling <tt>loadSync(uri, 0, config, parameters)</tt>.
     * @param uri The uri to load.
     * @param config May be <tt>null</tt>. The desired {@link Config} to decode bitmap.
     * @param parameters May be <tt>null</tt>. The {@link Parameters} to decode image.
     * @return The image, or <tt>null</tt> if load failed or this loader was shut down.
     * @see #loadSync(Object, int, Object[])
     */
    public final Image loadSync(Object uri, Config config, Parameters parameters) {
        return loadSync(resolveUri(uri), 0, config, parameters);
    }

    @Override
    protected final void onRecycle(Object[] params) {
        ImageModule.__checkParameters(params, PARAMS_LENGTH - 1);
        Arrays.fill(params, null);  // Clear for recycle.
        mModule.mParamsPool.recycle(params);
    }

    /**
     * Resolves an empty (0-length) string to <tt>null</tt>.
     */
    private static Object resolveUri(Object uri) {
        return (uri instanceof String && ((String)uri).isEmpty() ? null : uri);
    }

    /**
     * The <tt>LoadRequest</tt> class used to {@link AbsImageLoader} to load the image.
     * <h3>Usage</h3>
     * <p>Here is an example:</p><pre>
     * loader.load(uri)
     *     .config(Config.RGB_565)
     *     .parameters(R.xml.decode_params)
     *     .placeholder(R.drawable.ic_placeholder)
     *     .into(imageView);</pre>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final class LoadRequest {
        /* package */ Object mUri;
        /* package */ int mFlags;
        /* package */ Binder mBinder;
        /* package */ Object[] mParams;

        /**
         * Constructor
         */
        /* package */ LoadRequest() {
        }

        /**
         * Adds the loading flags to load image.
         * @param flags Loading flags. May be any combination of
         * <tt>FLAG_XXX</tt> constants.
         * @return This request.
         */
        public final LoadRequest flags(int flags) {
            mFlags |= flags;
            return this;
        }

        /**
         * Equivalent to calling <tt>flags(FLAG_IGNORE_MEMORY_CACHE)</tt>.
         * @return This request.
         * @see #flags(int)
         * @see #FLAG_IGNORE_MEMORY_CACHE
         */
        public final LoadRequest skipMemory() {
            mFlags |= FLAG_IGNORE_MEMORY_CACHE;
            return this;
        }

        /**
         * Equivalent to calling <tt>flags(FLAG_DUMP_OPTIONS)</tt>.
         * @return This request.
         * @see #flags(int)
         * @see #FLAG_DUMP_OPTIONS
         */
        public final LoadRequest dumpOptions() {
            mFlags |= FLAG_DUMP_OPTIONS;
            return this;
        }

        /**
         * Sets the desired {@link Config} to decode bitmap.
         * @param config The config to decode.
         * @return This request.
         */
        public final LoadRequest config(Config config) {
            mParams[CONFIG] = config;
            return this;
        }

        /**
         * Sets the {@link Parameters} to decode image.
         * @param id The xml resource id of the <tt>Parameters</tt>.
         * @return This request.
         * @see #parameters(Object)
         */
        public final LoadRequest parameters(int id) {
            mParams[PARAMETERS] = mModule.getResource(id, null);
            return this;
        }

        /**
         * Sets the parameters to decode image.
         * @param parameters The parameters to decode.
         * @return This request.
         * @see #parameters(int)
         */
        public final LoadRequest parameters(Object parameters) {
            mParams[PARAMETERS] = parameters;
            return this;
        }

        /**
         * Sets the <tt>Drawable</tt> to be used when the image is loading.
         * @param id The resource id of the <tt>Drawable</tt>.
         * @return This request.
         * @see #placeholder(Drawable)
         */
        public final LoadRequest placeholder(int id) {
            mParams[PLACEHOLDER] = id;
            return this;
        }

        /**
         * Sets the <tt>Drawable</tt> to be used when the image is loading.
         * @param drawable The <tt>Drawable</tt>.
         * @return This request.
         * @see #placeholder(int)
         */
        public final LoadRequest placeholder(Drawable drawable) {
            mParams[PLACEHOLDER] = drawable;
            return this;
        }

        /**
         * Sets the {@link Binder} to bind the image to target.
         * @param id The xml resource id of the <tt>Binder</tt>.
         * @return This request.
         * @see #binder(Binder)
         */
        public final LoadRequest binder(int id) {
            mBinder = (Binder)mModule.getResource(id, null);
            return this;
        }

        /**
         * Sets the {@link Binder} to bind the image to target.
         * @param binder The <tt>Binder</tt> to bind.
         * @return This request.
         * @see #binder(int)
         */
        public final LoadRequest binder(Binder binder) {
            mBinder = binder;
            return this;
        }

        /**
         * Loads the image with the arguments supplied to this request.
         * @param target The <tt>Object</tt> to bind.
         */
        public final void into(Object target) {
            load(mUri, target, mFlags, mBinder, mParams);
        }

//        /**
//         * preloads the image with the arguments supplied to this request.
//         */
//        public final void preload() {
//            DebugUtils.__checkError(mUri == null, "Invalid parameter - uri == null");
//            DebugUtils.__checkWarning(getCache() == null, "AbsImageLoader", "No image cache, invoking this method has no effect.");
//            DebugUtils.__checkWarning((mFlags & FLAG_IGNORE_MEMORY_CACHE) != 0, "AbsImageLoader", "The FLAG_IGNORE_MEMORY_CACHE is set, invoking this method has no effect.");
//            if (getCache() != null && (mFlags & FLAG_IGNORE_MEMORY_CACHE) == 0) {
//                load(mUri, mUri, mFlags, Binder.emptyBinder(), mParams);
//            }
//        }
    }
}
