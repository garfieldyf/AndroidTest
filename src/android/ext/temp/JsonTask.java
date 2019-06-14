package android.ext.temp;

import java.io.File;
import org.json.JSONObject;
import android.app.Activity;
import android.ext.content.AsyncCacheLoader.LoadParams;
import android.ext.content.AsyncCacheTask;
import android.ext.net.DownloadRequest;
import android.ext.util.JsonUtils;
import android.ext.util.UriUtils;
import android.util.Log;
import com.tencent.test.MainApplication;

public class JsonTask extends AsyncCacheTask<String> {
    private final LoadParams<String> mLoadParams;

    public JsonTask(Activity ownerActivity, LoadParams<String> params) {
        super(ownerActivity);
        mLoadParams = params;
    }

    @Override
    protected File getCacheFile(String[] params) {
        return mLoadParams.getCacheFile(mContext, params[0]);
    }

    @Override
    protected DownloadRequest newDownloadRequest(String[] params) throws Exception {
        return mLoadParams.newDownloadRequest(mContext, params[0]);
    }

    @Override
    protected Object parseResult(String[] params, File cacheFile) throws Exception {
        final Object uri = (cacheFile.exists() ? cacheFile : UriUtils.getAssetUri("json_cache/title"));
        final JSONObject result = JsonUtils.parse(MainApplication.sInstance, uri, this);
        return (JsonUtils.optInt(result, "retCode", 0) == 200 ? result : null);
    }

    @Override
    protected void onPostExecute(Object result) {
        final Activity activity = getOwnerActivity();
        if (activity == null) {
            // The owner activity has been destroyed or release by the GC.
            return;
        }

        if (isInvalidResult(result)) {
            Log.i("abc", "JsonTask - Load invalid result, do not update UI.");
            return;
        }

        if (result != null) {
            Log.i("abc", "JsonTask - Load Succeeded, Update UI.");
            // Toast.makeText(activity, "JsonTask - Load Succeeded, Update UI.", Toast.LENGTH_SHORT).show();
        } else {
            Log.i("abc", "JsonTask - Load Failed, Show error UI.");
        }
    }
}
