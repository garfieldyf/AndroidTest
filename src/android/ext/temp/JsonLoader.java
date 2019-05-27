package android.ext.temp;

import java.io.File;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.ext.content.AsyncCacheLoader;
import android.ext.net.DownloadPostRequest;
import android.ext.net.DownloadRequest;
import android.ext.util.Cancelable;
import android.ext.util.JsonUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.StringUtils;
import android.ext.util.UriUtils;
import android.util.Log;
import com.tencent.test.MainApplication;

public final class JsonLoader extends AsyncCacheLoader<String> {
    public JsonLoader(Activity ownerActivity) {
        super(ownerActivity, MainApplication.sInstance.getExecutor());
    }

    @Override
    protected void onLoadComplete(String key, LoadParams<String>[] params, Object result) {
        final Activity activity = getOwnerActivity();
        if (activity == null) {
            // The owner activity has been destroyed or release by the GC.
            return;
        }

        if (result == this) {
            //Log.i("abc", "JsonLoader - Load EMPTY_RESULT, do not update UI.");
            return;
        }

        if (result != null) {
            Log.i("abc", "JsonLoader - Load Succeeded, Update UI - " + getName((JSONObject)result));
            // Toast.makeText(activity, "JsonLoader - Load Succeeded, Update UI.", Toast.LENGTH_SHORT).show();
        } else {
            Log.i("abc", "JsonLoader - Load Failed, Show error UI.");
        }
    }

    private static String getName(JSONObject result) {
        final JSONArray rows = JsonUtils.optJSONArray(JsonUtils.optJSONObject(result, "data"), "rows");
        return JsonUtils.optString(JsonUtils.optJSONObject(rows, 0), "name", "null") + "  " + JsonUtils.optString(JsonUtils.optJSONObject(rows, 1), "name", "null");
    }

    public static class URLLoadParams extends LoadParams<String> {
        @Override
        public DownloadRequest newDownloadRequest(Context context, String url) throws Exception {
            return new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
        }
    }

    public static class JsonLoadParams extends URLLoadParams {
        private final File mCacheDir;

        /**
         * Constructor
         */
        public JsonLoadParams() {
            mCacheDir = new File(MainApplication.sInstance.getFilesDir(), ".json_files");
        }

        @Override
        public File getCacheFile(Context context, String url) {
            return new File(mCacheDir, StringUtils.toHexString(MessageDigests.computeString(url, Algorithm.SHA1)));
        }

        @Override
        public Object parseResult(Context context, String key, File cacheFile, Cancelable cancelable) throws Exception {
            final Object uri = (cacheFile.exists() ? cacheFile : UriUtils.getAssetUri("json_cache/content"));
            final JSONObject result = JsonUtils.parse(MainApplication.sInstance, uri, cancelable);
            return (JsonUtils.optInt(result, "retCode", 0) == 200 ? result : null);
        }
    }

    public static class AppLoadParams extends JsonLoadParams {
//        @Override
//        public File getCacheFile(String url) {
//            return null;
//        }

        @Override
        public DownloadRequest newDownloadRequest(Context context, String url) throws Exception {
            String s = "{\"channelId\": 5001,    \"cpuType\": \"638_cvte\",  \"deviceCode\": \"cvte\",   \"mac\": \"28:76:cd:01:d9:ea\",     \"packages\": [\"com.jrm.localmm\", \"com.tencent.qqmusictv\", \"com.ocj.tv\"],     \"requestId\": \"07d83d68-9cfe-4746-b172-f02b7eacc99f\",    \"romVersion\": \"5.0.0_2019-04-10_11-41\",     \"version\": \"4.1.1.1\" }";
            return new DownloadPostRequest(url)
                .post(s)
                .readTimeout(30000)
                .connectTimeout(30000)
                .contentType("application/json");
        }

        @Override
        public Object parseResult(Context context, String key, File cacheFile, Cancelable cancelable) throws Exception {
            final JSONObject result = JsonUtils.parse(MainApplication.sInstance, cacheFile, cancelable);
            return (JsonUtils.optInt(result, "retCode", 0) == 200 ? result : null);
        }
    }
}
