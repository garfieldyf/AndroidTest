package android.ext.temp;

import java.io.File;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.ext.content.AsyncCacheLoader;
import android.ext.net.DownloadRequest;
import android.ext.util.Cancelable;
import android.ext.util.JsonUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.StringUtils;
import android.ext.util.UriUtils;
import android.util.Log;
import com.tencent.test.MainApplication;

public final class JsonLoader extends AsyncCacheLoader<String, JSONObject> {
    public JsonLoader(Activity ownerActivity) {
        super(MainApplication.sInstance.getExecutor(), ownerActivity);
    }

    @Override
    protected void onStartLoading(String url, LoadParams<String, JSONObject>[] params) {
        final File cacheFile = params[0].getCacheFile(url);
        if (cacheFile == null || !cacheFile.exists()) {
            // Show loading UI.
            Log.i("abc", "JsonLoader - Show loading UI.");
        }
    }

    @Override
    protected void onLoadComplete(String url, LoadParams<String, JSONObject>[] params, LoadResult<JSONObject> result) {
        final Activity activity = getOwnerActivity();
        if (activity == null) {
            // The owner activity has been destroyed or release by the GC.
            return;
        }

        // Hide loading UI, if need.
        if (result.result != null) {
            // Loading succeeded, update UI.
            Log.i("abc", "JsonLoader - Load Succeeded Update UI. - " + getName(result.result));
            // Toast.makeText(activity, "JsonLoader - Load Succeeded Update UI.", Toast.LENGTH_SHORT).show();
        } else if (!result.hitCache) {
            // Loading failed and file cache not hit, show error UI.
            Log.i("abc", "JsonLoader - Show error UI.");
        }
    }

    private static String getName(JSONObject result) {
        final JSONArray rows = JsonUtils.optJSONArray(JsonUtils.optJSONObject(result, "data"), "rows");
        return JsonUtils.optString(JsonUtils.optJSONObject(rows, 0), "name", "null") + "  " + JsonUtils.optString(JsonUtils.optJSONObject(rows, 1), "name", "null");
    }

    public static class URLLoadParams extends LoadParams<String, JSONObject> {
        @Override
        public DownloadRequest newDownloadRequest(String url) throws Exception {
            return new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
        }
    }

    public static class JsonLoadParams extends URLLoadParams {
        private final File mCacheDir;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         */
        public JsonLoadParams(Context context) {
            mCacheDir = new File(context.getFilesDir(), ".json_files");
        }

        @Override
        public File getCacheFile(String url) {
            return new File(mCacheDir, StringUtils.toHexString(MessageDigests.computeString(url, Algorithm.SHA1)));
        }

        @Override
        public JSONObject parseResult(String key, File cacheFile, Cancelable cancelable) throws Exception {
            final Object uri = (cacheFile.exists() ? cacheFile : UriUtils.getAssetUri("json_cache/content"));
            final JSONObject result = JsonUtils.parse(MainApplication.sInstance, uri, cancelable);
            if (JsonUtils.optInt(result, "retCode", 0) == 200) {
                return result;
            } else {
                Log.e("abc", "Couldn't load JSON data from  - " + cacheFile.getPath());
                return null;
            }
        }
    }
}
