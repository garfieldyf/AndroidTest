package android.ext.temp;

import java.util.concurrent.Executor;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.ext.util.JSONUtils;
import android.net.Uri;

public class JsonLoader extends AsyncJsonLoader<JSONObject> {
    public JsonLoader(Context context, Executor executor, Object owner) {
        super(context, executor, owner);
    }

    @Override
    protected String parseUrl(String uri, LoadParams params) {
        return Uri.parse(uri).buildUpon()
            .appendQueryParameter("mode", "mode")
            .appendQueryParameter("mac", "mac")
            .toString();
    }

    @Override
    protected boolean validateResult(LoadParams params, JSONObject result) {
        return (JSONUtils.optInt(result, "retCode", 0) == 200);
    }

    @Override
    protected void onProgressUpdate(String key, LoadParams[] params, Object[] values) {
        final Activity activity = getOwner();
        if (activity == null || activity.isDestroyed()) {
            return;
        }

        final JSONObject result = (JSONObject)values[0];
        if (result != null) {
            // Update UI
            // ... ...
        } else {
            // Show loading UI.
            // ... ...
        }
    }

    @Override
    protected void onLoadComplete(String key, LoadParams[] params, JSONObject result) {
        final Activity activity = getOwner();
        if (activity == null || activity.isDestroyed()) {
            return;
        }

        // Hide loading UI, if need.
        // ... ...

        if (result != null) {
            // Update UI
            // ... ...
        } else if (!params[0].hitCache()) {
            // Show error UI.
            // ... ...
        }

        // Hit cache, do nothing.
    }
}
