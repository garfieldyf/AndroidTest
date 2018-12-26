package android.ext.temp;

import java.io.IOException;
import org.json.JSONObject;
import android.app.Activity;
import android.ext.net.DownloadRequest;
import android.ext.util.JSONUtils;
import android.util.Log;
import com.tencent.test.MainApplication;

public class JsonLoader extends AsyncJsonLoader<String, JSONObject> {
    public JsonLoader(Object owner) {
        super(MainApplication.sInstance.getExecutor(), owner);
    }

    @Override
    protected void onStartLoading(String url, LoadParams<String>[] params) {
        if (params[0] instanceof JsonParams) {
            // Show loading UI
            // ... ...
            Log.i("abc", "Show loading UI.");
        }
    }

    @Override
    protected boolean validateResult(String url, LoadParams<String> params, JSONObject result) {
        return (JSONUtils.optInt(result, "retCode", 0) == 200);
    }

    @Override
    protected void onProgressUpdate(String url, LoadParams<String>[] params, Object[] values) {
        final Activity activity = getOwner();
        if (activity == null || activity.isDestroyed()) {
            return;
        }

        final JSONObject result = (JSONObject)values[0];
        if (result != null) {
            // Update UI
            // ... ...
            Log.i("abc", "Hit Cache Update UI.");
        } else {
            // Show loading UI
            // ... ...
            Log.i("abc", "Show loading UI.");
        }
    }

    @Override
    protected void onLoadComplete(String url, LoadParams<String>[] params, JSONObject result) {
        final Activity activity = getOwner();
        if (activity == null || activity.isDestroyed()) {
            return;
        }

        // Hide loading UI, if need.
        // ... ...

        if (result != null) {
            // Update UI
            // ... ...
            Log.i("abc", "Load Succeeded Update UI.");
        } else if (!params[0].isHitCache()) {
            // Load failed and cache not hit.
            // Show error UI.
            // ... ...
            Log.i("abc", "Show error UI.");
        }
    }

    public static class JsonParams extends LoadParams<String> {
        @Override
        public DownloadRequest newDownloadRequest(String url) throws IOException {
            return new DownloadRequest(url).connectTimeout(20000).readTimeout(20000);
        }
    }
}
