package com.tencent.temp;

import android.content.Context;
import android.ext.content.ResourceLoader.LoadParams;
import android.ext.content.ResourceLoader.OnLoadCompleteListener;
import android.ext.json.JSONObject;
import android.ext.json.JSONUtils;
import android.ext.net.DownloadRequest;
import android.ext.util.Cancelable;
import android.ext.util.FileUtils;
import android.ext.util.UriUtils;
import android.util.Log;
import com.tencent.test.MainApplication;
import java.io.File;
import java.io.IOException;

public final class JsonLoader {
    public static final OnLoadCompleteListener<String, JSONObject> sListener = new OnLoadCompleteListener<String, JSONObject>() {
        @Override
        public void onLoadComplete(String key, LoadParams<String, JSONObject> loadParams, Object cookie, JSONObject result) {
            if (result != null) {
                Log.i("ResourceLoader", "JsonLoader - Load Succeeded, Update UI.");
            } else {
                Log.i("ResourceLoader", "JsonLoader - Load Failed, Show error UI.");
            }
        }
    };

    public static class URLLoadParams implements LoadParams<String, JSONObject> {
        @Override
        public DownloadRequest newDownloadRequest(Context context, String url) throws Exception {
            return new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
        }

        @Override
        public JSONObject parseResult(Context context, String url, File cacheFile, Cancelable cancelable) throws Exception {
            final JSONObject result = newDownloadRequest(context, url).download(cancelable);
            return (JSONUtils.optInt(result, "retCode", 0) == 200 ? result : null);
        }
    }

    public static class JsonLoadParams extends URLLoadParams {
        private final File mCacheFile;

        /**
         * Constructor
         */
        public JsonLoadParams(String cacheFileName) {
            mCacheFile = new File(FileUtils.getCacheDir(MainApplication.sInstance, ".json_cache"), cacheFileName);
        }

        @Override
        public File getCacheFile(Context context, String url) {
            if (!mCacheFile.exists()) {
                try {
                    FileUtils.copyFile(context, UriUtils.getAssetUri("json_cache/content"), mCacheFile.getPath(), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return mCacheFile;
        }

        @Override
        public JSONObject parseResult(Context context, String key, File cacheFile, Cancelable cancelable) throws Exception {
            final JSONObject result = JSONUtils.parse(context, cacheFile, cancelable);
//            result.put("retCode", "401");
            return (JSONUtils.optInt(result, "retCode", 0) == 200 ? result : null);
        }
    }
}
