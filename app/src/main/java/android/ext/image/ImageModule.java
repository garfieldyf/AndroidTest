package android.ext.image;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.ext.cache.BitmapPool;
import android.ext.cache.Cache;
import android.ext.cache.Caches;
import android.ext.cache.FileCache;
import android.ext.cache.LinkedBitmapPool;
import android.ext.cache.LruBitmapCache;
import android.ext.cache.LruBitmapCache2;
import android.ext.cache.LruCache;
import android.ext.cache.LruFileCache;
import android.ext.cache.LruImageCache;
import android.ext.concurrent.ThreadPool;
import android.ext.content.res.XmlResources;
import android.ext.content.res.XmlResources.XmlResourceInflater;
import android.ext.image.ImageLoader.LoadRequest;
import android.ext.image.decoder.BitmapDecoder;
import android.ext.image.decoder.ImageDecoder;
import android.ext.image.params.Parameters;
import android.ext.image.transformer.RoundedGIFTransformer;
import android.ext.image.transformer.RoundedRectTransformer;
import android.ext.image.transformer.Transformer;
import android.ext.util.ArrayUtils;
import android.ext.util.ClassUtils;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.Pools;
import android.ext.util.Pools.ByteArrayPool;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.graphics.BitmapFactory.Options;
import android.os.Process;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LogPrinter;
import android.util.Printer;
import android.util.SparseArray;
import android.util.TypedValue;
import android.util.Xml;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Class ImageModule
 * @author Garfield
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ImageModule<URI, Image> implements ComponentCallbacks2, Factory<Options>, XmlResourceInflater<ImageLoader> {
    private static final int MAX_ARRAY_LENGTH     = 4;
    private static final int FLAG_NO_FILE_CACHE   = 0x01;
    private static final int FLAG_NO_MEMORY_CACHE = 0x02;

    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /* package */ final Executor mExecutor;
    /* package */ final Pool<byte[]> mBufferPool;
    /* package */ final Pool<Options> mOptionsPool;
    /* package */ final Pool<Object[]> mParamsPool;

    private final FileCache mFileCache;
    private final Cache<URI, Image> mImageCache;
    private final SparseArray<Object> mResources;
    private final SparseArray<ImageLoader> mLoaderCache;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param executor The {@link Executor} to executing load task.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the loaded images.
     * @param fileCache May be <tt>null</tt>. The {@link FileCache} to store the loaded image files.
     */
    public ImageModule(Context context, Executor executor, Cache<URI, Image> imageCache, FileCache fileCache) {
        final int maxPoolSize = computeBufferPoolMaxSize(executor);
        mContext  = context.getApplicationContext();
        mExecutor = executor;
        mFileCache   = fileCache;
        mImageCache  = imageCache;
        mResources   = new SparseArray<Object>(8);
        mLoaderCache = new SparseArray<ImageLoader>(2);
        mParamsPool  = Pools.newPool(48, MAX_ARRAY_LENGTH, Object.class);
        mOptionsPool = Pools.synchronizedPool(Pools.newPool(this, maxPoolSize));
        mBufferPool  = Pools.synchronizedPool(Pools.<byte[]>newPool(maxPoolSize, 16384, byte.class));
        mContext.registerComponentCallbacks(this);
        ImageModule.__checkDumpSystemInfo(context);
    }

    /**
     * Equivalent to calling <tt>with(id).load(uri)</tt>.
     * @param id The xml resource id of the <tt>ImageLoader</tt>.
     * @param uri The uri to load.
     * @see #with(int)
     * @see ImageLoader#load(URI)
     */
    public final LoadRequest load(int id, URI uri) {
        return with(id).load(uri);
    }

    /**
     * Return an {@link ImageLoader} object associated with a resource id.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param id The xml resource id of the <tt>ImageLoader</tt>.
     * @return The <tt>ImageLoader</tt>.
     * @throws NotFoundException if the given <em>id</em> does not exist.
     * @see #load(int, URI)
     * @see ImageLoader#load(URI)
     */
    public final ImageLoader<URI, Image> with(int id) {
        DebugUtils.__checkUIThread("with");
        ImageLoader loader = mLoaderCache.get(id, null);
        if (loader == null) {
            DebugUtils.__checkStartMethodTracing();
            mLoaderCache.append(id, loader = XmlResources.load(mContext, id, this));
            DebugUtils.__checkStopMethodTracing("ImageModule", "Loads the ImageLoader - ID #0x" + Integer.toHexString(id));
        }

        return loader;
    }

    /**
     * Temporarily stops all actively running tasks with the specified image loader.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param id The xml resource id of the <tt>ImageLoader</tt>.
     * @see #resume(int)
     */
    public final void pause(int id) {
        DebugUtils.__checkUIThread("pause");
        final ImageLoader loader = mLoaderCache.get(id, null);
        if (loader != null) {
            loader.pause();
        }
    }

    /**
     * Resumes all actively running tasks associated with the specified image loader.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param id The xml resource id of the <tt>ImageLoader</tt>.
     * @see #pause(int)
     */
    public final void resume(int id) {
        DebugUtils.__checkUIThread("resume");
        final ImageLoader loader = mLoaderCache.get(id, null);
        if (loader != null) {
            loader.resume();
        }
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

    /**
     * Clears the {@link FileCache} and all cache files will be delete from filesystem.
     */
    public final void clearCacheFiles() {
        if (mFileCache instanceof LruFileCache) {
            ((LruFileCache)mFileCache).clearCache();
        }
    }

    public final void dump(Printer printer) {
        Pools.dumpPool(mParamsPool, printer);
        Pools.dumpPool(mBufferPool, printer);
        Pools.dumpPool(mOptionsPool, printer);
        Caches.dumpCache(mImageCache, mContext, printer);
        Caches.dumpCache(mFileCache, mContext, printer);

        final Resources res = mContext.getResources();
        dumpCache(printer, res, mResources, "Resources");
        dumpCache(printer, res, mLoaderCache, "ImageLoader");

        for (int i = mLoaderCache.size() - 1; i >= 0; --i) {
            mLoaderCache.valueAt(i).dump(mContext, printer);
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
        DebugUtils.__checkStartMethodTracing();
        if (mImageCache != null) {
            mImageCache.clear();
        }

        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            for (int i = mLoaderCache.size() - 1; i >= 0; --i) {
                mLoaderCache.valueAt(i).shutdown();
            }

            mResources.clear();
            mParamsPool.clear();
            mBufferPool.clear();
            mOptionsPool.clear();
            mLoaderCache.clear();
            ByteArrayPool.sInstance.clear();
        }

        DebugUtils.__checkStopMethodTracing("ImageModule", new StringBuilder(64).append("onTrimMemory - level = ").append(toString(level)).append('(').append(level).append("),").toString());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    }

    @Override
    public ImageLoader inflate(Context context, XmlPullParser parser) throws XmlPullParserException, ReflectiveOperationException {
        String className = parser.getName();
        if (className.equals("loader") && (className = parser.getAttributeValue(null, "class")) == null) {
            throw new XmlPullParserException(parser.getPositionDescription() + ": The <loader> tag requires a valid 'class' attribute");
        }

        final AttributeSet attrs = Xml.asAttributeSet(parser);
        final TypedArray a = context.obtainStyledAttributes(attrs, (int[])ClassUtils.getFieldValue(context, "ImageLoader"));
        final int flags = a.getInt((int)ClassUtils.getFieldValue(context, "ImageLoader_flags"), 0);
        final String name = a.getString((int)ClassUtils.getFieldValue(context, "ImageLoader_decoder"));
        a.recycle();

        final Cache imageCache = ((flags & FLAG_NO_MEMORY_CACHE) == 0 ? mImageCache : null);
        final FileCache fileCache = ((flags & FLAG_NO_FILE_CACHE) == 0 ? mFileCache : null);

        // Creates the image loader.
        final ImageLoader.ImageDecoder decoder = createImageDecoder(name, imageCache);
        if (className.equals("ImageLoader")) {
            return new ImageLoader(this, imageCache, fileCache, decoder);
        } else {
            return ClassUtils.newInstance(className, new Class[] { ImageModule.class, Cache.class, FileCache.class, ImageLoader.ImageDecoder.class }, this, imageCache, fileCache, decoder);
        }
    }

    /**
     * Return a {@link Binder} object associated with a resource id.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param id The xml resource id of the <tt>Binder</tt>.
     * @return The <tt>Binder</tt>.
     * @throws NotFoundException if the given <em>id</em> does not exist.
     */
    /* package */ final Object getBinder(int id) {
        DebugUtils.__checkUIThread("getBinder");
        Object result = mResources.get(id, null);
        if (result == null) {
            DebugUtils.__checkStartMethodTracing();
            mResources.append(id, result = XmlResources.loadBinder(mContext, id));
            DebugUtils.__checkStopMethodTracing("ImageModule", "Loads the Binder - ID #0x" + Integer.toHexString(id));
        }

        return result;
    }

    /**
     * Return a {@link Parameters} object associated with a resource id.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param id The xml resource id of the <tt>Parameters</tt>.
     * @return The <tt>Parameters</tt>.
     * @throws NotFoundException if the given <em>id</em> does not exist.
     */
    /* package */ final Object getParameters(int id) {
        DebugUtils.__checkUIThread("getParameters");
        Object result = mResources.get(id, null);
        if (result == null) {
            DebugUtils.__checkStartMethodTracing();
            mResources.append(id, result = XmlResources.loadParameters(mContext, id));
            DebugUtils.__checkStopMethodTracing("ImageModule", "Loads the Parameters - ID #0x" + Integer.toHexString(id));
        }

        return result;
    }

    /**
     * Return a {@link Transformer} object associated with a resource id.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param id The xml resource id of the <tt>Transformer</tt>.
     * @return The <tt>Transformer</tt>.
     * @throws NotFoundException if the given <em>id</em> does not exist.
     */
    /* package */ final Object getTransformer(int id) {
        DebugUtils.__checkUIThread("getTransformer");
        Object result = mResources.get(id, null);
        if (result == null) {
            DebugUtils.__checkStartMethodTracing();
            mResources.append(id, result = XmlResources.loadTransformer(mContext, id));
            DebugUtils.__checkStopMethodTracing("ImageModule", "Loads the Transformer - ID #0x" + Integer.toHexString(id));
        }

        return result;
    }

    private ImageLoader.ImageDecoder createImageDecoder(String className, Cache imageCache) throws ReflectiveOperationException {
        final BitmapPool bitmapPool = (imageCache != null ? imageCache.getBitmapPool() : null);
        if (!TextUtils.isEmpty(className)) {
            return ClassUtils.newInstance(className, new Class[] { Context.class, Pool.class, BitmapPool.class }, mContext, mOptionsPool, bitmapPool);
        } else if (imageCache instanceof LruImageCache) {
            return new ImageDecoder(mContext, mOptionsPool, bitmapPool);
        } else {
            return new BitmapDecoder(mContext, mOptionsPool, bitmapPool);
        }
    }

    private static int computeBufferPoolMaxSize(Executor executor) {
        final int maxPoolSize = (executor instanceof ThreadPoolExecutor ? ((ThreadPoolExecutor)executor).getMaximumPoolSize() : 4);
        return (maxPoolSize == Integer.MAX_VALUE ? 12 : maxPoolSize + 1);
    }

    private static String toString(int level) {
        switch (level) {
        case TRIM_MEMORY_COMPLETE:
            return "TRIM_MEMORY_COMPLETE";

        case TRIM_MEMORY_MODERATE:
            return "TRIM_MEMORY_MODERATE";

        case TRIM_MEMORY_BACKGROUND:
            return "TRIM_MEMORY_BACKGROUND";

        case TRIM_MEMORY_UI_HIDDEN:
            return "TRIM_MEMORY_UI_HIDDEN";

        case TRIM_MEMORY_RUNNING_LOW:
            return "TRIM_MEMORY_RUNNING_LOW";

        case TRIM_MEMORY_RUNNING_CRITICAL:
            return "TRIM_MEMORY_RUNNING_CRITICAL";

        case TRIM_MEMORY_RUNNING_MODERATE:
            return "TRIM_MEMORY_RUNNING_MODERATE";

        default:
            return Integer.toString(level);
        }
    }

    private static void __checkDumpSystemInfo(Context context) {
        DeviceUtils.dumpSystemInfo(context, new LogPrinter(Log.DEBUG, "ImageModule"));
    }

    private static void dumpCache(Printer printer, Resources res, SparseArray cache, String cacheName) {
        final int size = cache.size();
        if (size == 0) {
            return;
        }

        final StringBuilder result = new StringBuilder(130);
        final TypedValue value = new TypedValue();
        DebugUtils.dumpSummary(printer, result, 130, " Dumping %s cache [ size = %d ] ", cacheName, size);
        for (int i = 0; i < size; ++i) {
            res.getValue(cache.keyAt(i), value, true);
            final Object object = cache.valueAt(i);

            result.setLength(0);
            result.append("  ").append(value.string).append(" ==> ");
            if (object instanceof Parameters) {
                ((Parameters)object).dump(printer, result);
            } else if (object instanceof RoundedGIFTransformer) {
                ((RoundedGIFTransformer)object).dump(printer, result);
            } else if (object instanceof RoundedRectTransformer) {
                ((RoundedRectTransformer)object).dump(printer, result);
            } else {
                printer.println(DebugUtils.toString(object, result).toString());
            }
        }
    }

    /**
     * Class <tt>Builder</tt> to creates an {@link ImageModule}.
     */
    public static final class Builder<URI, Image> {
        private int mPriority;
        private int mPoolSize;
        private int mImageSize;
        private int mMaxThreads;
        private Object mFileCache;
        private Object mImageCache;
        private final Context mContext;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         */
        public Builder(Context context) {
            mContext  = context;
            mPriority = Process.THREAD_PRIORITY_DEFAULT;
        }

        /**
         * Sets the maximum number of images to allow in the internal image cache.
         * @param size The maximum number of images.
         * @return This builder.
         */
        public final Builder<URI, Image> setImageSize(int size) {
            mImageSize = size;
            return this;
        }

        /**
         * Sets the maximum number of bitmaps to allow in the internal {@link BitmapPool}.
         * @param size The maximum number of bitmaps.
         * @return This builder.
         */
        public final Builder<URI, Image> setPoolSize(int size) {
            mPoolSize = size;
            return this;
        }

        /**
         * Sets the maximum number of bytes to allow in the internal bitmap cache.
         * @param size The maximum number of bytes.
         * @return This builder.
         * @see #setMemorySize(int)
         */
        public final Builder<URI, Image> setMemorySize(int size) {
            mImageCache = size;
            return this;
        }

        /**
         * Sets the scale of memory of the internal bitmap cache, expressed as a
         * percentage of this application maximum memory of the current device.
         * @param scaleMemory The scale of memory of the bitmap cache.
         * @return This builder.
         * @see #setScaleMemory(float)
         */
        public final Builder<URI, Image> setScaleMemory(float scaleMemory) {
            mImageCache = scaleMemory;
            return this;
        }

        /**
         * Sets the image {@link Cache} to store the loaded images.
         * @param cache The image <tt>Cache</tt>.
         * @return This builder.
         */
        public final Builder<URI, Image> setImageCache(Cache<URI, Image> cache) {
            mImageCache = cache;
            return this;
        }

        /**
         * Sets the maximum number of files to allow in the internal {@link FileCache}.
         * @param size The maximum number of files.
         * @return This builder.
         */
        public final Builder<URI, Image> setFileSize(int size) {
            mFileCache = size;
            return this;
        }

        /**
         * Sets the {@link FileCache} to store the loaded image files.
         * @param cache The <tt>FileCache</tt>.
         * @return This builder.
         */
        public final Builder<URI, Image> setFileCache(FileCache cache) {
            mFileCache = cache;
            return this;
        }

        /**
         * Sets the priority to run the work thread at. The value supplied
         * must be from {@link Process} and not from {@link Thread}.
         * @param priority The priority.
         * @return This builder.
         */
        public final Builder<URI, Image> setPriority(int priority) {
            mPriority = priority;
            return this;
        }

        /**
         * Sets the maximum number of threads to allow in the internal thread pool.
         * @param maxThreads The maximum number of threads.
         * @return This builder.
         */
        public final Builder<URI, Image> setMaximumThreads(int maxThreads) {
            mMaxThreads = maxThreads;
            return this;
        }

        /**
         * Creates an {@link ImageModule} with the arguments supplied to this builder.
         * @return The <tt>ImageModule</tt>.
         */
        public final ImageModule<URI, Image> build() {
            final int maxThreads = (mMaxThreads > 0 ? mMaxThreads : ArrayUtils.rangeOf(Runtime.getRuntime().availableProcessors(), 3, 5));
            return new ImageModule(mContext, ThreadPool.createImageThreadPool(maxThreads, 60, TimeUnit.SECONDS, mPriority), createImageCache(), createFileCache());
        }

        private FileCache createFileCache() {
            if (mFileCache == null) {
                return null;
            } else if (mFileCache instanceof FileCache) {
                return (FileCache)mFileCache;
            } else {
                final int maxSize = (int)mFileCache;
                return (maxSize > 0 ? new LruFileCache(mContext, "._image_cache", maxSize) : null);
            }
        }

        private Cache createImageCache() {
            if (mImageCache == null) {
                return null;
            } else if (mImageCache instanceof Cache) {
                return (Cache)mImageCache;
            }

            final int maxSize;
            if (mImageCache instanceof Integer) {
                maxSize = (int)mImageCache;
            } else {
                final float scaleMemory = (float)mImageCache;
                DebugUtils.__checkError(Float.compare(scaleMemory, 1.0f) >= 0, "scaleMemory >= 1.0");
                maxSize = (int)(Runtime.getRuntime().maxMemory() * scaleMemory + 0.5f);
            }

            if (maxSize == 0) {
                return null;
            } else if (mImageSize == 0) {
                return createBitmapCache(maxSize);
            } else {
                return new LruImageCache(createBitmapCache(maxSize), new LruCache(mImageSize));
            }
        }

        private Cache createBitmapCache(int maxSize) {
            return (mPoolSize > 0 ? new LruBitmapCache2(maxSize, new LinkedBitmapPool(mPoolSize)) : new LruBitmapCache(maxSize));
        }
    }
}
