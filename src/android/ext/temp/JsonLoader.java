package android.ext.temp;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLConnection;
import java.util.List;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.ext.net.DownloadPostRequest;
import android.ext.net.DownloadPostRequest.PostCallback;
import android.ext.net.DownloadRequest;
import android.ext.net.NetworkUtils;
import android.ext.util.ArrayUtils;
import android.ext.util.JSONUtils;
import android.ext.util.PackageUtils;
import android.ext.util.PackageUtils.InstalledPackageFilter;
import android.net.Uri;
import android.os.Build;
import android.util.JsonWriter;
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

    public static class PackageParams extends CacheLoadParams implements PostCallback {
        public PackageParams(Context context) {
            super(context);
        }

        @Override
        public DownloadRequest newDownloadRequest(String url) throws IOException {
            return new DownloadPostRequest(Uri.parse(url).buildUpon()
                .appendQueryParameter("model", Build.MODEL)
                .appendQueryParameter("channel", "2055")
                .appendQueryParameter("mac", NetworkUtils.getMacAddress(NetworkUtils.WLAN, ""))
                .appendQueryParameter("version", PackageUtils.myPackageInfo(mContext, 0).versionName)
                .toString())
                .post(this, (Object[])null)
                .readTimeout(30000)
                .connectTimeout(30000)
                .contentType("application/json");
        }

        @Override
        public void onPostData(URLConnection conn, Object[] params, byte[] tempBuffer) throws IOException {
            final JsonWriter writer = new JsonWriter(new OutputStreamWriter(conn.getOutputStream()));
            try {
                writer.beginArray();
                final List<PackageInfo> infos = PackageUtils.getInstalledPackages(mContext, 0, new InstalledPackageFilter(mContext.getPackageName()));
                for (int i = 0, size = ArrayUtils.getSize(infos); i < size; ++i) {
                    writer.value(infos.get(i).packageName);
                }
                writer.endArray();
            } finally {
                writer.close();
            }
        }
    }
}
