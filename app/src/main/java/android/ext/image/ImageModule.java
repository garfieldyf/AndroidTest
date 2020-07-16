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
import android.ext.content.Loader.Task;
import android.ext.content.res.XmlResources;
import android.ext.content.res.XmlResources.XmlResourceInflater;
import android.ext.image.ImageLoader.LoadRequest;
import android.ext.image.binder.GIFImageBinder;
import android.ext.image.binder.RoundedBitmapBinder;
import android.ext.image.binder.TransitionBinder;
import android.ext.image.decoder.BitmapDecoder;
import android.ext.image.decoder.ContactPhotoDecoder;
import android.ext.image.decoder.ImageDecoder;
import android.ext.image.params.Parameters;
import android.ext.image.params.SizeParameters;
import android.ext.util.ArrayUtils;
import android.ext.util.ClassUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.Pools;
import android.ext.util.Pools.ByteArrayPool;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.text.TextUtils;
import android.util.AttributeSet;
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
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class ImageModule<URI, Image> implements ComponentCallbacks2, Factory<Object[]>, XmlResourceInflater<ImageLoader> {
    private static final int FLAG_NO_FILE_CACHE   = 0x01;
    private static final int FLAG_NO_MEMORY_CACHE = 0x02;

    /* package */ static final int PARAMETERS  = 0;
    /* package */ static final int PLACEHOLDER = 1;
    /* package */ static final int PARAMS_LENGTH = 2;

    /**
     * The maximum number of tasks.
     */
    private static final int MAX_POOL_SIZE = 48;

    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /* package */ final Executor mExecutor;
    /* package */ final Pool<Task> mTaskPool;
    /* package */ final Pool<byte[]> mBufferPool;
    /* package */ final Pool<Options> mOptionsPool;
    /* package */ final Pool<Object[]> mParamsPool;

    private final FileCache mFileCache;
    private final Cache<URI, Image> mImageCache;
    private final SparseArray<Object> mResources;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param executor The {@link Executor} to executing load task.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the loaded images.
     * @param fileCache May be <tt>null</tt>. The {@link FileCache} to store the loaded image files.
     */
    /* package */ ImageModule(Context context, Executor executor, Cache<URI, Image> imageCache, FileCache fileCache) {
        final int maxPoolSize = ((ThreadPoolExecutor)executor).getMaximumPoolSize();
        mContext  = context.getApplicationContext();
        mExecutor = executor;
        mFileCache   = fileCache;
        mImageCache  = imageCache;
        mResources   = new SparseArray<Object>(8);
        mTaskPool    = ImageLoader.newTaskPool(MAX_POOL_SIZE);
        mParamsPool  = Pools.newPool(this, MAX_POOL_SIZE);
        mOptionsPool = Pools.synchronizedPool(Pools.newPool(Options::new, maxPoolSize));
        mBufferPool  = Pools.synchronizedPool(Pools.newPool(() -> new byte[16384], maxPoolSize));
        mContext.registerComponentCallbacks(this);
    }

    /**
     * Equivalent to calling <tt>module.get(id).load(uri)</tt>.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * <h3>Usage</h3>
     * <p>Here is an example:</p><pre>
     * module.load(R.xml.image_loader, uri)
     *       .placeholder(R.drawable.ic_placeholder)
     *       .into(imageView);</pre>
     * @param id The xml resource id of the <tt>ImageLoader</tt>.
     * @param uri The uri to load.
     * @see #get(int)
     * @see ImageLoader#load(URI)
     */
    public final LoadRequest load(int id, URI uri) {
        return get(id).load(uri);
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
    public final ImageLoader<URI, Image> get(int id) {
        DebugUtils.__checkUIThread("get");
        ImageLoader loader = (ImageLoader)mResources.get(id, null);
        if (loader == null) {
            DebugUtils.__checkStartMethodTracing();
            mResources.append(id, loader = XmlResources.load(mContext, id, this));
            DebugUtils.__checkStopMethodTracing("ImageModule", "Loads " + loader + " - ID #0x" + Integer.toHexString(id));
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
        final ImageLoader loader = (ImageLoader)mResources.get(id, null);
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
        final ImageLoader loader = (ImageLoader)mResources.get(id, null);
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

    public final void dump(Printer printer) {
        Pools.dumpPool(mParamsPool, printer);
        Pools.dumpPool(mTaskPool, printer);
        Pools.dumpPool(mOptionsPool, printer);
        Pools.dumpPool(mBufferPool, printer);
        Caches.dumpCache(mImageCache, mContext, printer);
        Caches.dumpCache(mFileCache, mContext, printer);

        final StringBuilder result = new StringBuilder(130);
        final Resources res = mContext.getResources();
        final TypedValue value = new TypedValue();
        final int size = mResources.size();
        DebugUtils.dumpSummary(printer, result, 130, " Dumping XmlResources cache [ size = %d ] ", size);
        for (int i = 0; i < size; ++i) {
            res.getValue(mResources.keyAt(i), value, true);
            final Object object = mResources.valueAt(i);

            result.setLength(0);
            result.append("  ").append(value.string).append(" ==> ");
            if (object instanceof Parameters) {
                ((Parameters)object).dump(printer, result);
            } else if (object instanceof GIFImageBinder) {
                ((GIFImageBinder)object).dump(printer, result);
            } else if (object instanceof TransitionBinder) {
                ((TransitionBinder)object).dump(printer, result);
            } else if (object instanceof RoundedBitmapBinder) {
                ((RoundedBitmapBinder)object).dump(printer, result);
            } else {
                printer.println(DebugUtils.toString(object, result).toString());
            }
        }

        result.setLength(0);
        SizeParameters.defaultParameters.dump(printer, result.append("  default ==> "));

        for (int i = 0; i < size; ++i) {
            final Object object = mResources.valueAt(i);
            if (object instanceof ImageLoader) {
                ((ImageLoader<?, ?>)object).dump(printer);
            }
        }
    }

    @Override
    public final Object[] newInstance() {
        return new Object[PARAMS_LENGTH];
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
            mImageCache.trimMemory(level);
        }

        if (mFileCache != null) {
            mFileCache.trimMemory(level);
        }

        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            for (int i = mResources.size() - 1; i >= 0; --i) {
                final Object value = mResources.valueAt(i);
                if (value instanceof ImageLoader) {
                    ((ImageLoader<?, ?>)value).shutdown();
                }
            }

            mTaskPool.clear();
            mResources.clear();
            mParamsPool.clear();
            mBufferPool.clear();
            mOptionsPool.clear();
            ByteArrayPool.sInstance.clear();
        }

        DebugUtils.__checkStopMethodTracing("ImageModule", new StringBuilder(64).append("onTrimMemory - level = ").append(toString(level)).append(',').toString());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    }

    @Override
    public final ImageLoader inflate(Context context, XmlPullParser parser) throws XmlPullParserException, ReflectiveOperationException {
        String className = parser.getName();
        if (className.equals("loader") && (className = parser.getAttributeValue(null, "class")) == null) {
            throw new XmlPullParserException(parser.getPositionDescription() + ": The <loader> tag requires a valid 'class' attribute");
        }

        final String packageName = context.getPackageName();
        final AttributeSet attrs = Xml.asAttributeSet(parser);
        final TypedArray a = context.obtainStyledAttributes(attrs, ClassUtils.getFieldValue(packageName, "ImageLoader"));
        final int flags = a.getInt(ClassUtils.getFieldValue(packageName, "ImageLoader_flags"), 0);
        final String name = a.getString(ClassUtils.getFieldValue(packageName, "ImageLoader_decoder"));
        a.recycle();

        final Cache imageCache = ((flags & FLAG_NO_MEMORY_CACHE) == 0 ? mImageCache : null);
        final FileCache fileCache = ((flags & FLAG_NO_FILE_CACHE) == 0 ? mFileCache : null);

        // Creates the image loader.
        if (className.equals("IconLoader")) {
            return new IconLoader(this, imageCache);
        } else if (className.equals("ImageLoader")) {
            return new ImageLoader(this, imageCache, fileCache, createImageDecoder(name, imageCache));
        }

        final Class<ImageLoader> clazz = (Class<ImageLoader>)Class.forName(className);
        try {
            return ClassUtils.newInstance(clazz, new Class[] { ImageModule.class, Cache.class }, this, imageCache);
        } catch (NoSuchMethodException e) {
            return ClassUtils.newInstance(clazz, new Class[] { ImageModule.class, Cache.class, FileCache.class, ImageLoader.ImageDecoder.class }, this, imageCache, fileCache, createImageDecoder(name, imageCache));
        }
    }

    /**
     * Returns the parameters associated with the <em>params</em>.
     * @param params The parameters, passed earlier by {@link ImageLoader#load}.
     * @return The parameters or <tt>null</tt>.
     * @see #getPlaceholder(Resources, Object[])
     */
    public static <T> T getParameters(Object[] params) {
        DebugUtils.__checkError(ArrayUtils.getSize(params) < (PARAMETERS + 1), "Invalid parameter - params == null || params.length(" + ArrayUtils.getSize(params) + ") < " + (PARAMETERS + 1));
        return (T)params[PARAMETERS];
    }

    /**
     * Returns the placeholder drawable associated with the <em>params</em>.
     * @param res The <tt>Resources</tt>.
     * @param params The parameters, passed earlier by {@link ImageLoader#load}.
     * @return The placeholder <tt>Drawable</tt> or <tt>null</tt>.
     * @see #getParameters(Object[])
     */
    @SuppressWarnings("deprecation")
    public static Drawable getPlaceholder(Resources res, Object[] params) {
        DebugUtils.__checkError(ArrayUtils.getSize(params) < (PLACEHOLDER + 1), "Invalid parameter - params == null || params.length(" + ArrayUtils.getSize(params) + ") < " + (PLACEHOLDER + 1));
        final Object placeholder = params[PLACEHOLDER];
        return (placeholder instanceof Integer ? res.getDrawable((int)placeholder) : (Drawable)placeholder);
    }

    /**
     * Return an object associated with a xml resource id.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param id The xml resource id.
     * @return The object.
     * @throws NotFoundException if the given <em>id</em> does not exist.
     */
    /* package */ final Object getResource(int id) {
        DebugUtils.__checkUIThread("getResource");
        Object result = mResources.get(id, null);
        if (result == null) {
            DebugUtils.__checkStartMethodTracing();
            mResources.append(id, result = XmlResources.load(mContext, id));
            DebugUtils.__checkStopMethodTracing("ImageModule", "Loads " + result + " - ID #0x" + Integer.toHexString(id));
        }

        return result;
    }

    private ImageLoader.ImageDecoder createImageDecoder(String className, Cache imageCache) throws ReflectiveOperationException {
        final BitmapPool bitmapPool = (imageCache != null ? imageCache.getBitmapPool() : null);
        if (TextUtils.isEmpty(className)) {
            return new BitmapDecoder(this, bitmapPool);
        }

        switch (className) {
        case "BitmapDecoder":
            return new BitmapDecoder(this, bitmapPool);

        case "ImageDecoder":
            return new ImageDecoder(this, bitmapPool);

        case "ContactPhotoDecoder":
            return new ContactPhotoDecoder(this, bitmapPool);

        default:
            return createImageDecoder(className, bitmapPool);
        }
    }

    private ImageLoader.ImageDecoder createImageDecoder(String className, BitmapPool bitmapPool) throws ReflectiveOperationException {
        final Class<ImageLoader.ImageDecoder> clazz = (Class<ImageLoader.ImageDecoder>)Class.forName(className);
        try {
            return ClassUtils.newInstance(clazz, new Class[] { ImageModule.class, BitmapPool.class }, this, bitmapPool);
        } catch (NoSuchMethodException e) {
            return ClassUtils.newInstance(clazz, new Class[] { ImageModule.class }, this);
        }
    }

    private static String toString(int level) {
        switch (level) {
        case TRIM_MEMORY_COMPLETE:
            return level + "(TRIM_MEMORY_COMPLETE)";

        case TRIM_MEMORY_MODERATE:
            return level + "(TRIM_MEMORY_MODERATE)";

        case TRIM_MEMORY_BACKGROUND:
            return level + "(TRIM_MEMORY_BACKGROUND)";

        case TRIM_MEMORY_UI_HIDDEN:
            return level + "(TRIM_MEMORY_UI_HIDDEN)";

        case TRIM_MEMORY_RUNNING_LOW:
            return level + "(TRIM_MEMORY_RUNNING_LOW)";

        case TRIM_MEMORY_RUNNING_CRITICAL:
            return level + "(TRIM_MEMORY_RUNNING_CRITICAL)";

        case TRIM_MEMORY_RUNNING_MODERATE:
            return level + "(TRIM_MEMORY_RUNNING_MODERATE)";

        default:
            return Integer.toString(level);
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
            mPriority = Process.THREAD_PRIORITY_BACKGROUND;
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
         * Sets the maximum number of bitmaps to allow in the internal {@link BitmapPool}.
         * @param size The maximum number of bitmaps.
         * @return This builder.
         */
        public final Builder<URI, Image> setBitmapPoolSize(int size) {
            mPoolSize = size;
            return this;
        }

        /**
         * Sets the priority to run the work thread at. The value supplied
         * must be from {@link Process} and not from {@link Thread}.
         * @param priority The priority.
         * @return This builder.
         */
        public final Builder<URI, Image> setThreadPriority(int priority) {
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
            final int maxThreads = (mMaxThreads > 0 ? mMaxThreads : ArrayUtils.rangeOf(Runtime.getRuntime().availableProcessors(), 2, 4));
            final Executor executor = ThreadPool.createImageThreadPool(maxThreads, 60, TimeUnit.SECONDS, mPriority);
            return new ImageModule(mContext, executor, createImageCache(), createFileCache(executor));
        }

        private FileCache createFileCache(Executor executor) {
            if (mFileCache == null) {
                return null;
            } else if (mFileCache instanceof FileCache) {
                return (FileCache)mFileCache;
            } else {
                final int maxSize = (int)mFileCache;
                return (maxSize > 0 ? new LruFileCache(executor, FileUtils.getCacheDir(mContext, "._image_cache!"), maxSize) : null);
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
                DebugUtils.__checkError(Float.compare(scaleMemory, 1.0f) >= 0, "Invalid parameter - scaleMemory(" + scaleMemory + ") >= 1.0");
                maxSize = (int)(Runtime.getRuntime().maxMemory() * scaleMemory + 0.5f);
            }

            if (maxSize <= 0) {
                return null;
            } else if (mImageSize <= 0) {
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
