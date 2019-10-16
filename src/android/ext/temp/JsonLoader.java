package android.ext.temp;

import java.io.File;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.ext.content.CachedTaskLoader.LoadParams;
import android.ext.content.CachedTaskLoader.OnLoadCompleteListener;
import android.ext.net.DownloadPostRequest;
import android.ext.net.DownloadRequest;
import android.ext.util.Cancelable;
import android.ext.util.JSONUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.StringUtils;
import android.ext.util.UriUtils;
import android.util.Log;
import com.tencent.test.MainApplication;

public final class JsonLoader {

    static String getName(JSONObject result) {
        final JSONArray rows = JSONUtils.optJSONArray(JSONUtils.optJSONObject(result, "data"), "rows");
        return JSONUtils.optString(JSONUtils.optJSONObject(rows, 0), "name", "null") + "  " + JSONUtils.optString(JSONUtils.optJSONObject(rows, 1), "name", "null");
    }

    public static final OnLoadCompleteListener<String, JSONObject> sListener = new OnLoadCompleteListener<String, JSONObject>() {
        @Override
        public void onLoadComplete(String key, LoadParams<String, JSONObject> loadParams, Object cookie, JSONObject result) {
            if (result != null) {
                Log.i("abc", "JsonLoader - Load Succeeded, Update UI - " + getName(result));
                // Toast.makeText(activity, "JsonLoader - Load Succeeded, Update UI.", Toast.LENGTH_SHORT).show();
            } else {
                Log.i("abc", "JsonLoader - Load Failed, Show error UI.");
            }
        }
    };

    public static class URLLoadParams implements LoadParams<String, JSONObject> {
        @Override
        public File getCacheFile(Context context, String key) {
            return null;
        }

        @Override
        public DownloadRequest newDownloadRequest(Context context, String url) throws Exception {
            return new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
        }

        @Override
        public JSONObject parseResult(Context context, String key, File cacheFile, Cancelable cancelable) throws Exception {
            return JSONUtils.parse(context, cacheFile, cancelable);
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
        public JSONObject parseResult(Context context, String key, File cacheFile, Cancelable cancelable) throws Exception {
            final Object uri = (cacheFile.exists() ? cacheFile : UriUtils.getAssetUri("json_cache/content"));
            final JSONObject result = JSONUtils.parse(context, uri, cancelable);
            return (JSONUtils.optInt(result, "retCode", 0) == 200 ? result : null);
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
        public JSONObject parseResult(Context context, String key, File cacheFile, Cancelable cancelable) throws Exception {
            final JSONObject result = JSONUtils.parse(context, cacheFile, cancelable);
            return (JSONUtils.optInt(result, "retCode", 0) == 200 ? result : null);
        }
    }
}
