package android.ext.temp;

import org.json.JSONObject;
import android.app.Activity;
import android.ext.content.AsyncJsonLoader;
import android.util.Log;
import android.util.Pair;
import com.tencent.test.MainApplication;

public final class JsonLoader extends AsyncJsonLoader<String, JSONObject> {
    public JsonLoader(Activity ownerActivity) {
        super(MainApplication.sInstance.getExecutor(), ownerActivity);
    }

    @Override
    protected void onStartLoading(String url, LoadParams<String>[] params) {
        final Activity activity = getOwner();
        if (activity != null && !activity.isDestroyed()) {
            // Show loading UI.
            Log.i("abc", "Show loading UI.");
        }
    }

    @Override
    protected void onLoadComplete(String url, LoadParams<String>[] params, Pair<JSONObject, Boolean> result) {
        final Activity activity = getOwner();
        if (activity == null || activity.isDestroyed()) {
            // The owner activity has been destroyed or release by the GC.
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
        return (result != null && result.optInt("retCode") == 200);
    }
}
