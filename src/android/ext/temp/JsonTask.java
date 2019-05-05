package android.ext.temp;

import java.io.File;
import org.json.JSONObject;
import android.app.Activity;
import android.ext.content.AsyncJsonLoader.LoadParams;
import android.ext.content.AsyncJsonLoader.LoadResult;
import android.ext.net.AsyncJsonTask;
import android.ext.net.DownloadRequest;
import android.ext.util.JsonUtils;
import android.ext.util.UriUtils;
import android.util.Log;
import com.tencent.test.MainApplication;

public class JsonTask extends AsyncJsonTask<String, JSONObject> {
    private final LoadParams<String, JSONObject> mLoadParams;

    public JsonTask(Activity ownerActivity, LoadParams<String, JSONObject> params) {
        super(ownerActivity);
        mLoadParams = params;
    }

    @Override
    protected File getCacheFile(String[] params) {
        return mLoadParams.getCacheFile(params[0]);
    }

    @Override
    protected boolean validateResult(String[] params, JSONObject result) {
        return mLoadParams.validateResult(params[0], result);
    }

    @Override
    protected JSONObject loadFromCache(String[] params, File cacheFile) throws Exception {
        final Object uri;
        if (cacheFile.exists()) {
            uri = cacheFile;
        } else {
            uri = UriUtils.getAssetUri("json_cache/title");
        }

        Log.i("abc", uri.toString());
        return JsonUtils.parse(MainApplication.sInstance, uri, this);
    }

    @Override
    protected DownloadRequest newDownloadRequest(String[] params) throws Exception {
        return mLoadParams.newDownloadRequest(params[0]);
    }

    @Override
    protected void onPostExecute(LoadResult<JSONObject> result) {
        final Activity activity = getOwnerActivity();
        if (activity == null) {
            // The owner activity has been destroyed or release by the GC.
            return;
        }

        // Hide loading UI, if need.
        if (result.result != null) {
            // Loading succeeded, update UI.
            Log.i("abc", "JsonTask - Load Succeeded Update UI.");
            // Toast.makeText(activity, "JsonTask - Load Succeeded Update UI.", Toast.LENGTH_SHORT).show();
        } else if (!result.hitCache) {
            // Loading failed and file cache not hit, show error UI.
            Log.i("abc", "JsonTask - Show error UI.");
        }
    }
}
