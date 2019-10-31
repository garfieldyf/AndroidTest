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
import android.ext.cache.ImageCache;
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
import android.ext.util.Pools;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
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
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the loaded images.
     * @param fileCache May be <tt>null</tt>. The {@link FileCache} to store the loaded image files.
     * @see #ImageModule(Context, Executor, Cache, FileCache)
     */
    public ImageModule(Context context, Cache<URI, Image> imageCache, FileCache fileCache) {
        this(context, ThreadPool.createImageThreadPool(ArrayUtils.rangeOf(Runtime.getRuntime().availableProcessors(), 3, 5), 60, TimeUnit.SECONDS), imageCache, fileCache);
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
        mFileCache   = fileCache;
        mImageCache  = imageCache;
        mResources   = new SparseArray<Object>(8);
        mLoaderCache = new SparseArray<ImageLoader>(2);
        mParamsPool  = Pools.newPool(48, MAX_ARRAY_LENGTH, Object.class);
        mOptionsPool = Pools.synchronizedPool(Pools.newPool(this, maxPoolSize));
        mBufferPool  = Pools.synchronizedPool(Pools.<byte[]>newPool(maxPoolSize, 16384, byte.class));
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
            return (ImageLoader)ClassUtils.getConstructor(className, ImageModule.class, Cache.class, FileCache.class, ImageLoader.ImageDecoder.class).newInstance(this, imageCache, fileCache, decoder);
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

    /**
     * Returns a new {@link FileCache} instance.
     * @param maxSize The maximum number of files to allow in the <tt>FileCache</tt>.
     */
    protected static FileCache createFileCache(Context context, int maxSize) {
        return (maxSize > 0 ? new LruFileCache(context, "._image_cache", maxSize) : null);
    }

    private ImageLoader.ImageDecoder createImageDecoder(String className, Cache imageCache) throws ReflectiveOperationException {
        final BitmapPool bitmapPool = (imageCache instanceof ImageCache ? ((ImageCache)imageCache).getBitmapPool() : null);
        if (!TextUtils.isEmpty(className)) {
            return (ImageLoader.ImageDecoder)ClassUtils.getConstructor(className, Context.class, Pool.class, BitmapPool.class).newInstance(mContext, mOptionsPool, bitmapPool);
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
}
