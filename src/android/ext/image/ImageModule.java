package android.ext.image;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.ext.cache.BitmapPool;
import android.ext.cache.Cache;
import android.ext.cache.Caches;
import android.ext.cache.FileCache;
import android.ext.cache.ImageCache;
import android.ext.cache.LruFileCache;
import android.ext.cache.LruImageCache;
import android.ext.concurrent.ThreadPool;
import android.ext.content.AsyncLoader.Binder;
import android.ext.content.res.XmlResources;
import android.ext.image.decoder.BitmapDecoder;
import android.ext.image.decoder.ImageDecoder;
import android.ext.image.params.Parameters;
import android.ext.image.transformer.Transformer;
import android.ext.util.ClassUtils;
import android.ext.util.DebugUtils;
import android.ext.util.Pools;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.util.Printer;
import android.util.SparseArray;
import android.util.TypedValue;

/**
 * Class ImageModule
 * @author Garfield
 */
public class ImageModule<URI, Image> implements ComponentCallbacks2, Factory<Options> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    protected final Executor mExecutor;
    protected final FileCache mFileCache;
    protected final Cache<URI, Image> mImageCache;

    /* package */ final Pool<byte[]> mBufferPool;
    /* package */ final Pool<Options> mOptionsPool;

    private final SparseArray<Parameters> mParameters;
    private final SparseArray<Transformer<Image>> mTransformers;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the loaded images.
     * @param fileCache May be <tt>null</tt>. The {@link FileCache} to store the loaded image files.
     * @see #ImageModule(Context, Executor, Cache, FileCache)
     */
    public ImageModule(Context context, Cache<URI, Image> imageCache, FileCache fileCache) {
        this(context, ThreadPool.createImageThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), 3), 60, TimeUnit.SECONDS), imageCache, fileCache);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param executor The {@link Executor} to executing load task.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the loaded images.
     * @param fileCache May be <tt>null</tt>. The {@link FileCache} to store the loaded image files.
     * @see #ImageModule(Context, Cache, FileCache)
     * @see ThreadPool#createImageThreadPool(int, long, TimeUnit)
     */
    public ImageModule(Context context, Executor executor, Cache<URI, Image> imageCache, FileCache fileCache) {
        final int maxPoolSize = computeBufferPoolMaxSize(executor);
        mContext  = context.getApplicationContext();
        mExecutor = executor;
        mFileCache    = fileCache;
        mImageCache   = imageCache;
        mParameters   = new SparseArray<Parameters>(2);
        mTransformers = new SparseArray<Transformer<Image>>(4);
        mOptionsPool  = Pools.synchronizedPool(Pools.newPool(this, maxPoolSize));
        mBufferPool   = Pools.synchronizedPool(Pools.<byte[]>newPool(maxPoolSize, 16384, byte.class));
        mContext.registerComponentCallbacks(this);
    }

    /**
     * Creates a new {@link Bitmap} module.
     * @param context The <tt>Context</tt>.
     * @param scaleMemory The scale of memory of the bitmap cache, expressed as a percentage of this application maximum memory
     * of the current device. Pass <tt>0</tt> indicates the module has no bitmap cache.
     * @param maxFileSize The maximum number of files in the file cache. Pass <tt>0</tt> indicates the module has no file cache.
     * @param maxPoolSize The maximum number of bitmaps to allow in the internal {@link BitmapPool} of the bitmap cache.
     * Pass <tt>0</tt> indicates the bitmap cache has no {@link BitmapPool}.
     * @return The {@link ImageModule}.
     */
    public static <URI> ImageModule<URI, Bitmap> createBitmapModule(Context context, float scaleMemory, int maxFileSize, int maxPoolSize) {
        return new ImageModule<URI, Bitmap>(context, Caches.<URI>createBitmapCache(scaleMemory, maxPoolSize), createFileCache(context, maxFileSize));
    }

    /**
     * Creates a new image module.
     * @param context The <tt>Context</tt>.
     * @param scaleMemory The scale of memory of the bitmap cache, expressed as a percentage of this application maximum memory
     * of the current device. Pass <tt>0</tt> indicates the module has no bitmap cache.
     * @param maxFileSize The maximum number of files in the file cache. Pass <tt>0</tt> indicates the module has no file cache.
     * @param maxImageSize The maximum image size in the cache. Pass <tt>0</tt> indicates the module has no image cache.
     * @param maxPoolSize The maximum number of bitmaps to allow in the internal {@link BitmapPool} of the bitmap cache.
     * Pass <tt>0</tt> indicates the bitmap cache has no {@link BitmapPool}.
     * @return The {@link ImageModule}.
     */
    public static <URI> ImageModule<URI, Object> createImageModule(Context context, float scaleMemory, int maxFileSize, int maxImageSize, int maxPoolSize) {
        return new ImageModule<URI, Object>(context, new LruImageCache<URI>(scaleMemory, maxImageSize, maxPoolSize), createFileCache(context, maxFileSize));
    }

    /**
     * Returns a {@link Builder} to creates an {@link ImageLoader}.
     * @return The <tt>Builder</tt>.
     */
    public final Builder<URI, Image> createImageLoader() {
        return new Builder<URI, Image>(this);
    }

    /**
     * Returns the {@link Executor} associated with this object.
     * @return The <tt>Executor</tt>.
     */
    public final Executor getExecutor() {
        return mExecutor;
    }

    /**
     * Returns the {@link FileCache} associated with this object.
     * @return The <tt>FileCache</tt> or <tt>null</tt>.
     */
    public final FileCache getFileCache() {
        return mFileCache;
    }

    /**
     * Returns the image cache associated with this object.
     * @return The {@link Cache} or <tt>null</tt>.
     */
    public final Cache<URI, Image> getImageCache() {
        return mImageCache;
    }

    public final void dump(Printer printer) {
        Pools.dumpPool(mBufferPool, printer);
        Pools.dumpPool(mOptionsPool, printer);
        Caches.dumpCache(mImageCache, mContext, printer);
        Caches.dumpCache(mFileCache, mContext, printer);

        final TypedValue value = new TypedValue();
        final Resources res = mContext.getResources();
        int size = mParameters.size();
        if (size > 0) {
            DebugUtils.dumpSummary(printer, new StringBuilder(130), 130, " Dumping Parameters cache [ size = %d ] ", size);
            for (int i = 0; i < size; ++i) {
                res.getValue(mParameters.keyAt(i), value, true);
                mParameters.valueAt(i).dump(printer, "  " + value.string.toString() + " ==> ");
            }
        }

        if ((size = mTransformers.size()) > 0) {
            DebugUtils.dumpSummary(printer, new StringBuilder(130), 130, " Dumping Transformer cache [ size = %d ] ", size);
            for (int i = 0; i < size; ++i) {
                res.getValue(mTransformers.keyAt(i), value, true);
                printer.println("  " + value.string.toString() + " ==> " + mTransformers.valueAt(i));
            }
        }
    }

    @Override
    public Options newInstance() {
        return new Options();
    }

    @Override
    public void onLowMemory() {
        onTrimMemory(TRIM_MEMORY_COMPLETE);
    }

    @Override
    public void onTrimMemory(int level) {
        DebugUtils.__checkUIThread("onTrimMemory");
        DebugUtils.__checkDebug(true, "ImageModule", "onTrimMemory " + this + " level = " + level);
        mBufferPool.clear();
        mOptionsPool.clear();
        mParameters.clear();
        mTransformers.clear();

        if (mImageCache != null) {
            mImageCache.clear();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    }

    /**
     * Return a {@link Parameters} object associated with a resource id.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param id The xml resource id of the <tt>Parameters</tt>.
     * @return The <tt>Parameters</tt> object.
     * @throws NotFoundException if the given <em>id</em> does not exist.
     */
    /* package */ final Parameters getParameters(int id) {
        DebugUtils.__checkUIThread("getParameters");
        Parameters parameters = mParameters.get(id, null);
        if (parameters == null) {
            DebugUtils.__checkDebug(true, "ImageModule", "Loads the Parameters - ID #0x = " + Integer.toHexString(id));
            mParameters.append(id, parameters = XmlResources.loadParameters(mContext, id));
        }

        return parameters;
    }

    /**
     * Return a {@link Transformer} object associated with a resource id.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param id The xml resource id of the <tt>Transformer</tt>.
     * @return The <tt>Transformer</tt> object.
     * @throws NotFoundException if the given <em>id</em> does not exist.
     */
    /* package */ final Transformer<Image> getTransformer(int id) {
        DebugUtils.__checkUIThread("getTransformer");
        Transformer<Image> transformer = mTransformers.get(id, null);
        if (transformer == null) {
            DebugUtils.__checkDebug(true, "ImageModule", "Loads the Transformer - ID #0x = " + Integer.toHexString(id));
            mTransformers.append(id, transformer = XmlResources.loadTransformer(mContext, id));
        }

        return transformer;
    }

    /**
     * Creates a new {@link FileCache} instance.
     */
    private static FileCache createFileCache(Context context, int maxSize) {
        return (maxSize > 0 ? new LruFileCache(context, "._img_module_cache", maxSize) : null);
    }

    /**
     * Computes the maximum buffer pool size of the image module.
     */
    private static int computeBufferPoolMaxSize(Executor executor) {
        final int maxPoolSize = (executor instanceof ThreadPoolExecutor ? ((ThreadPoolExecutor)executor).getMaximumPoolSize() : 4);
        return (maxPoolSize == Integer.MAX_VALUE ? 12 : maxPoolSize + 1);
    }

    /**
     * Class <tt>Builder</tt> to creates an {@link ImageLoader}.
     * <h3>Usage</h3>
     * <p>Here is an example:</p><pre>
     * final ImageLoader&lt;String, Bitmap&gt; mImageLoader = module.createImageLoader()
     *     .setParameters(R.xml.decode_params)
     *     .setBinder(R.xml.image_binder)
     *     .create();</pre>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final class Builder<URI, Image> {
        private static final int FLAG_NO_FILE_CACHE   = 0x01;
        private static final int FLAG_NO_MEMORY_CACHE = 0x02;

        private int mFlags;
        private Class mClass;
        private Object mBinder;
        private Object mDecoder;
        private Object mParameters;
        private final ImageModule mModule;

        /**
         * Constructor
         * @param The {@link ImageModule}.
         */
        /* package */ Builder(ImageModule<URI, Image> module) {
            mModule = module;
        }

        /**
         * Sets the image loader has no file cache.
         * @return This builder.
         * @see #noMemoryCache()
         */
        public final Builder<URI, Image> noFileCache() {
            mFlags |= FLAG_NO_FILE_CACHE;
            return this;
        }

        /**
         * Sets the image loader has no memory cache.
         * @return This builder.
         * @see #noFileCache()
         */
        public final Builder<URI, Image> noMemoryCache() {
            mFlags |= FLAG_NO_MEMORY_CACHE;
            return this;
        }

        /**
         * Sets the {@link Parameters} used to decode the image.
         * @param id The xml resource id of the <tt>Parameters</tt>.
         * @return This builder.
         * @see #setParameters(Parameters)
         */
        public final Builder<URI, Image> setParameters(int id) {
            mParameters = id;
            return this;
        }

        /**
         * Sets the {@link Parameters} used to decode the image.
         * @param parameters The <tt>Parameters</tt>.
         * @return This builder.
         * @see #setParameters(int)
         */
        public final Builder<URI, Image> setParameters(Parameters parameters) {
            mParameters = parameters;
            return this;
        }

        /**
         * Sets the {@link Binder} used to binder the image to target.
         * @param id The xml resource id of the <tt>Binder</tt>.
         * @return This builder.
         * @see #setBinder(Binder)
         */
        public final Builder<URI, Image> setBinder(int id) {
            mBinder = id;
            return this;
        }

        /**
         * Sets the {@link Binder} used to binder the image to target.
         * @param binder The <tt>Binder</tt>.
         * @return This builder.
         * @see #setBinder(int)
         */
        public final Builder<URI, Image> setBinder(Binder<URI, Object, Image> binder) {
            mBinder = binder;
            return this;
        }

        /**
         * Sets the {@link ImageLoader} subclass to create.
         * @param clazz The <tt>ImageLoader</tt> subclass.
         * @return This builder.
         */
        public final Builder<URI, Image> setClass(Class<? extends ImageLoader<URI, Image>> clazz) {
            mClass = clazz;
            return this;
        }

        /**
         * Sets the <tt>ImageDecoder</tt> to decode the image data.
         * @param decoder The <tt>ImageDecoder</tt>.
         * @return This builder.
         * @see #setImageDecoder(Class)
         */
        public final Builder<URI, Image> setImageDecoder(ImageLoader.ImageDecoder<Image> decoder) {
            mDecoder = decoder;
            return this;
        }

        /**
         * Sets the <tt>ImageDecoder</tt> to decode the image data.
         * @param clazz The <tt>ImageDecoder</tt> subclass.
         * @return This builder.
         * @see #setImageDecoder(ImageLoader.ImageDecoder)
         */
        public final Builder<URI, Image> setImageDecoder(Class<? extends ImageLoader.ImageDecoder> clazz) {
            mDecoder = clazz;
            return this;
        }

        /**
         * Creates an {@link ImageLoader} with the arguments supplied to this builder.
         * @return The instance of <tt>ImageLoader</tt>.
         */
        public final ImageLoader<URI, Image> create() {
            // Retrieves the image cache and file cache from image module, may be null.
            final Cache imageCache = ((mFlags & FLAG_NO_MEMORY_CACHE) == 0 ? mModule.mImageCache : null);
            final FileCache fileCache = ((mFlags & FLAG_NO_FILE_CACHE) == 0 ? mModule.mFileCache : null);

            // Creates the binder.
            final Binder binder;
            if (mBinder instanceof Binder) {
                binder = (Binder)mBinder;
            } else if (mBinder instanceof Integer) {
                binder = XmlResources.loadBinder(mModule.mContext, (int)mBinder);
            } else {
                binder = ImageLoader.defaultBinder();
            }

            // Creates the image loader.
            final ImageLoader.ImageDecoder decoder = (ImageLoader.ImageDecoder)createImageDecoder(imageCache);
            if (mClass == null) {
                return new ImageLoader(mModule, imageCache, fileCache, decoder, binder);
            } else {
                return (ImageLoader)newInstance(mClass, new Class[] { ImageModule.class, Cache.class, FileCache.class, ImageLoader.ImageDecoder.class, Binder.class }, mModule, imageCache, fileCache, decoder, binder);
            }
        }

        private Object createImageDecoder(Cache imageCache) {
            if (mDecoder instanceof ImageLoader.ImageDecoder) {
                return mDecoder;
            }

            final Parameters parameters;
            if (mParameters instanceof Parameters) {
                parameters = (Parameters)mParameters;
            } else if (mParameters instanceof Integer) {
                parameters = XmlResources.loadParameters(mModule.mContext, (int)mParameters);
            } else {
                parameters = Parameters.defaultParameters();
            }

            final BitmapPool bitmapPool = (imageCache instanceof ImageCache ? ((ImageCache)imageCache).getBitmapPool() : null);
            if (mDecoder instanceof Class) {
                return newInstance((Class)mDecoder, new Class[] { Context.class, Parameters.class, Pool.class, BitmapPool.class }, mModule.mContext, parameters, mModule.mOptionsPool, bitmapPool);
            } else if (imageCache instanceof LruImageCache) {
                return new ImageDecoder(mModule.mContext, parameters, mModule.mOptionsPool, bitmapPool);
            } else {
                return new BitmapDecoder(mModule.mContext, parameters, mModule.mOptionsPool, bitmapPool);
            }
        }

        private static Object newInstance(Class clazz, Class[] parameterTypes, Object... args) {
            try {
                return ClassUtils.getConstructor(clazz, parameterTypes).newInstance(args);
            } catch (Throwable e) {
                throw new IllegalArgumentException("Couldn't create " + clazz.getName() + " instance", e);
            }
        }
    }
}
