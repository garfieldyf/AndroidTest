package android.ext.temp;

import java.io.File;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Activity;
import android.ext.content.AsyncJsonLoader;
import android.ext.util.JsonUtils;
import android.util.Log;
import android.util.Pair;
import com.tencent.test.MainApplication;

public final class JsonLoader extends AsyncJsonLoader<String, JSONObject> {
    public JsonLoader(Activity ownerActivity) {
        super(MainApplication.sInstance.getExecutor(), ownerActivity);
    }

    @Override
    protected void onStartLoading(String url, LoadParams<String>[] params) {
        final File cacheFile = params[0].getCacheFile(url);
        if (cacheFile == null || !cacheFile.exists()) {
            // Show loading UI.
            Log.i("abc", "JsonLoader - Show loading UI.");
        }
    }

    @Override
    protected boolean validateResult(String url, LoadParams<String> params, JSONObject result) {
        return (result != null && result.optInt("retCode") == 200);
    }

    @Override
    protected void onLoadComplete(String url, LoadParams<String>[] params, Pair<JSONObject, Boolean> result) {
        final Activity activity = getOwnerActivity();
        if (activity == null) {
            // The owner activity has been destroyed or release by the GC.
            return;
        }

        // Hide loading UI, if need.
        if (result.first != null) {
            // Loading succeeded, update UI.
            Log.i("abc", "JsonLoader - Load Succeeded Update UI. - " + getName(result.first));
            //Toast.makeText(activity, "JsonLoader - Load Succeeded Update UI.", Toast.LENGTH_SHORT).show();
        } else if (!result.second) {
            // Loading failed and file cache not hit, show error UI.
            Log.i("abc", "JsonLoader - Show error UI.");
        }
    }

    private static String getName(JSONObject result) {
        final JSONArray rows = JsonUtils.optJSONArray(JsonUtils.optJSONObject(result, "data"), "rows");
        return JsonUtils.optString(JsonUtils.optJSONObject(rows, 0), "name", "null") + "  " + JsonUtils.optString(JsonUtils.optJSONObject(rows, 1), "name", "null");
    }
}
