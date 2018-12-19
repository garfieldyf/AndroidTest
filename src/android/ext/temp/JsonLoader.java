package android.ext.temp;

import java.io.File;
import java.io.IOException;
import org.json.JSONObject;
import android.app.Activity;
import android.ext.net.DownloadRequest;
import android.ext.util.FileUtils;
import android.ext.util.JSONUtils;
import com.tencent.test.MainApplication;

public class JsonLoader extends AsyncJsonLoader<Object, JSONObject> {
    public JsonLoader(Object owner) {
        super(MainApplication.sInstance.getExecutor(), owner);
    }

    @Override
    protected void onProgressUpdate(Object key, LoadParams<Object, JSONObject>[] params, Object[] values) {
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
    protected void onLoadComplete(Object key, LoadParams<Object, JSONObject>[] params, JSONObject result) {
        final Activity activity = getOwner();
        if (activity == null || activity.isDestroyed()) {
            return;
        }

        // Hide loading UI, if need.
        // ... ...

        if (result != null) {
            // Update UI
            // ... ...
        } else if (!params[0].isHitCache()) {
            // Load failed and cache not hit.
            // Show error UI.
            // ... ...
        } else {
            // Hit cache, do nothing.
        }
    }

    public static final class JsonParams extends LoadParams<Object, JSONObject> {
        @Override
        public String getCacheFile(Object key) {
            // return new File(MainApplication.sInstance.getFilesDir(), ".json_files/" + key.toString()).getPath();
            return new File(FileUtils.getCacheDir(MainApplication.sInstance, ".json_files"), key.toString()).getPath();
        }

        @Override
        public boolean validateResult(Object key, JSONObject result) {
            return (JSONUtils.optInt(result, "retCode", 0) == 200);
        }

        @Override
        public DownloadRequest newDownloadRequest(Object key) throws IOException {
            return new DownloadRequest(key.toString()).connectTimeout(10000).readTimeout(10000);
        }
    }
}
