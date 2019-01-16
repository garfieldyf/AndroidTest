package android.ext.temp;

import org.json.JSONObject;
import android.app.Activity;
import android.ext.content.AsyncJsonLoader;
import android.ext.util.JsonUtils;
import android.util.Log;
import android.util.Pair;
import com.tencent.test.MainApplication;

public class JsonLoader extends AsyncJsonLoader<String, JSONObject> {
    public JsonLoader(Object owner) {
        super(MainApplication.sInstance.getExecutor(), owner);
    }

    @Override
    protected void onStartLoading(String url, LoadParams<String>[] params) {
        // Show loading UI.
        Log.i("abc", "Show loading UI.");
    }

    @Override
    protected void onProgressUpdate(String url, LoadParams<String>[] params, Object[] values) {
        final Activity activity = getOwner();
        if (activity == null || activity.isDestroyed()) {
            return;
        }

        final JSONObject result = (JSONObject)values[0];
        if (result == null) {
            // Show loading UI.
            Log.i("abc", "Show loading UI.");
        } else {
            // Loading cache file succeeded, update UI.
            Log.i("abc", "Hit Cache Update UI.");
        }
    }

    @Override
    protected void onLoadComplete(String url, LoadParams<String>[] params, Pair<JSONObject, Boolean> result) {
        final Activity activity = getOwner();
        if (activity == null || activity.isDestroyed()) {
            return;
        }

        // Hide loading UI, if need.
        if (result.first != null) {
            // Loading succeeded, update UI.
            Log.i("abc", "Load Succeeded Update UI.");
        } else if (!result.second) {
            // Loading failed and file cache not hit, show error UI.
            Log.i("abc", "Show error UI.");
        }
    }

    @Override
    protected boolean validateResult(String url, LoadParams<String> params, JSONObject result) {
        return (JsonUtils.optInt(result, "retCode", 0) == 200);
    }
}
