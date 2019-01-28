package android.ext.temp;

import org.json.JSONObject;
import android.app.Activity;
import android.ext.net.AsyncJsonTask;
import android.ext.net.DownloadRequest;
import android.util.Log;
import android.util.Pair;
import com.tencent.test.MainApplication;

public class JsonTask extends AsyncJsonTask<String, JSONObject> {
    public JsonTask(Object ownerActivity) {
        super(ownerActivity);
    }

    @Override
    protected String getCacheFile(String[] params) {
        return MainApplication.sInstance.getFilesDir().getPath() + "/.json_files/channel";
    }

    @Override
    protected DownloadRequest newDownloadRequest(String[] params) throws Exception {
        return new DownloadRequest(params[0]).connectTimeout(30000).readTimeout(30000);
    }

    @Override
    protected boolean validateResult(String[] params, JSONObject result) {
        return (result != null && result.optInt("retCode") == 200);
    }

    @Override
    protected void onPostExecute(Pair<JSONObject, Boolean> result) {
        final Activity activity = getOwnerActivity();
        if (activity == null) {
            // The owner activity has been destroyed or release by the GC.
            return;
        }

        // Hide loading UI, if need.
        if (result.first != null) {
            // Loading succeeded, update UI.
            Log.i("abc", "JsonTask - Load Succeeded Update UI.");
            //Toast.makeText(activity, "JsonTask - Load Succeeded Update UI.", Toast.LENGTH_SHORT).show();
        } else if (!result.second) {
            // Loading failed and file cache not hit, show error UI.
            Log.i("abc", "JsonTask - Show error UI.");
        }
    }
}
