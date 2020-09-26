package com.tencent.temp;

import android.app.Activity;
import android.ext.content.ResourceLoader.LoadParams;
import android.ext.content.ResourceTask;
import android.ext.json.JSONObject;
import android.util.Log;

public class JsonTask extends ResourceTask<String, JSONObject> {
    public JsonTask(Activity ownerActivity, String key) {
        super(ownerActivity, key);
    }

    @Override
    protected void onPostExecute(LoadParams<String, JSONObject>[] params, JSONObject result) {
        final Activity activity = getOwner();
        if (activity == null) {
            // The owner activity has been destroyed or release by the GC.
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
