package android.ext.temp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.concurrent.Executor;
import android.content.Context;
import android.ext.content.AsyncTaskLoader;
import android.ext.net.DownloadRequest;
import android.ext.temp.AsyncJsonLoader.LoadParams;
import android.ext.util.FileUtils;
import android.ext.util.JSONUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.StringUtils;
import android.ext.util.UriUtils;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

public class AsyncJsonLoader<URI, Result> extends AsyncTaskLoader<URI, LoadParams, Result> {
    public final Context mContext;

    public AsyncJsonLoader(Context context, Executor executor) {
        super(executor);
        mContext = context.getApplicationContext();
    }

    public AsyncJsonLoader(Context context, Executor executor, Object owner) {
        super(executor, owner);
        mContext = context.getApplicationContext();
    }

    @Override
    protected Result loadInBackground(Task<?, ?> task, URI uri, LoadParams[] params) {
        try {
            if (!matchScheme(uri)) {
                return loadFromUri(task, uri);
            }

            final String url = uri.toString();
            final LoadParams param = params[0];
            final String cacheFile = param.getCacheFile(url);
            return (TextUtils.isEmpty(cacheFile) ? param.newDownloadRequest(url).<Result>download(task, null) : download(task, url, cacheFile, param));
        } catch (Exception e) {
            Log.e(getClass().getName(), new StringBuilder("Couldn't load JSON from - '").append(uri).append("'\n").append(e).toString());
            return null;
        }
    }

    protected boolean matchScheme(URI uri) {
        return UriUtils.matchScheme(uri);
    }

    protected Result loadFromCache(Task<?, ?> task, URI uri, LoadParams params) {
        try {
            final String cacheFile = params.getCacheFile(uri.toString());
            return (TextUtils.isEmpty(cacheFile) ? null : loadFromUri(task, cacheFile));
        } catch (Exception e) {
            return null;
        }
    }

    private Result loadFromUri(Task<?, ?> task, Object uri) throws Exception {
        final JsonReader reader = new JsonReader(new InputStreamReader(UriUtils.openInputStream(mContext, uri)));
        try {
            return JSONUtils.newInstance(reader, task);
        } finally {
            reader.close();
        }
    }

    private Result download(Task<?, ?> task, String url, String cacheFile, LoadParams params) throws Exception {
        final String tempFile = new StringBuilder(128).append(cacheFile, 0, cacheFile.lastIndexOf('/') + 1).append(Thread.currentThread().hashCode()).toString();
        final int statusCode  = params.newDownloadRequest(url).download(tempFile, task, null);

        Result result = null;
        if (statusCode == HttpURLConnection.HTTP_OK && !isTaskCancelled(task) && FileUtils.moveFile(tempFile, cacheFile) == 0) {
            result = loadFromUri(task, cacheFile);
        } else {
            FileUtils.deleteFiles(tempFile, false);
        }

        return result;
    }

    public static class LoadParams {
        public String getCacheFile(String url) {
            return null;
        }

        public DownloadRequest newDownloadRequest(String url) throws IOException {
            return new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
        }
    }

    public static class CacheLoadParams extends LoadParams {
        protected final String mCacheDir;

        public CacheLoadParams(String cacheDir) {
            mCacheDir = cacheDir;
        }

        public CacheLoadParams(Context context) {
            mCacheDir = FileUtils.getCacheDir(context, ".json_cache").getPath();
        }

        @Override
        public String getCacheFile(String url) {
            final byte[] hash = MessageDigests.computeFile(url, Algorithm.SHA1);
            return StringUtils.toHexString(new StringBuilder(128).append(mCacheDir).append('/'), hash, 0, hash.length, true).toString();
        }
    }
}
