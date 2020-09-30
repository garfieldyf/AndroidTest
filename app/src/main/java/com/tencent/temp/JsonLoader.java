package com.tencent.temp;

import android.content.Context;
import android.ext.content.ResourceLoader.LoadParams;
import android.ext.content.ResourceLoader.OnLoadCompleteListener;
import android.ext.json.JSONObject;
import android.ext.json.JSONUtils;
import android.ext.net.DownloadRequest;
import android.ext.util.Cancelable;
import android.ext.util.FileUtils;
import android.util.Log;
import com.tencent.test.MainApplication;
import java.io.File;

public final class JsonLoader {
    public static final OnLoadCompleteListener<String, JSONObject> sListener = new OnLoadCompleteListener<String, JSONObject>() {
        @Override
        public void onLoadComplete(String[] urls, JSONObject result) {
            if (result != null) {
                Log.i("ResourceLoader", "JsonLoader - Load Succeeded, Update UI.");
                //result.dump(new LogPrinter(Log.DEBUG, "ResourceLoader"));
            } else {
                Log.i("ResourceLoader", "JsonLoader - Load Failed, Show error UI.");
            }
        }
    };

    public static final class JSONLoadParams implements LoadParams<String, JSONObject> {
        private final File mCacheFile;

        public JSONLoadParams(String cacheFileName) {
            mCacheFile = new File(FileUtils.getCacheDir(MainApplication.sInstance, ".json_cache"), cacheFileName);
        }

        @Override
        public File getCacheFile(Context context, String[] urls) {
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException ignored) {
//            }
            return mCacheFile;
        }

        @Override
        public DownloadRequest newDownloadRequest(Context context, String[] urls) throws Exception {
            return new DownloadRequest(urls[0]).connectTimeout(30000).readTimeout(30000).accept("*/*");
        }

        @Override
        public JSONObject parseResult(Context context, String[] urls, File cacheFile, Cancelable cancelable) throws Exception {
            final JSONObject result = JSONUtils.parse(context, cacheFile, cancelable);
//            result.put("retCode", "401");
            return (JSONUtils.optInt(result, "retCode", 0) == 200 ? result : null);
        }
    }
}
